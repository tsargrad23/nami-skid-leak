package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

@RegisterModule
public class TargetModule extends Module {

    public final IntSetting maxIdleTicks = addSetting(new IntSetting("IdleTicks", 500, 250, 750));
    public final DoubleSetting targetRange = addSetting(new DoubleSetting("Range", 5.0, 4.0, 16.0));
    public final DoubleSetting minTicksExisted = addSetting(new DoubleSetting("Age", 12, 0.0, 20.0));
    public final BoolSetting targetPlayers = addSetting(new BoolSetting("Players", true));
    public final BoolSetting targetHostiles = addSetting(new BoolSetting("Hostiles", true));
    public final BoolSetting targetNeutrals = addSetting(new BoolSetting("Neutrals", false));
    public final BoolSetting targetPassives = addSetting(new BoolSetting("Passives", false));
    public final BoolSetting targetPrijectiles = addSetting(new BoolSetting("Projectiles", true));
    public final EnumSetting<TargetPriority> priority = addSetting(new EnumSetting<>("Priority", TargetPriority.SMART));

    public enum TargetPriority {
        DISTANCE, HEALTH, SMART
    }

    public TargetModule() {
        super("Target", "Allows you to configure target logic.", ModuleCategory.of("Client"), "entity", "entitymanager", "enity");
        if (!this.isEnabled())
            this.toggle();

        maxIdleTicks.setShow(false);
    }

    @Override
    public void onDisable(){
        if (!this.isEnabled())
            this.toggle();
    }
}
