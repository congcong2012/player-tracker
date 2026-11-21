package com.playertracker.event;

import com.playertracker.PlayerTracker;
import com.playertracker.tracking.TrackingService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 游戏总线事件：每 tick 的追踪状态同步。
 */
@EventBusSubscriber(modid = PlayerTracker.MOD_ID)
public final class TrackingEvents {

    private TrackingEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TrackingService.tick(player);
        }
    }
}
