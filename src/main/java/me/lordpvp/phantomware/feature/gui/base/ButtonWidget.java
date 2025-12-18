package me.kiriyaga.nami.feature.gui.base;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.toRGBA;

public class ButtonWidget {
    private final String label;
    private final Runnable onClick;
    private final PanelRenderer panelRenderer = new PanelRenderer();

    private int x, y, width, height;
    private boolean active;

    public ButtonWidget(String label, int x, int y, int width, int height, boolean active, Runnable onClick) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.active = active;
        this.onClick = onClick;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        int bgColor = 0xAA000000;
        panelRenderer.renderPanel(context, x, y, width, height, 0, false);


        ClickGuiModule clickGuiModule = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
        Color primary = MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor();
        Color textOff = new Color(155, 155, 155, 255);
        Color textCol = active ? (clickGuiModule.moduleFill.get() ? new Color(255, 255, 255, 255) : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255)) : textOff;

        int textWidth = textRenderer.getWidth(label);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - textRenderer.fontHeight) / 2 + 1;

        FONT_MANAGER.drawText(context, Text.of(label), textX, textY, true, CLICK_GUI.applyFade(toRGBA(textCol)));
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            onClick.run();
            return true;
        }
        return false;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}