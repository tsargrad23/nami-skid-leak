package me.kiriyaga.nami.feature.gui.components;

import me.kiriyaga.nami.feature.gui.screen.ClickGuiScreen;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.gui.base.PanelRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.toRGBA;

public class NavigatePanel {
    private static final int HEIGHT = 14;
    private static final int PADDING = 6;

    private final PanelRenderer renderer = new PanelRenderer();
    private final Map<String, Screen> screens = new LinkedHashMap<>();

    private final ColorModule colorModule;
    private final ClickGuiModule clickGuiModule;
    private String activeKey;

    public NavigatePanel() {
        addScreen("ClickGui", CLICK_GUI);
        addScreen("HudEditor", HUD_EDITOR);
        addScreen("Friends", FRIEND);
        this.colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        this.clickGuiModule = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
        resetActive();
    }

    public void addScreen(String name, Screen screen) {
        screens.put(name, screen);
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY) {
        int totalWidth = calcWidth();

        renderer.renderPanel(context, x, y, totalWidth, HEIGHT, 0, false);

        int offsetX = x + PADDING;
        for (Map.Entry<String, Screen> entry : screens.entrySet()) {
            String name = entry.getKey();
            boolean active = name.equals(activeKey);

            Color primary = colorModule.getStyledGlobalColor();
            Color textOff = new Color(155, 155, 155, 255);
            Color textCol = active ? clickGuiModule.moduleFill.get() ? new Color(255, 255, 255, 255) : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255) : textOff;

            int textWidth = FONT_MANAGER.getWidth(name);
            FONT_MANAGER.drawText(context, name, offsetX, (y + (HEIGHT - FONT_MANAGER.getHeight()) / 2) + 1, CLICK_GUI.applyFade(toRGBA(textCol)), true);

            offsetX += textWidth + PADDING * 2;
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int x, int y, TextRenderer textRenderer) {
        int offsetX = x + PADDING;

        for (Map.Entry<String, Screen> entry : screens.entrySet()) {
            String name = entry.getKey();
            Screen screen = entry.getValue();
            int textWidth = FONT_MANAGER.getWidth(name);

            int startX = offsetX;
            int endX = offsetX + textWidth + PADDING * 2;

            if (mouseX >= startX && mouseX <= endX && mouseY >= y && mouseY <= y + HEIGHT) {
                if (!name.equals(activeKey)) {
                    activeKey = name;
                    if (screen instanceof ClickGuiScreen screen1)
                        screen1.setPreviousScreen(MC.currentScreen);

                    MC.setScreen(screen);
                }
                return;
            }
            offsetX = endX;
        }
    }

    public int calcWidth() {
        int width = PADDING;
        for (String key : screens.keySet()) {
            width += FONT_MANAGER.getWidth(key) + PADDING * 2;
        }
        return width;
    }

    public void resetActive() {
        if (screens.isEmpty()) return;
        activeKey = screens.keySet().iterator().next();
    }
}
