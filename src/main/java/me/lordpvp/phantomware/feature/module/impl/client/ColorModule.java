package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render2DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.ColorSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;

import java.awt.*;

@RegisterModule
public class ColorModule extends Module {

    public final ColorSetting globalColor = addSetting(new ColorSetting("Global", new Color(22, 22, 230, 170), true));

    public final BoolSetting rainbowEnabled = addSetting(new BoolSetting("Rainbow", false));
    public final DoubleSetting rainbowSpeed = addSetting(new DoubleSetting("Speed", 0.4, 0.01, 5.0));

    private int phase = 0;

    public ColorModule() {
        super("Color", "Customizes color scheme.", ModuleCategory.of("Client"), "colr", "c", "colors", "clitor");
        if (!this.isEnabled())
            this.toggle();
        rainbowSpeed.setShowCondition(() -> rainbowEnabled.get());
    }

    @Override
    public void onDisable() {
        if (!this.isEnabled())
            this.toggle();
    }

    private int getAlpha255() {
        return 130;
    }

    public Color applySaturation(Color base, double saturationFactor) {
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        float saturation = (float) (hsb[1] * saturationFactor);
        saturation = Math.max(0f, Math.min(1f, saturation));
        return Color.getHSBColor(hsb[0], saturation, hsb[2]);
    }

    public Color applyDarkness(Color base, double darknessFactor) {
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);

        float brightness = hsb[2] * (float)(1.0 - darknessFactor);

        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], Math.max(0f, Math.min(1f, brightness)));

        return new Color((rgb >> 16) & 0xFF,
                (rgb >> 8) & 0xFF,
                rgb & 0xFF,
                base.getAlpha());
    }

    public Color getStyledColor(Color base, double saturation, double darkness) {
        Color adjusted = applyDarkness(applySaturation(base, saturation), darkness);
        return new Color(adjusted.getRed(), adjusted.getGreen(), adjusted.getBlue(), getAlpha255());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onRender(Render2DEvent ev){
        updateGlobalColor();
    }

    public void updateGlobalColor() {
        if (!rainbowEnabled.get()) return;

        Color current = globalColor.get();
        int r = current.getRed();
        int g = current.getGreen();
        int b = current.getBlue();
        int a = getAlpha255();

        int step = (int) Math.max(1, rainbowSpeed.get() * 4);

        switch (phase) {
            case 0: g += step; if (g >= 255) { g = 255; phase = 1; } break;
            case 1: r -= step; if (r <= 0)   { r = 0;   phase = 2; } break;
            case 2: b += step; if (b >= 255) { b = 255; phase = 3; } break;
            case 3: g -= step; if (g <= 0)   { g = 0;   phase = 4; } break;
            case 4: r += step; if (r >= 255) { r = 255; phase = 5; } break;
            case 5: b -= step; if (b <= 0)   { b = 0;   phase = 0; } break;
        }

        r = clamp(r); g = clamp(g); b = clamp(b);
        globalColor.set(new Color(r, g, b, a));
    }

    private int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    public Color getEffectiveGlobalColor() {
        return globalColor.get();
    }

    public Color getStyledGlobalColor() {
        return getStyledColor(getEffectiveGlobalColor(), 1.00, 0.00);
    }

    public Color getStyledSecondColor() {
        return applyDarkness(getStyledGlobalColor(), 0.35);
    }
}
