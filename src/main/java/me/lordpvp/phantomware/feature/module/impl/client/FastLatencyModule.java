package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

@RegisterModule
public class FastLatencyModule extends Module {

    public enum FastLatencyMode {
        OLD,
        NEW,
        OFF
    }

    public final EnumSetting<FastLatencyMode> fastLatencyMode = addSetting(new EnumSetting<>("Mode", FastLatencyMode.NEW));
    public final IntSetting smoothingStrength = addSetting(new IntSetting("Smooth", 10, 1, 50));
    public final IntSetting unstableConnectionTimeout = addSetting(new IntSetting("Unstable", 3, 1, 60));
    public final IntSetting keepAliveInterval = addSetting(new IntSetting("Interval", 900, 250, 2500));

    public FastLatencyModule() {
        super("FastLatency", "Defines how ping should be received.", ModuleCategory.of("Client"), "ping", "manager", "managr", "png");

        smoothingStrength.setShowCondition(() -> fastLatencyMode.get() == FastLatencyMode.OLD);
        unstableConnectionTimeout.setShowCondition(() -> fastLatencyMode.get() != FastLatencyMode.OFF);
        keepAliveInterval.setShowCondition(() -> fastLatencyMode.get() == FastLatencyMode.OLD);
    }

    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }
}
