package com.playertracker.registry;

import com.playertracker.PlayerTracker;
import com.playertracker.item.PlayerCompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PlayerTracker.MOD_ID);

    public static final DeferredItem<PlayerCompassItem> PLAYER_COMPASS = ITEMS.registerItem(
            "player_compass",
            PlayerCompassItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    private ModItems() {
    }
}
