package com.playertracker.client;

import com.playertracker.PlayerTracker;
import com.playertracker.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端初始化：注册物品模型谓词 {@code minecraft:angle}。
 * <p>
 * NeoForge 为 {@code ItemProperties.register} 补了公开重载（参数类型 {@link ItemPropertyFunction}），
 * 因此不需要 AccessTransformer。
 */
@EventBusSubscriber(modid = PlayerTracker.MOD_ID, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.PLAYER_COMPASS.get(),
                ResourceLocation.withDefaultNamespace("angle"),
                (ItemPropertyFunction) CompassAngleProperty.INSTANCE));
    }
}
