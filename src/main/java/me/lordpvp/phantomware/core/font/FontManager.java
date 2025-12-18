package me.kiriyaga.nami.core.font;

import me.kiriyaga.nami.feature.module.impl.client.FontModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class FontManager {

    public final FontLoader fontLoader = new FontLoader();
    public final FontRendererProvider rendererProvider = new FontRendererProvider(fontLoader);
    public final FontMetrics fontMetrics = new FontMetrics(rendererProvider);

    public void init() {
        fontLoader.init();
    }

    public void drawText(DrawContext context, Text text, int x, int y, boolean shadow, int color) {
        context.drawText(rendererProvider.getRenderer(), text, x, y, color, shadow);
    }

    public void drawText(DrawContext context, String text, int x, int y, boolean shadow, int color) {
        context.drawText(rendererProvider.getRenderer(), text, x, y, color, shadow);
    }

    public void drawText(DrawContext context, Text text, int x, int y, boolean shadow) {
        context.drawText(rendererProvider.getRenderer(), text, x, y, 0xFFFFFFFF, shadow);
    }

    public void drawText(DrawContext context, String text, int x, int y, boolean shadow) {
        context.drawText(rendererProvider.getRenderer(), text, x, y, 0xFFFFFFFF, shadow);
    }

    public void drawText(DrawContext context, Text text, int x, int y, int rgb, boolean shadow) {
        context.drawText(rendererProvider.getRenderer(), text, x, y, rgb, shadow);
    }

    public void drawText(DrawContext context, String text, int x, int y, int rgb, boolean shadow) {
        context.drawText(rendererProvider.getRenderer(), text, x, y, rgb, shadow);
    }

    public int getWidth(Text text) {
        return rendererProvider.getRenderer().getWidth(text);
    }

    public int getWidth(String text) {
        return rendererProvider.getRenderer().getWidth(text);
    }

    public int getHeight() {
        return fontMetrics.getHeight();
    }
}
