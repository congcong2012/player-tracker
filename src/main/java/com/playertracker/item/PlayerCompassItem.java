package com.playertracker.item;

import com.playertracker.registry.ModComponents;
import com.playertracker.tracking.TrackingService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

/**
 * 玩家追踪指南针。
 * <p>
 * 交互：
 * <ul>
 *   <li>右键玩家：绑定目标；再次右键当前目标则解除</li>
 *   <li>潜行 + 右键：清除目标</li>
 * </ul>
 * 渲染与 HUD 都读取同一个 UUID 组件，服务端只负责补发实时状态（在线 / 维度 / 距离）。
 */
public class PlayerCompassItem extends Item {

    public PlayerCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Player trackedPlayer)) {
            return InteractionResult.PASS;
        }

        // 客户端只负责挥手动画，真正的状态变更交给服务端
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (trackedPlayer == player) {
            message(player, "message.playertracker.self");
            return InteractionResult.SUCCESS;
        }

        UUID current = stack.get(ModComponents.TARGET.get());
        if (trackedPlayer.getUUID().equals(current)) {
            clearTarget(stack);
            message(player, "message.playertracker.cleared");
        } else {
            stack.set(ModComponents.TARGET.get(), trackedPlayer.getUUID());
            stack.set(ModComponents.TARGET_NAME.get(), trackedPlayer.getScoreboardName());
            message(player, "message.playertracker.bound", trackedPlayer.getDisplayName());
        }

        player.getInventory().setChanged();
        if (player instanceof ServerPlayer serverPlayer) {
            // 立即推一次状态，避免客户端等下一个同步周期
            TrackingService.syncHand(serverPlayer, hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.has(ModComponents.TARGET.get()) || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide()) {
            clearTarget(stack);
            message(player, "message.playertracker.cleared");
            player.getInventory().setChanged();
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // 已绑定时带附魔光效，一眼区分「有目标 / 无目标」
        return stack.has(ModComponents.TARGET.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        UUID target = stack.get(ModComponents.TARGET.get());
        if (target == null) {
            tooltip.add(Component.translatable("item.playertracker.player_compass.unbound")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            String name = stack.getOrDefault(ModComponents.TARGET_NAME.get(), "?");
            tooltip.add(Component.translatable("item.playertracker.player_compass.tracked", name)
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.playertracker.player_compass.hint_bind")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.playertracker.player_compass.hint_clear")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void clearTarget(ItemStack stack) {
        stack.remove(ModComponents.TARGET.get());
        stack.remove(ModComponents.TARGET_NAME.get());
    }

    private static void message(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }
}
