package me.lordpvp.phantomware.util;

import net.minecraft.item.ItemStack;

public class InventoryUtils {

    public static boolean isBroken(ItemStack stack, int threshold) {
        if (!stack.isDamageable()) return false;
        int max = stack.getMaxDamage();
        int damage = stack.getDamage();
        int percentRemaining = (int) (((max - damage) / (float) max) * 100);
        return percentRemaining <= threshold;
    }

}
