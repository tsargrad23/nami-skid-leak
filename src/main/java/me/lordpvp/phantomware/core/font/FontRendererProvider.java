package me.kiriyaga.nami.core.font;

import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import net.minecraft.client.font.TextRenderer;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class FontRendererProvider {

    private final FontLoader fontLoader;
    private TextRenderer cachedRenderer;
    private int cachedSize = -1;
    private int cachedOversample = -1;

    public FontRendererProvider(FontLoader fontLoader) {
        this.fontLoader = fontLoader;
    }

    public TextRenderer getRenderer() {
        FontModule fontModule = MODULE_MANAGER.getStorage().getByClass(FontModule.class);

        if (!fontModule.isEnabled()) {
            return MC.textRenderer;
        }

        int newSize = fontModule.glyphSize.get();
        int newOversample = fontModule.oversample.get();

        if (cachedRenderer == null || cachedSize != newSize || cachedOversample != newOversample) {
            fontLoader.init();

            if (fontLoader.getStorage() != null) {
                cachedRenderer = new TextRenderer(id -> fontLoader.getStorage(), true);
            } else {
                cachedRenderer = MC.textRenderer;
            }

            cachedSize = newSize;
            cachedOversample = newOversample;
        }

        return cachedRenderer;
    }
}
