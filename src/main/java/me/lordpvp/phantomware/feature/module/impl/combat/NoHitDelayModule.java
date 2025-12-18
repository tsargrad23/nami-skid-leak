package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule
public class NoHitDelayModule extends Module {

    public NoHitDelayModule() {
        super("NoHitDelay", "Removes vanilla hit delay which increases hit speed.", ModuleCategory.of("Combat"), "nohitdelay");
    }
}
