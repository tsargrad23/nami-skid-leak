package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;

@RegisterModule
public class ViewModelModule extends Module {

    public final BoolSetting hand = addSetting(new BoolSetting("Hand", false));
    public final BoolSetting eating = addSetting(new BoolSetting("Eating", true));
    public final DoubleSetting eatingBob = addSetting(new DoubleSetting("EatingBob", 1.00, 0.00, 1.00));
    public final BoolSetting sway = addSetting(new BoolSetting("Sway", true));
    public final BoolSetting oldAnimation = addSetting(new BoolSetting("OldAnimation", false));
    public final DoubleSetting scale = addSetting(new DoubleSetting("Scale", 1.0, 0.1, 2));
    public final DoubleSetting posX = addSetting(new DoubleSetting("PosX", 0.0, -3, 3));
    public final DoubleSetting posY = addSetting(new DoubleSetting("PosY", 0.0, -3, 3));
    public final DoubleSetting posZ = addSetting(new DoubleSetting("PosZ", 0.0, -3, 3));
    public final DoubleSetting rotX = addSetting(new DoubleSetting("RotX", 0.0, -180.0, 180.0));
    public final DoubleSetting rotY = addSetting(new DoubleSetting("RotY", 0.0, -180.0, 180.0));
    public final DoubleSetting rotZ = addSetting(new DoubleSetting("RotZ", 0.0, -180.0, 180.0));

    public ViewModelModule() {
        super("Viewmodel", "Modifies hand position, scale, and rotation.", ModuleCategory.of("Render"), "vm", "handpos");
    eatingBob.setShowCondition(() -> eating.get());
    }
}
