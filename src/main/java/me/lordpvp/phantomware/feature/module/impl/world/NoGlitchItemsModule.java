package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.ScreenHandlerAccessor;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.ScreenHandler;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class NoGlitchItemsModule extends Module {
    public NoGlitchItemsModule() {
        super("NoGlitchItems", "Prevents ghost items in inventory.", ModuleCategory.of("World"), "noglitchitems");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (MC.player == null || !(event.getPacket() instanceof InventoryS2CPacket packet)) return;

        ScreenHandler handler = MC.player.currentScreenHandler;
        if (handler.syncId != packet.syncId()) return;

        int revisionID = packet.revision();
        if (revisionID > 0 && revisionID < handler.getRevision()) {
            ((ScreenHandlerAccessor) handler).setRevision(handler.nextRevision());
        }
    }
}
