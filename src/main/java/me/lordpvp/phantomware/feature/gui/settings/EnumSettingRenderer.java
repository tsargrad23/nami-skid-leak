package me.kiriyaga.nami.feature.gui.settings;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;

public class EnumSettingRenderer implements SettingRenderer<EnumSetting<?>> {

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, EnumSetting<?> setting, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        Color primary = getColorModule().getStyledGlobalColor();
        Color secondary = getColorModule().getStyledSecondColor();
        Color textCol = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).moduleFill.get()
                ? new Color(255, 255, 255, 255)
                : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);
        Color bgColor = new Color(30, 30, 30, 0);

        int bgColorInt = CLICK_GUI.applyFade(toRGBA(bgColor));
        int textColorInt = CLICK_GUI.applyFade(toRGBA(textCol));

        context.fill(x, y, x + WIDTH, y + HEIGHT, bgColorInt);

        int lineOffset = 1;
        if (MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).expandedIdentifier.get())
            context.fill(
                    x,
                    y - lineOffset,
                    x + 1,
                    y + HEIGHT,
                    CLICK_GUI.applyFade(
                            textCol.getRGB()
                    )
            );

        int textX = x + PADDING + (hovered ? 1 : 0);
        int textY = y + (HEIGHT - 8) / 2;

        FONT_MANAGER.drawText(
                context,
                setting.getName(),
                textX,
                textY,
                textColorInt,
                true
        );

        String valueStr = setting.get().toString();
        FONT_MANAGER.drawText(
                context,
                valueStr,
                x + WIDTH - PADDING - FONT_MANAGER.getWidth(valueStr),
                textY,
                textColorInt,
                true
        );
    }

    @Override
    public boolean mouseClicked(EnumSetting<?> setting, double mouseX, double mouseY, int button) {
        if (button == 0) {
            setting.cycle(false);
        } else if (button == 1) {
            setting.cycle(true);
        }
        return true;
    }

    @Override
    public void mouseDragged(EnumSetting<?> setting, double mouseX) {
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
    }

    private float approach(float current, float target, float maxDelta) {
        if (current < target) {
            current += maxDelta;
            if (current > target) current = target;
        } else if (current > target) {
            current -= maxDelta;
            if (current < target) current = target;
        }
        return current;
    }
}
