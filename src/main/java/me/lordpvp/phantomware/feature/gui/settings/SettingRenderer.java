package me.kiriyaga.nami.feature.gui.settings;

import me.kiriyaga.nami.feature.gui.components.CategoryPanel;
import me.kiriyaga.nami.feature.gui.components.SettingPanel;
import me.kiriyaga.nami.feature.setting.Setting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public interface SettingRenderer<T extends Setting<?>> {
    int HEIGHT = 13;
    int PADDING = 3;
    int WIDTH = 100 - CategoryPanel.BORDER_WIDTH * 2 - SettingPanel.INNER_PADDING * 2;
    int SLIDER_HEIGHT = 1;
    int MODULE_SPACING = 1;

    void render(DrawContext context, TextRenderer textRenderer, T setting, int x, int y, int mouseX, int mouseY);
    boolean mouseClicked(T setting, double mouseX, double mouseY, int button);
    void mouseDragged(T setting, double mouseX);
    default boolean mouseReleased(T setting, double mouseX, double mouseY, int button) {
        return false;
    }

    default int getHeight(T setting) {
        return HEIGHT;
    }
}
