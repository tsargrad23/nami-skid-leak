package me.kiriyaga.nami.feature.module.impl.miscellaneous;


import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;

@RegisterModule
public class NoPacketKick extends Module {

    public NoPacketKick() {
        super("NoPacketKick", "Prevents from kicking because of netty exceptions.", ModuleCategory.of("Miscellaneous"), "npacketkick", "antipacketkick");
    }
}
