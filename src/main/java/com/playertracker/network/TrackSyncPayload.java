package com.playertracker.network;

import com.playertracker.PlayerTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * 服务端 -> 客户端：当前指南针目标的实时状态。
 *
 * @param target    目标 UUID
 * @param name      目标名（离线时用绑定时保存的名字）
 * @param status    {@link com.playertracker.tracking.TrackStatus} 的序号
 * @param dimension 目标所在维度
 * @param x         目标 X
 * @param y         目标 Y
 * @param z         目标 Z
 * @param distance  与持有者的距离（仅同维度时有意义，否则为 -1）
 */
public record TrackSyncPayload(UUID target, String name, byte status, ResourceLocation dimension,
                               double x, double y, double z, double distance) implements CustomPacketPayload {

    public static final Type<TrackSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PlayerTracker.MOD_ID, "track_sync"));

    public static final StreamCodec<FriendlyByteBuf, TrackSyncPayload> STREAM_CODEC =
            StreamCodec.of(TrackSyncPayload::encode, TrackSyncPayload::decode);

    private static void encode(FriendlyByteBuf buf, TrackSyncPayload payload) {
        buf.writeUUID(payload.target);
        buf.writeUtf(payload.name, 64);
        buf.writeByte(payload.status);
        buf.writeResourceLocation(payload.dimension);
        buf.writeDouble(payload.x);
        buf.writeDouble(payload.y);
        buf.writeDouble(payload.z);
        buf.writeDouble(payload.distance);
    }

    private static TrackSyncPayload decode(FriendlyByteBuf buf) {
        return new TrackSyncPayload(
                buf.readUUID(),
                buf.readUtf(64),
                buf.readByte(),
                buf.readResourceLocation(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
