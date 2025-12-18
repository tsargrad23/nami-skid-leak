package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule
public class NoBreakDelayModule extends Module {

    public NoBreakDelayModule() {
        super("NoBreakDelay", "Removes vanilla break delay which increases break speed.", ModuleCategory.of("World"), "nobreakdelay");
    }
}
