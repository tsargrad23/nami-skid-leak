package me.kiriyaga.nami.feature.module.impl.miscellaneous;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;

@RegisterModule
public class ReachModule extends Module {

    public final DoubleSetting block = addSetting(new DoubleSetting("Block", 1.00,0.00, 3.00));
    public final DoubleSetting entity = addSetting(new DoubleSetting("Entity", 0.00,0.00, 3.00));

    public final BoolSetting noEntityTrace = addSetting(new BoolSetting("NoEntityTrace", false));
    public final BoolSetting playerOnly = addSetting(new BoolSetting("PlayerOnly", false));
    public final BoolSetting pickaxeOnly = addSetting(new BoolSetting("PickaxeOnly", false));


    public ReachModule() {
        super("Reach", "Extends player reach values.", ModuleCategory.of("Miscellaneous"));
        playerOnly.setShowCondition(noEntityTrace::get);
        pickaxeOnly.setShowCondition(noEntityTrace::get);
    }
}
