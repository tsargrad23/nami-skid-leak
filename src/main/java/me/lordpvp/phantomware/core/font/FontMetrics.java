package me.kiriyaga.nami.core.font;

import me.kiriyaga.nami.feature.module.impl.client.FontModule;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class FontMetrics {

    private final FontRendererProvider rendererProvider;

    public FontMetrics(FontRendererProvider rendererProvider) {
        this.rendererProvider = rendererProvider;
    }

    public int getHeight() {
        FontModule fontModule = MODULE_MANAGER.getStorage().getByClass(FontModule.class);

        if (!fontModule.isEnabled()) {
            return rendererProvider.getRenderer().fontHeight;
        }

        int baseSize = 9;
        int size = fontModule.glyphSize.get();

        return (int) (baseSize * (size / 9.0));
    }
}
