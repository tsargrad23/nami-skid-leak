package me.kiriyaga.nami.feature.module.impl.miscellaneous;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule
public class NameProtectModule extends Module {

    public NameProtectModule() {
        super("NameProtect", "Changes client name on all client side accessible sides.", ModuleCategory.of("Miscellaneous"), "nameprotect");
    }
}
