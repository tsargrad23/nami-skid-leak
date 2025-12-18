package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.WorldTimeUpdateEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class ClientTimeModule extends Module {
    public final IntSetting value = addSetting(new IntSetting("Time", 25000, 0, 25000));

    public ClientTimeModule() {
        super("ClientTime", "Sets game time client side.", ModuleCategory.of("Render"));
    }

    @SubscribeEvent
    private void onPacketReceiveEvent(WorldTimeUpdateEvent event) {
        event.cancel();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onPreTickEvent(PreTickEvent event) {
        if (MC.world == null || MC.player == null)
            return;

        MC.world.getLevelProperties().setTimeOfDay((long)value.get());
    }
}