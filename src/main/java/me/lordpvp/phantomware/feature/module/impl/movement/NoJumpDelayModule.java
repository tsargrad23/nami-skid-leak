package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule
public class NoJumpDelayModule extends Module {

    public NoJumpDelayModule() {
        super("NoJumpDelay", "Removes vanilla jump delay which increases movement speed.", ModuleCategory.of("Movement"), "nojumpdelay");
    }
}
