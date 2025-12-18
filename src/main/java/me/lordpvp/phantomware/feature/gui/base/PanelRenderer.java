package me.kiriyaga.nami.feature.gui.base;


import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;


import java.awt.*;


import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.toRGBA;


public class PanelRenderer {
    private final ColorModule colorModule;
    private final ClickGuiModule clickGuiModule;
    public PanelRenderer() {
        this.colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        this.clickGuiModule = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
    }

    public void renderPanel(DrawContext context, int x, int y, int width, int height, int headerHeight, boolean renderHeader) {
        Color primary = colorModule.getStyledGlobalColor();


        int bgColor = CLICK_GUI.applyFade(toRGBA(new Color(30, 30, 30, clickGuiModule.guiAlpha.get())));
        context.fill(x, y, x + width, y + height, bgColor);


        int lineColor;
        if (clickGuiModule.lines.get()) {
            lineColor = CLICK_GUI.applyFade(primary.getRGB());
        } else {
            lineColor = CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB());
        }

        if (!renderHeader) {
            context.fill(x, y, x + width, y + 1, lineColor);
        }

        if (renderHeader && headerHeight > 0) {
            context.fill(x, y + headerHeight, x + width, y + headerHeight + 1, lineColor);
        }

        context.fill(x, y + height - 1, x + width, y + height, lineColor);

        int topOffset = (renderHeader && headerHeight > 0) ? headerHeight + 1 : 1;
        context.fill(x, y + topOffset, x + 1, y + height - 1, lineColor);
        context.fill(x + width - 1, y + topOffset, x + width, y + height - 1, lineColor);

        if (renderHeader && headerHeight > 0) {
            context.fill(x, y, x + width, y + headerHeight, CLICK_GUI.applyFade(toRGBA(primary)));
        }
    }

    public void renderHeaderText(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int headerHeight, int padding) {
        Color primary = colorModule.getStyledGlobalColor();
        Color textCol = clickGuiModule.moduleFill.get() ? new Color(255, 255, 255, 255) : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);

        int textY = y + (headerHeight - textRenderer.fontHeight) / 2;
        FONT_MANAGER.drawText(context, text, x + padding, textY + 1, CLICK_GUI.applyFade(toRGBA(textCol)), true);
    }


    public void renderPanel(DrawContext context, int x, int y, int width, int height, int headerHeight) {
        renderPanel(context, x, y, width, height, headerHeight, true);
    }
}