package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.ColorSetting;
import me.kiriyaga.nami.util.NamiDimension;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class SkyColorModule extends Module {

    public final BoolSetting endSky = addSetting(new BoolSetting("EndSky", false));
    public final ColorSetting skyColor = addSetting(new ColorSetting("SkyColor", new Color(60, 60, 60, 255), true));

    public DimensionEffects dimension;
    private Color lastColor = null;

    public SkyColorModule() {
        super("SkyColor", "Changes sky color/effects.", ModuleCategory.of("Render"));
    }

    @SubscribeEvent
    public void onPreTickEvent(PreTickEvent event) {
        if (MC.world == null) return;

        Color c = skyColor.get();

        if (lastColor == null || !lastColor.equals(c)) {
            Vec3d vec = new Vec3d(
                    c.getRed() / 255.0,
                    c.getGreen() / 255.0,
                    c.getBlue() / 255.0
            );
            dimension = new NamiDimension(vec, 1.0D);
            lastColor = c;
        }
    }
}