package com.playertracker.tracking;

import com.playertracker.network.TrackSyncPayload;
import com.playertracker.registry.ModComponents;
import com.playertracker.registry.ModItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

/**
 * 服务端追踪状态解析：每个同步周期检查玩家双手的指南针，把目标状态推给客户端。
 */
public final class TrackingService {

    /** 每 10 tick（0.5 秒）同步一次，足够让 HUD 距离跟手，也不至于刷包。 */
    private static final int SYNC_INTERVAL_TICKS = 10;

    private TrackingService() {
    }

    public static void tick(ServerPlayer player) {
        if (player.tickCount % SYNC_INTERVAL_TICKS != 0) {
            return;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            syncHand(player, hand);
        }
    }

    /**
     * 立即为指定手推送一次状态；未绑定或不是本模组的指南针时不做任何事。
     */
    public static void syncHand(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.PLAYER_COMPASS.get())) {
            return;
        }

        UUID targetId = stack.get(ModComponents.TARGET.get());
        if (targetId == null) {
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        String storedName = stack.getOrDefault(ModComponents.TARGET_NAME.get(), "");
        ServerPlayer target = server.getPlayerList().getPlayer(targetId);

        if (target == null) {
            PacketDistributor.sendToPlayer(player, new TrackSyncPayload(
                    targetId,
                    storedName,
                    TrackStatus.OFFLINE.id(),
                    player.level().dimension().location(),
                    0.0D, 0.0D, 0.0D,
                    -1.0D));
            return;
        }

        boolean sameDimension = target.level().dimension().equals(player.level().dimension());
        TrackStatus status = sameDimension ? TrackStatus.ONLINE_SAME_DIM : TrackStatus.ONLINE_OTHER_DIM;
        double distance = sameDimension ? Math.sqrt(player.distanceToSqr(target)) : -1.0D;

        PacketDistributor.sendToPlayer(player, new TrackSyncPayload(
                targetId,
                target.getScoreboardName(),
                status.id(),
                target.level().dimension().location(),
                target.getX(), target.getY(), target.getZ(),
                distance));
    }
}
