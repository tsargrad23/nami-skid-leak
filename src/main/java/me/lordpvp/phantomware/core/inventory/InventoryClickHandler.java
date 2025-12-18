package me.kiriyaga.nami.core.inventory;

import me.kiriyaga.nami.feature.module.impl.combat.AutoTotemModule;
import me.kiriyaga.nami.util.EnchantmentUtils;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

import com.google.common.collect.Lists;

import java.util.List;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class InventoryClickHandler {

    public void pickupSlot(int slotIndex, boolean skipGeneric) {
        click(slotIndex, 0, SlotActionType.PICKUP, skipGeneric);
    }

    public void quickMoveSlot(int slotIndex, boolean skipGeneric) {
        click(slotIndex, 0, SlotActionType.QUICK_MOVE, skipGeneric);
    }

    public void throwSlot(int slotIndex, boolean skipGeneric) {
        click(slotIndex, 0, SlotActionType.THROW, skipGeneric);
    }

    public void swapSlot(int targetSlot, int hotbarSlotIndex, boolean skipGeneric) {
        click(targetSlot, hotbarSlotIndex, SlotActionType.SWAP, skipGeneric);
    }


    public void pickupSlot(int slotIndex) {
        click(slotIndex, 0, SlotActionType.PICKUP);
    }

    public void quickMoveSlot(int slotIndex) {
        click(slotIndex, 0, SlotActionType.QUICK_MOVE);
    }

    public void throwSlot(int slotIndex) {
        click(slotIndex, 0, SlotActionType.THROW);
    }

    public void swapSlot(int targetSlot, int hotbarSlotIndex) {
        click(targetSlot, hotbarSlotIndex, SlotActionType.SWAP);
    }

    private void click(int slot, int button, SlotActionType type) {
        click(slot, button, type, false);
    }

    private void click(int slot, int button, SlotActionType type, boolean skipGeneric) {
        if (slot < 0) return;

        if (MC.currentScreen instanceof ShulkerBoxScreen
                || MC.currentScreen instanceof AnvilScreen
                || MC.currentScreen instanceof BrewingStandScreen
                || MC.currentScreen instanceof CartographyTableScreen
                || MC.currentScreen instanceof CrafterScreen
                || MC.currentScreen instanceof EnchantmentScreen
                || MC.currentScreen instanceof FurnaceScreen
                || MC.currentScreen instanceof GrindstoneScreen
                || MC.currentScreen instanceof HopperScreen
                || MC.currentScreen instanceof HorseScreen
                || MC.currentScreen instanceof MerchantScreen
                || MC.currentScreen instanceof SmithingScreen
                || MC.currentScreen instanceof SmokerScreen
                || MC.currentScreen instanceof StonecutterScreen
                || (MC.currentScreen instanceof GenericContainerScreen && !skipGeneric)
                || MC.currentScreen instanceof CreativeInventoryScreen) {
            MODULE_MANAGER.getStorage().getByClass(AutoTotemModule.class).addDeathReason("invfail", "Inventory Fail");
            return;
        }

        ScreenHandler handler = MC.player.currentScreenHandler;

        DefaultedList<Slot> slots = handler.slots;
        List<ItemStack> before = Lists.newArrayListWithCapacity(slots.size());
        for (Slot s : slots) before.add(s.getStack().copy());

        MC.interactionManager.clickSlot(handler.syncId, slot, button, type, MC.player);
    }
}