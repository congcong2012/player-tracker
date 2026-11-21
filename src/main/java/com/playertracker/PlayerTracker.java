package com.playertracker;

import com.mojang.logging.LogUtils;
import com.playertracker.registry.ModComponents;
import com.playertracker.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Player Tracker Compass - 玩家追踪指南针
 * <p>
 * 一个复用原版指南针贴图与模型的追踪物品：右键玩家绑定目标，手持时指针指向目标并显示距离。
 */
@Mod(PlayerTracker.MOD_ID)
public class PlayerTracker {
    public static final String MOD_ID = "playertracker";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PlayerTracker(IEventBus modBus, ModContainer container) {
        // 数据组件先于物品注册，保证物品属性里可以安全引用
        ModComponents.COMPONENTS.register(modBus);
        ModItems.ITEMS.register(modBus);
        LOGGER.info("[Player Tracker] 玩家追踪指南针已装载");
    }
}
