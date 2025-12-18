package me.kiriyaga.nami.feature.module.impl.client.cape;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;

@RegisterModule
public class CapeModule extends Module {
    public final EnumSetting<CapeType> cape = addSetting(new EnumSetting<>("Texture", CapeType.NAMI));

    public CapeModule() {
        super("Cape", "Defines cape renderer logic.", ModuleCategory.of("Client"), "customcape");
        cape.setShow(false);//haha
    }

    public CapeType getSelectedCape() {
        return cape.get();
    }
}
