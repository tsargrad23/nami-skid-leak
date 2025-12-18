package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;

@RegisterModule
public class FastPlaceModule extends Module {

    public final IntSetting delay = addSetting(new IntSetting("Delay", 1, 0, 5));
    public final IntSetting startDelay = addSetting(new IntSetting("StartDelay", 10, 0, 50));
    public final WhitelistSetting whitelist = addSetting(new WhitelistSetting("WhiteList", false, WhitelistSetting.Type.ANY));
    public final WhitelistSetting blacklist = addSetting(new WhitelistSetting("BlackList", false, WhitelistSetting.Type.ANY));

    public FastPlaceModule() {
        super("FastPlace", "Decreases cooldown between any type of use.", ModuleCategory.of("World"), "fastplace");
    }
}
