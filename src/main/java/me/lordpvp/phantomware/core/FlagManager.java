package me.kiriyaga.nami.core;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

import static me.kiriyaga.nami.Nami.EVENT_MANAGER;
// this is mostly for grimv2 and not updated checks in v3, since we can read it source code
public class FlagManager {
    private Vec3d lastSetbackPosition;
    private long lastSetbackTime;
    private int lastTeleportId;

    private final int[] pendingTransactions = new int[4];
    private int transactionIndex;

    public void init() {
        EVENT_MANAGER.register(this);
        Arrays.fill(pendingTransactions, -1);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof CommonPingS2CPacket packet) {
            if (transactionIndex > 3) return;

            pendingTransactions[transactionIndex] = packet.getParameter();
            transactionIndex++;
        } else if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            lastSetbackPosition = packet.comp_3228().comp_3148();
            lastSetbackTime = System.currentTimeMillis();
            lastTeleportId = packet.teleportId();
        }
    }

    public boolean hasElapsedSinceSetback(long milliseconds) {
        return lastSetbackPosition != null && (System.currentTimeMillis() - lastSetbackTime) >= milliseconds;
    }

    public Vec3d getLastSetbackPosition() {
        return lastSetbackPosition;
    }

    public long getLastSetbackTime() {
        return lastSetbackTime;
    }

    public int getLastTeleportId() {
        return lastTeleportId;
    }
}