package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class WatermarkModule extends HudElementModule {

    public WatermarkModule() {
        super("Watermark", "Displays client watermark.", 0, 0, 50, 9);
    }

    @Override
    public Text getDisplayText() {
        String watermarkStr = DISPLAY_NAME + " " + VERSION;
        if (watermarkStr.isEmpty()) {
            return CAT_FORMAT.format("{bg}NaN");
        }

        width = FONT_MANAGER.getWidth(watermarkStr);
        height = FONT_MANAGER.getHeight();

        return CAT_FORMAT.format("{bg}" + DISPLAY_NAME +" "+ VERSION);
    }
}
