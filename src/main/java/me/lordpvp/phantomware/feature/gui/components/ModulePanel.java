package me.kiriyaga.nami.feature.gui.components;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import java.awt.*;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;

public class ModulePanel {
    public static final int WIDTH = 100 - CategoryPanel.BORDER_WIDTH * 2 - SettingPanel.INNER_PADDING * 2;
    public static final int HEIGHT = 13;
    public static final int PADDING = 3;
    public static final int MODULE_SPACING = 1;

    private final Module module;
    private final Set<Module> expandedModules;

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
    }

    public ModulePanel(Module module, Set<Module> expandedModules) {
        this.module = module;
        this.expandedModules = expandedModules;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY, x, y);
        boolean enabled = module.isEnabled();
        boolean expanded = expandedModules.contains(module);

        Color primary = getColorModule().getStyledGlobalColor();
        Color secondary = getColorModule().getStyledColor(getColorModule().getStyledSecondColor(), 0.90, 0.40);
        Color textCol = new Color(155, 155, 155, 255);
        Color textColActivated = MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).moduleFill.get()
                ? new Color(255, 255, 255, 255)
                : new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);

        Color bgColor;
        if (MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).moduleFill.get()) {
            bgColor = enabled ? primary : secondary;
        } else {
            bgColor = new Color(30, 30, 30, 0);
        }

        if (hovered) {
            bgColor = brighten(bgColor, 0.1f);
        }

        context.fill(
                x,
                y,
                x + WIDTH,
                y + HEIGHT,
                CLICK_GUI.applyFade(toRGBA(bgColor))
        );

        if (!expanded) {
            context.fill(
                    x,
                    y + HEIGHT,
                    x + WIDTH,
                    y + HEIGHT + 1,
                    CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB())
            );
        }

        int textY = y + (HEIGHT - 8) / 2;
        int baseTextX = x + PADDING + (hovered ? 1 : 0);
        FONT_MANAGER.drawText(
                context,
                module.getName(),
                baseTextX,
                textY,
                CLICK_GUI.applyFade(toRGBA(enabled ? textColActivated : textCol)),
                true
        );
    }

    public static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
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