package com.playertracker.client;

import com.playertracker.network.TrackSyncPayload;
import com.playertracker.tracking.TrackStatus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端缓存：UUID -> 服务端最近一次推送的目标状态。
 * <p>
 * 按 UUID 存而不是「只存最后一个」，这样主副手各拿一个绑定不同目标的指南针也不会串。
 */
public final class ClientTrackCache {

    /** 超过这个时间没收到新包，就认为数据已失效（正常每 0.5 秒一次）。 */
    private static final long STALE_MILLIS = 3000L;

    public record TrackData(UUID target, String name, TrackStatus status, ResourceKey<Level> dimension,
                            double x, double y, double z, double distance, long updatedAt) {

        public boolean isStale() {
            return System.currentTimeMillis() - this.updatedAt > STALE_MILLIS;
        }
    }

    private static final Map<UUID, TrackData> DATA = new ConcurrentHashMap<>();

    private ClientTrackCache() {
    }

    public static void accept(TrackSyncPayload payload) {
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, payload.dimension());
        DATA.put(payload.target(), new TrackData(
                payload.target(),
                payload.name(),
                TrackStatus.byId(payload.status()),
                dimension,
                payload.x(), payload.y(), payload.z(),
                payload.distance(),
                System.currentTimeMillis()));
    }

    public static TrackData get(UUID target) {
        return target == null ? null : DATA.get(target);
    }

    public static void clear() {
        DATA.clear();
    }
}
