package com.playertracker.client;

import com.playertracker.registry.ModComponents;
import com.playertracker.tracking.TrackStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 指南针指针角度（对应模型里 {@code minecraft:angle} 谓词，取值 0~1 对应原版 32 帧）。
 * <p>
 * 计算方式与原版 {@code CompassItemPropertyFunction} 保持一致：
 * 先把「目标相对玩家」的方向角换算成 0~1，再减去玩家朝向，最后取正模。
 * 目标不在同维度 / 没有数据时返回缓慢旋转值，用「转圈」表示无信号。
 */
@SuppressWarnings("deprecation")
public final class CompassAngleProperty implements ItemPropertyFunction {

    public static final CompassAngleProperty INSTANCE = new CompassAngleProperty();

    private CompassAngleProperty() {
    }

    @Override
    public float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        if (level == null || entity == null) {
            return 0.0F;
        }

        long gameTime = level.getGameTime();
        UUID targetId = stack.get(ModComponents.TARGET.get());
        if (targetId == null) {
            return idle(gameTime, seed);
        }

        double targetX;
        double targetZ;
        boolean valid = false;

        // 优先用客户端本地实体（位置更新更平滑），跨维度或未加载时退回服务端同步的位置
        Player online = level.getPlayerByUUID(targetId);
        if (online != null) {
            targetX = online.getX();
            targetZ = online.getZ();
            valid = true;
        } else {
            ClientTrackCache.TrackData data = ClientTrackCache.get(targetId);
            if (data != null
                    && !data.isStale()
                    && data.status() == TrackStatus.ONLINE_SAME_DIM
                    && data.dimension().equals(level.dimension())) {
                targetX = data.x();
                targetZ = data.z();
                valid = true;
            } else {
                targetX = 0.0D;
                targetZ = 0.0D;
            }
        }

        if (!valid) {
            return idle(gameTime, seed);
        }

        double dx = targetX - entity.getX();
        double dz = targetZ - entity.getZ();
        if (dx * dx + dz * dz < 1.0E-4D) {
            return idle(gameTime, seed);
        }

        double angle = Math.atan2(dz, dx) / (Math.PI * 2.0D);
        double visualRotation = Mth.positiveModulo(entity.getVisualRotationYInDegrees() / 360.0D, 1.0D);
        return (float) Mth.positiveModulo(0.5D - (visualRotation - 0.25D - angle), 1.0D);
    }

    /** 无有效目标时的慢速空转。 */
    private static float idle(long gameTime, int seed) {
        return (float) Mth.positiveModulo(gameTime * 0.01D + (seed & 7) * 0.125D, 1.0D);
    }
}
