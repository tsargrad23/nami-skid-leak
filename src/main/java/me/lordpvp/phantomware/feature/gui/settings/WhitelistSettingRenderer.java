package me.kiriyaga.nami.feature.gui.settings;

import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class WhitelistSettingRenderer implements SettingRenderer<WhitelistSetting> {
    private final BoolSettingRenderer boolRenderer = new BoolSettingRenderer();
    private boolean showWhitelist = false;

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, WhitelistSetting setting, int x, int y, int mouseX, int mouseY) {
        boolRenderer.render(context, textRenderer, setting, x, y, mouseX, mouseY);

        //TODO: item identifier list extension
    }

    @Override
    public boolean mouseClicked(WhitelistSetting setting, double mouseX, double mouseY, int button) {
        return boolRenderer.mouseClicked(setting, mouseX, mouseY, button);
    }

    @Override
    public void mouseDragged(WhitelistSetting setting, double mouseX) {
    }

    private ColorModule getColorModule() {
        return me.kiriyaga.nami.Nami.MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
    }

    private Color brighten(Color color, float factor) {
        int r = Math.min((int)(color.getRed() * (1 + factor)), 255);
        int g = Math.min((int)(color.getGreen() * (1 + factor)), 255);
        int b = Math.min((int)(color.getBlue() * (1 + factor)), 255);
        return new Color(r, g, b, color.getAlpha());
    }

    private int toRGBA(Color color) {
        return (color.getAlpha() << 24) |
                (color.getRed() << 16) |
                (color.getGreen() << 8) |
                (color.getBlue());
    }
}
