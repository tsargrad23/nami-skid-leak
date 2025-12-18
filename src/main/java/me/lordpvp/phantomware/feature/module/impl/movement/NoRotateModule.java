package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.PlayerPositionAccessor;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class NoRotateModule extends Module {

    public NoRotateModule() {
        super("NoRotate", "Prevents you from receiving forced server rotate packets.", ModuleCategory.of("Movement"), "norotate", "antirotate");
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onPacketRecieve(PacketReceiveEvent ev){
        if (ev.getPacket() instanceof PlayerPositionLookS2CPacket packet && MC.player != null && MC.world != null) {

            ((PlayerPositionAccessor) (Object) packet.comp_3228()).setYaw(MC.player.getYaw());
            ((PlayerPositionAccessor) (Object) packet.comp_3228()).setPitch(MC.player.getPitch());

            packet.comp_3229().remove(PositionFlag.X_ROT);
            packet.comp_3229().remove(PositionFlag.Y_ROT);
        }
    }
}
