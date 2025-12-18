package me.kiriyaga.nami.feature.gui.components;

import me.kiriyaga.nami.feature.gui.base.PanelRenderer;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.util.render.ScissorUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.List;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;
import static me.kiriyaga.nami.feature.gui.components.ModulePanel.MODULE_SPACING;

public class CategoryPanel {
    public static final int WIDTH = 100;
    public static final int HEADER_HEIGHT = 12;
    private static final int PADDING = 5;
    public static final int BORDER_WIDTH = 1;
    public static final int BOTTOM_MARGIN = 1;

    private final ModuleCategory moduleCategory;
    private final Set<Module> expandedModules;
    private final PanelRenderer renderer = new PanelRenderer();

    private double scrollOffset = 0;
    private double targetScrollOffset = 0;

    public CategoryPanel(ModuleCategory moduleCategory, Set<Module> expandedModules) {
        this.moduleCategory = moduleCategory;
        this.expandedModules = expandedModules;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int mouseX, int mouseY, int screenHeight) {
        List<Module> modules = MODULE_MANAGER.getStorage().getByCategory(moduleCategory);

        int contentTotalHeight = modules.size() * (ModulePanel.HEIGHT + MODULE_SPACING);
        int basePanelHeight = HEADER_HEIGHT + BOTTOM_MARGIN + MODULE_SPACING + contentTotalHeight + MODULE_SPACING;

        renderer.renderPanel(context, x, y, WIDTH, basePanelHeight, HEADER_HEIGHT);
        renderer.renderHeaderText(context, textRenderer, moduleCategory.getName(), x, y, HEADER_HEIGHT, PADDING);

        int innerShade = CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB());
        context.fill(x + 1, y + HEADER_HEIGHT + 1, x + 2, y + basePanelHeight - 1, innerShade);
        context.fill(x + WIDTH - 2, y + HEADER_HEIGHT + 1, x + WIDTH - 1, y + basePanelHeight - 1, innerShade);
        context.fill(x + 2, y + HEADER_HEIGHT + 1, x + WIDTH - 2, y + HEADER_HEIGHT + 2,
                CLICK_GUI.applyFade(new Color(20, 20, 20, 122).getRGB()));

        int contentY = y + HEADER_HEIGHT + MODULE_SPACING + BOTTOM_MARGIN;

        int visibleHeight = Math.min(basePanelHeight - HEADER_HEIGHT - MODULE_SPACING - BOTTOM_MARGIN,
                screenHeight - contentY - 10);

        boolean b = expandedModules.stream().anyMatch(module -> module.getCategory() == moduleCategory);

        if (b) {
            visibleHeight -= 1;
            if (visibleHeight < 0) visibleHeight = 0;
        }

        int scrollableHeight = 0;
        for (Module module : modules) {
            scrollableHeight += ModulePanel.HEIGHT + MODULE_SPACING;
            if (expandedModules.contains(module)) {
                scrollableHeight += SettingPanel.getSettingsHeight(module);
            }
        }
        scrollOffset += (targetScrollOffset - scrollOffset) * 0.1;
        double maxScroll = Math.max(0, scrollableHeight - visibleHeight);
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;



        ScissorUtil.enable(context, x, contentY, x + WIDTH, contentY + visibleHeight);

        int moduleY = contentY - (int) scrollOffset;
        for (Module module : modules) {
            ModulePanel modulePanel = new ModulePanel(module, expandedModules);
            modulePanel.render(context, textRenderer, x + BORDER_WIDTH + SettingPanel.INNER_PADDING, moduleY, mouseX, mouseY);
            moduleY += ModulePanel.HEIGHT + MODULE_SPACING;

            if (expandedModules.contains(module)) {
                moduleY += SettingPanel.renderSettings(context, textRenderer, module,
                        x + BORDER_WIDTH + SettingPanel.INNER_PADDING, moduleY, mouseX, mouseY);
            }
        }

        ScissorUtil.disable(context);
    }
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta, int x, int y, int screenHeight) {
        List<Module> modules = MODULE_MANAGER.getStorage().getByCategory(moduleCategory);

        int contentY = y + HEADER_HEIGHT + MODULE_SPACING + BOTTOM_MARGIN;

        int visibleHeight = Math.min(modules.size() * (ModulePanel.HEIGHT + MODULE_SPACING),
                screenHeight - contentY - 10);

        int scrollableHeight = 0;
        for (Module module : modules) {
            scrollableHeight += ModulePanel.HEIGHT + MODULE_SPACING;
            if (expandedModules.contains(module)) {
                scrollableHeight += SettingPanel.getSettingsHeight(module);
            }
        }
        if (mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= contentY && mouseY <= contentY + visibleHeight) {

            targetScrollOffset -= scrollDelta * 45;

            double maxScroll = Math.max(0, scrollableHeight - visibleHeight);
            if (targetScrollOffset < 0) targetScrollOffset = 0;
            if (targetScrollOffset > maxScroll) targetScrollOffset = maxScroll;

            return true;
        } else {
        }

        return false;
    }


    public static boolean isHeaderHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
    }

    public double getScrollOffset() {
        return scrollOffset;
    }
}
