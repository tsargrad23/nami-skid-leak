package me.kiriyaga.nami.feature.module.impl.miscellaneous;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

@RegisterModule
public class UnfocusedFpsModule extends Module {

    public final IntSetting limit = addSetting(new IntSetting("Limit", 15, 5, 30));

    public UnfocusedFpsModule() {
        super("UnfocusedFPS", "Limits your frame generation while unfocused.", ModuleCategory.of("Miscellaneous"), "unfocusedcpu");
    }
}
