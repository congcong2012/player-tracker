package com.playertracker.network;

import com.playertracker.PlayerTracker;
import com.playertracker.client.ClientTrackCache;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * 网络注册。
 * <p>
 * 注意：处理器写成 lambda，让客户端类 {@code ClientTrackCache} 只在真正收到包时才被解析，
 * 专用服务器启动时不会因为缺客户端类而崩。
 */
@EventBusSubscriber(modid = PlayerTracker.MOD_ID)
public final class ModNetwork {

    private ModNetwork() {
    }

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                TrackSyncPayload.TYPE,
                TrackSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientTrackCache.accept(payload)));
    }
}
