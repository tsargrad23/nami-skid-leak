package me.lordpvp.phantomware.core;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import static me.kiriyaga.nami.Nami.*;

import java.util.Arrays;

public class TickRateManager {

    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private int count = 0;
    private long lastTimeUpdate = -1;

    public void init() {
        EVENT_MANAGER.register(this);
        LOGGER.info("Tick Manager loaded");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            long now = System.currentTimeMillis();

            if (lastTimeUpdate != -1) {
                float elapsed = (now - lastTimeUpdate) / 1000.0f;
                float tps = 20.0f / elapsed;
                tickRates[nextIndex % tickRates.length] = Math.min(Math.max(tps, 0.0f), 20.0f);
                nextIndex++;
                count = Math.min(count + 1, tickRates.length);
            }

            lastTimeUpdate = now;
        }
    }

    public float getAverageTPS() {
        if (count == 0) return 20.0f;

        float sum = 0.0f;
        int valid = 0;
        for (int i = 0; i < count; i++) {
            float t = tickRates[i];
            if (t > 0.0f) {
                sum += t;
                valid++;
            }
        }
        return valid == 0 ? 20.0f : Math.min(Math.max(sum / valid, 0.0f), 20.0f);
    }

    public float getMinTPS() {
        if (count == 0) return 20.0f;

        float min = 20.0f;
        for (int i = 0; i < count; i++) {
            float t = tickRates[i];
            if (t > 0.0f && t < min) {
                min = t;
            }
        }
        return Math.min(Math.max(min, 0.0f), 20.0f);
    }

    public float getLatestTPS() {
        if (count == 0) return 20.0f;

        int last = (nextIndex - 1 + tickRates.length) % tickRates.length;
        return Math.min(Math.max(tickRates[last], 0.0f), 20.0f);
    }
}
