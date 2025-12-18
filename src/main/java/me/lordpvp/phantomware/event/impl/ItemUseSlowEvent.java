package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;

public class ItemUseSlowEvent extends Event {
    private final PlayerEntity player;
    private final ItemStack item;

    public ItemUseSlowEvent(PlayerEntity player, ItemStack item) {
        this.player = player;
        this.item = item;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public ItemStack getItem() {
        return item;
    }
}
