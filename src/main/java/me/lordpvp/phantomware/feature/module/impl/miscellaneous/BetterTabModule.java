package me.kiriyaga.nami.feature.module.impl.miscellaneous;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

@RegisterModule
public class BetterTabModule extends Module {

    public final IntSetting limit = addSetting(new IntSetting("Limit", 300, 25, 2500));
    public final DoubleSetting scale = addSetting(new DoubleSetting("Scale", 1.00, 0.50, 1.50));
    //public final IntSetting columns = addSetting(new IntSetting("columns", 4, 1, 20));
    //public final IntSetting rows = addSetting(new IntSetting("rows", 5, 1, 20));
    public final BoolSetting friendsOnly = addSetting(new BoolSetting("OnlyFriends", false));
    public final BoolSetting highlighFriends = addSetting(new BoolSetting("Highlight", true));

    public BetterTabModule() {
        super("BetterTab", "Extends tab limits and tweaks.", ModuleCategory.of("Miscellaneous"), "bettertab");
    }
}
