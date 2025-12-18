package me.lordpvp.phantomware.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public class EnchantmentUtils {

    public static int getEnchantmentLevel(ItemStack stack, RegistryKey<Enchantment> enchantmentKey) {
        var components = stack.getComponents();

        if (!components.contains(DataComponentTypes.ENCHANTMENTS)) {
            return 0;
        }

        var enchantments = components.get(DataComponentTypes.ENCHANTMENTS);

        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments.getEnchantmentEntries()) {
            var keyOptional = entry.getKey().getKey();

            if (keyOptional.isPresent() && keyOptional.get().equals(enchantmentKey)) {
                return entry.getIntValue();
            }
        }

        return 0;
    }
}
