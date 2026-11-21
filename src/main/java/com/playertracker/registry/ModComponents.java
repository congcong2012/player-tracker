package com.playertracker.registry;

import com.mojang.serialization.Codec;
import com.playertracker.PlayerTracker;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

/**
 * 指南针的持久化数据。
 * <p>
 * 两个组件都声明了 {@code networkSynchronized}，所以物品栏内容一变化就会随容器同步包发到客户端，
 * 客户端无需额外 RPC 就能知道「正在追踪谁」。
 */
public final class ModComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, PlayerTracker.MOD_ID);

    /** 目标玩家的 UUID。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> TARGET =
            COMPONENTS.registerComponentType("target", builder -> builder
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC));

    /** 绑定时的玩家名，用于目标离线时显示。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> TARGET_NAME =
            COMPONENTS.registerComponentType("target_name", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private ModComponents() {
    }
}
