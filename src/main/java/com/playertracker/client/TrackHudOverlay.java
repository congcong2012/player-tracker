package com.playertracker.client;

import com.playertracker.PlayerTracker;
import com.playertracker.registry.ModComponents;
import com.playertracker.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 手持指南针时的状态提示：目标名 + 距离 / 维度 / 离线 / 未绑定。
 */
@EventBusSubscriber(modid = PlayerTracker.MOD_ID, value = Dist.CLIENT)
public final class TrackHudOverlay {

    /** 距屏幕底部的像素偏移：落在原版物品名 / 动作栏文字（约 -59）之上，避免重叠。 */
    private static final int BOTTOM_OFFSET = 72;

    private TrackHudOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }

        ItemStack stack = findCompass(minecraft.player);
        if (stack == null) {
            return;
        }

        Component line = buildLine(stack);
        GuiGraphics graphics = event.getGuiGraphics();
        Font font = minecraft.font;
        int centerX = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() - BOTTOM_OFFSET;
        int halfWidth = font.width(line) / 2;

        graphics.fill(centerX - halfWidth - 3, y - 2, centerX + halfWidth + 3, y + 10, 0x66000000);
        graphics.drawCenteredString(font, line, centerX, y, 0xFFFFFF);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        // 退出世界时清掉旧存档的追踪数据
        ClientTrackCache.clear();
    }

    @Nullable
    private static ItemStack findCompass(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(ModItems.PLAYER_COMPASS.get())) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (offHand.is(ModItems.PLAYER_COMPASS.get())) {
            return offHand;
        }
        return null;
    }

    private static Component buildLine(ItemStack stack) {
        UUID target = stack.get(ModComponents.TARGET.get());
        if (target == null) {
            return Component.translatable("hud.playertracker.unbound").withStyle(ChatFormatting.GRAY);
        }

        ClientTrackCache.TrackData data = ClientTrackCache.get(target);
        if (data == null || data.isStale()) {
            return Component.translatable("hud.playertracker.syncing").withStyle(ChatFormatting.DARK_GRAY);
        }

        String name = data.name().isEmpty()
                ? stack.getOrDefault(ModComponents.TARGET_NAME.get(), "?")
                : data.name();

        return switch (data.status()) {
            case ONLINE_SAME_DIM -> Component.translatable("hud.playertracker.distance",
                    name, Long.toString(Math.round(data.distance()))).withStyle(ChatFormatting.GREEN);
            case ONLINE_OTHER_DIM -> Component.translatable("hud.playertracker.other_dimension",
                    name, dimensionName(data.dimension())).withStyle(ChatFormatting.YELLOW);
            case OFFLINE -> Component.translatable("hud.playertracker.offline", name)
                    .withStyle(ChatFormatting.DARK_GRAY);
            case UNBOUND -> Component.translatable("hud.playertracker.unbound").withStyle(ChatFormatting.GRAY);
        };
    }

    private static Component dimensionName(ResourceKey<Level> dimension) {
        ResourceLocation id = dimension.location();
        // 原版维度自带 dimension.minecraft.overworld / the_nether / the_end 翻译键
        return Component.translatable("dimension." + id.getNamespace() + "." + id.getPath());
    }
}
