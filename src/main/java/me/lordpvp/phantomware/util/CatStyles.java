package me.lordpvp.phantomware.util;

import dev.cattyn.catformat.stylist.annotations.Style;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.impl.client.HudModule;

import java.awt.*;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class CatStyles {

    @Style("g")
    Color global() {
        Color gc = MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor();
        return new Color(gc.getRed(), gc.getGreen(), gc.getBlue(), 255);
    }

    @Style("s")
    Color secondary() {
        Color gs = MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledSecondColor();
        return new Color(gs.getRed(), gs.getGreen(), gs.getBlue(), 255);
    }

    @Style("namiRed")
    final Color namiRed() {
        return new Color(180, 0, 0);
    }

    @Style("namiDarkRed")
    final Color namiDarkRed() {
        return new Color(110, 0, 0);
    }

    @Style("bg")
    Color bounceGlobal() {
        Color gc = MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor();
        return MODULE_MANAGER.getStorage().getByClass(HudModule.class).getPulsingColor(new Color(gc.getRed(), gc.getGreen(), gc.getBlue(), 255));
    }

    @Style("bw")
    Color bounceWhite() {
        return MODULE_MANAGER.getStorage().getByClass(HudModule.class).getPulsingColor(new Color(255, 255,255));
    }

    @Style("bgr")
    Color bounceGray() {
        return MODULE_MANAGER.getStorage().getByClass(HudModule.class).getPulsingColor(new Color(77,77,77));
    }
}
