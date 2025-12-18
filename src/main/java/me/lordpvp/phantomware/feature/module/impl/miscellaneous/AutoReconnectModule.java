package me.kiriyaga.nami.feature.module.impl.miscellaneous;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

@RegisterModule
public class AutoReconnectModule extends Module {

    public final BoolSetting hardHide = addSetting(new BoolSetting("HideMenu", false));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 5, 0, 80));

    public AutoReconnectModule() {
        super("AutoReconnect", "Automatically reconnects to the specified server.", ModuleCategory.of("Miscellaneous"), "autoreconnect");
    }
}
