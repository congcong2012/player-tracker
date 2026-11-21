package com.playertracker.event;

import com.playertracker.PlayerTracker;
import com.playertracker.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * 模组总线事件：把指南针放进原版「工具与实用物品」页签。
 */
@EventBusSubscriber(modid = PlayerTracker.MOD_ID)
public final class CreativeTabEvents {

    private CreativeTabEvents() {
    }

    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (CreativeModeTabs.TOOLS_AND_UTILITIES.equals(event.getTabKey())) {
            event.accept(new ItemStack(ModItems.PLAYER_COMPASS.get()));
        }
    }
}
