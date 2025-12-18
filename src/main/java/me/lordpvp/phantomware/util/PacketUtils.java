package me.lordpvp.phantomware.util;

import me.kiriyaga.nami.mixin.ClientWorldAccessor;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;

import static me.kiriyaga.nami.Nami.MC;

public class PacketUtils {

    public static void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        if (MC.world == null || MC.getNetworkHandler() == null) {
            return;
        }

        PendingUpdateManager p = ((ClientWorldAccessor) MC.world).getPendingUpdateManager().incrementSequence();

        try (p) {
            int sequence = p.getSequence();
            MC.getNetworkHandler().sendPacket(packetCreator.predict(sequence));
        }
    }
}
