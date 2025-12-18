package me.kiriyaga.nami.core.font;

import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontFilterType;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TrueTypeFontLoader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class FontLoader {

    private static final String FONT_NAME = "verdanapro";

    private FontStorage storage;
    private int currentSize = -1;
    private int currentOversample = -1;

    public void init() {
        FontModule fontModule = MODULE_MANAGER.getStorage().getByClass(FontModule.class);
        if (fontModule == null)
            return;

        int newSize = fontModule.glyphSize.get();
        int newOversample = fontModule.oversample.get();

        if (storage != null && currentSize == newSize && currentOversample == newOversample) {
            return;
        }

        TrueTypeFontLoader loader = new TrueTypeFontLoader(
                net.minecraft.util.Identifier.of("nami", FONT_NAME + ".ttf"),
                newSize,
                newOversample,
                TrueTypeFontLoader.Shift.NONE,
                ""
        );

        try {
            Font font = loader.build().orThrow().load(MC.getResourceManager());

            storage = new FontStorage(MC.getTextureManager(),
                    net.minecraft.util.Identifier.of("nami", FONT_NAME + "_storage"));
            storage.setFonts(List.of(new Font.FontFilterPair(font, FontFilterType.FilterMap.NO_FILTER)),
                    Collections.emptySet());

            currentSize = newSize;
            currentOversample = newOversample;
        } catch (IOException e) {
            e.printStackTrace();
            storage = null;
            currentSize = -1;
            currentOversample = -1;
        }
    }

    public FontStorage getStorage() {
        return storage;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public int getCurrentOversample() {
        return currentOversample;
    }
}
