package me.kiriyaga.nami.core.inventory;

import me.kiriyaga.nami.mixininterface.IClientPlayerInteractionManager;

import static me.kiriyaga.nami.Nami.*;

public class InventorySlotHandler {

    // this shit was overengeneered and uncompat with like any client so i made it mc vanilla way
    public void attemptSwitch(int targetSlot) {
        if (MC.player == null || MC.world == null || MC.interactionManager == null || targetSlot < 0 || targetSlot > 8)
            return;

        MC.player.getInventory().setSelectedSlot(targetSlot);
        syncSelectedSlot();
    }

    public void syncSelectedSlot(){
        ((IClientPlayerInteractionManager)MC.interactionManager).updateSlot(); // this one is the same as mc default one
    }
}