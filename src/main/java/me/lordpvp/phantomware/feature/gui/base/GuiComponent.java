package me.kiriyaga.nami.feature.gui.base;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;

public interface GuiComponent {
    void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, float delta);
    boolean mouseClicked(double mouseX, double mouseY, int button);
}