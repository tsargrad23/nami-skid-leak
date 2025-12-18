package me.kiriyaga.nami.feature.gui.screen;

import me.kiriyaga.nami.feature.gui.components.CategoryPanel;
import me.kiriyaga.nami.feature.gui.components.ModulePanel;
import me.kiriyaga.nami.feature.gui.components.NavigatePanel;
import me.kiriyaga.nami.feature.gui.components.SettingPanel;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.util.ChatAnimationHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.compress.archivers.sevenz.CLI;

import java.awt.Point;
import java.util.*;

import static me.kiriyaga.nami.Nami.*;

public class HudEditorScreen extends Screen {

    private final Set<Module> expandedModules = new HashSet<>();
    private final Map<ModuleCategory, Point> categoryPositions = new HashMap<>();
    private final Map<ModuleCategory, CategoryPanel> categoryPanels = new HashMap<>();

    private boolean draggingCategory = false;
    private ModuleCategory draggedModuleCategory = null;
    private int dragStartX, dragStartY;
    private int initialCategoryX, initialCategoryY;

    private HudElementModule draggingElement = null;
    private int dragOffsetX, dragOffsetY;

    public HudEditorScreen() {
        super(Text.literal("NamiHudEditor"));
        initPanels();
    }

    private ClickGuiModule getClickGuiModule() {
        return MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
    }

    private void initPanels() {
        ModuleCategory hudCategory = ModuleCategory.of("HUD");
        Point pos = new Point(20, 20);
        categoryPositions.put(hudCategory, pos);

        if (!categoryPanels.containsKey(hudCategory)) {
            categoryPanels.put(hudCategory, new CategoryPanel(hudCategory, expandedModules));
        }
    }

    @Override
    public void renderBackground(DrawContext context, int i, int j, float f) {
        ClickGuiModule clickGui = getClickGuiModule();
        if (MC.world != null && clickGui != null && clickGui.blur.get()) {
            this.applyBlur(context);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int scaledMouseX = (int) (mouseX / CLICK_GUI.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI.scale);

        ClickGuiModule hudEditorModule = getClickGuiModule();
        if (hudEditorModule != null && hudEditorModule.background.get()) {
            int alpha = (hudEditorModule.backgroundAlpha.get() & 0xFF) << 24;
            int color = alpha | 0x101010;
            context.fill(0, 0, this.width, this.height, color);
        }

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(CLICK_GUI.scale, CLICK_GUI.scale);

        int scaledWidth = (int) (this.width / CLICK_GUI.scale);
        int panelWidth = NAVIGATE_PANEL.calcWidth();
        int navigateX = (scaledWidth - panelWidth) / 2;
        NAVIGATE_PANEL.render(context, this.textRenderer, navigateX, 1, mouseX, mouseY);

        ModuleCategory hudCategory = ModuleCategory.of("HUD");
        Point pos = categoryPositions.get(hudCategory);
        CategoryPanel hudPanel = categoryPanels.get(hudCategory);

        if (pos != null && hudPanel != null) {
            hudPanel.render(context, this.textRenderer, pos.x, pos.y, scaledMouseX, scaledMouseY, this.height);
        }

        if (hudPanel != null && hudEditorModule != null && hudEditorModule.descriptions.get() && pos != null) {
            double scrollOffset = hudPanel.getScrollOffset();
            List<Module> modules = MODULE_MANAGER.getStorage().getByCategory(hudCategory);

            int curY = pos.y + CategoryPanel.HEADER_HEIGHT + ModulePanel.MODULE_SPACING + CategoryPanel.BOTTOM_MARGIN
                    - (int) scrollOffset;

            for (Module module : modules) {
                int modX = pos.x + CategoryPanel.BORDER_WIDTH + SettingPanel.INNER_PADDING;

                if (ModulePanel.isHovered(scaledMouseX, scaledMouseY, modX, curY)) {
                    String description = module.getDescription();
                    if (description != null && !description.isEmpty()) {
                        int descX = scaledMouseX + 5;
                        int descY = scaledMouseY;
                        int textWidth = FONT_MANAGER.getWidth(Text.of(description));
                        int textHeight = 8;

                        context.fill(descX - 2, descY - 2, descX + textWidth + 2, descY + textHeight + 2, 0x7F000000);
                        FONT_MANAGER.drawText(context, Text.of(description), descX, descY, true);
                    }
                    context.getMatrices().popMatrix();

                    renderHudElements(context, mouseX, mouseY);

                    super.render(context, mouseX, mouseY, delta);
                    return;
                }

                curY += ModulePanel.HEIGHT + ModulePanel.MODULE_SPACING;
                if (expandedModules.contains(module)) {
                    curY += SettingPanel.getSettingsHeight(module);
                }
            }
        }

        context.getMatrices().popMatrix();

        renderHudElements(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderHudElements(DrawContext context, int mouseX, int mouseY) {
        int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();
        int screenHeight = MC.getWindow().getScaledHeight();
        int chatZoneTop = screenHeight - (screenHeight / 8);

        for (Module module : MODULE_MANAGER.getStorage().getByCategory(ModuleCategory.of("HUD"))) {
            if (module instanceof HudElementModule hud && hud.isEnabled()) {
                int y = hud.getRenderY();
                int renderY = (y + hud.height >= chatZoneTop) ? y - chatAnimationOffset : y;
                int baseX = hud.getRenderX();

                boolean hovered = mouseX >= baseX && mouseX <= baseX + hud.width &&
                        mouseY >= renderY && mouseY <= renderY + hud.height;

                if (hovered) {
                    context.fill(baseX - 1, renderY - 1, baseX + hud.width + 1, renderY + hud.height + 1, 0x50FFFFFF);
                }

                for (HudElementModule.TextElement element : new ArrayList<>(hud.getTextElements())) {
                    int drawX = hud.getRenderXForElement(element);
                    int drawY = renderY + element.offsetY();
                    FONT_MANAGER.drawText(context, element.text(), drawX, drawY, true);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int scaledMouseX = (int) (mouseX / CLICK_GUI.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI.scale);

        int navX = (int) ((this.width / CLICK_GUI.scale - NAVIGATE_PANEL.calcWidth()) / 2);
        NAVIGATE_PANEL.mouseClicked(scaledMouseX, scaledMouseY, navX, 1, this.textRenderer);

        ModuleCategory hudCategory = ModuleCategory.of("HUD");
        Point pos = categoryPositions.get(hudCategory);
        CategoryPanel hudPanel = categoryPanels.get(hudCategory);

        if (pos != null && hudPanel != null && CategoryPanel.isHeaderHovered(scaledMouseX, scaledMouseY, pos.x, pos.y)) {
            if (button == 0) {
                playClickSound();
                draggingCategory = true;
                draggedModuleCategory = hudCategory;
                dragStartX = scaledMouseX;
                dragStartY = scaledMouseY;
                initialCategoryX = pos.x;
                initialCategoryY = pos.y;
                return true;
            }
        }

        if (!draggingCategory && pos != null && hudPanel != null) {
            double scrollOffset = hudPanel.getScrollOffset();
            List<Module> modules = MODULE_MANAGER.getStorage().getByCategory(hudCategory);

            int curY = pos.y + CategoryPanel.HEADER_HEIGHT + ModulePanel.MODULE_SPACING + CategoryPanel.BOTTOM_MARGIN
                    - (int) scrollOffset;

            for (Module module : modules) {
                int modX = pos.x + CategoryPanel.BORDER_WIDTH + SettingPanel.INNER_PADDING;

                if (ModulePanel.isHovered(scaledMouseX, scaledMouseY, modX, curY)) {
                    if (button == 0) {
                        playClickSound();
                        module.toggle();
                    } else if (button == 1) {
                        if (expandedModules.contains(module)) expandedModules.remove(module);
                        else expandedModules.add(module);
                        playClickSound();
                    } else if (button == 2) {
                        playClickSound();
                        module.setDrawn(!module.isDrawn());
                    }
                    return true;
                }

                curY += ModulePanel.HEIGHT + ModulePanel.MODULE_SPACING;
                if (expandedModules.contains(module)) {
                    if (SettingPanel.mouseClicked(module, scaledMouseX, scaledMouseY, button, modX, curY)) return true;
                    curY += SettingPanel.getSettingsHeight(module);
                }
            }
        }

        if (button == 0) {
            int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();
            int screenHeight = MC.getWindow().getScaledHeight();
            int chatZoneTop = screenHeight - (screenHeight / 8);

            for (Module module : MODULE_MANAGER.getStorage().getByCategory(ModuleCategory.of("HUD"))) {
                if (module instanceof HudElementModule hud && hud.isEnabled()) {
                    int x = hud.getRenderX();
                    int y = hud.getRenderY();
                    int renderY = (y + hud.height >= chatZoneTop) ? y - chatAnimationOffset : y;

                    if (mouseX >= x && mouseX <= x + hud.width &&
                            mouseY >= renderY && mouseY <= renderY + hud.height) {
                        draggingElement = hud;
                        dragOffsetX = (int) mouseX - x;
                        dragOffsetY = (int) mouseY - renderY;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int scaledMouseX = (int) (mouseX / CLICK_GUI.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI.scale);

        if (draggingCategory && draggedModuleCategory != null) {
            Point pos = categoryPositions.get(draggedModuleCategory);
            if (pos != null) {
                pos.x = initialCategoryX + (scaledMouseX - dragStartX);
                pos.y = initialCategoryY + (scaledMouseY - dragStartY);
                return true;
            }
        }

        if (button == 0 && draggingElement != null) {
            dragHudElement(mouseX, mouseY);
            return true;
        }

        SettingPanel.mouseDragged(scaledMouseX, scaledMouseY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void dragHudElement(double mouseX, double mouseY) {
        int chatAnimationOffset = (int) ChatAnimationHelper.getAnimationOffset();
        int newRenderX = (int) mouseX - dragOffsetX;
        int newRenderY = (int) (mouseY - dragOffsetY + chatAnimationOffset);

        int screenWidth = MC.getWindow().getScaledWidth();
        int screenHeight = MC.getWindow().getScaledHeight();

        newRenderY = Math.max(1, Math.min(newRenderY, screenHeight - draggingElement.height - 1));

        int newX;
        switch (draggingElement.alignment.get()) {
            case LEFT -> {
                newRenderX = Math.max(1, Math.min(newRenderX, screenWidth - draggingElement.width - 1));
                newX = newRenderX;
            }
            case CENTER -> {
                newRenderX = Math.max(draggingElement.width / 2, Math.min(newRenderX, screenWidth - draggingElement.width / 2));
                newX = newRenderX + draggingElement.width / 2;
            }
            case RIGHT -> {
                newRenderX = Math.max(0, Math.min(newRenderX, screenWidth - draggingElement.width));
                newX = newRenderX + draggingElement.width;
            }
            default -> newX = Math.max(1, Math.min(newRenderX, screenWidth - draggingElement.width - 1));
        }

        boolean intersects = false;
        for (Module module : MODULE_MANAGER.getStorage().getByCategory(ModuleCategory.of("HUD"))) {
            if (module instanceof HudElementModule other && other.isEnabled() && other != draggingElement) {
                boolean overlapX = newRenderX < other.getRenderX() + other.width && newRenderX + draggingElement.width > other.getRenderX();
                boolean overlapY = newRenderY < other.getRenderY() + other.height && newRenderY + draggingElement.height > other.getRenderY();
                if (overlapX && overlapY) {
                    intersects = true;
                    break;
                }
            }
        }

        if (!intersects) {
            draggingElement.x.set(newX / (double) screenWidth);
            draggingElement.y.set(newRenderY / (double) screenHeight);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int scaledMouseX = (int) (mouseX / CLICK_GUI.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI.scale);

        for (ModuleCategory moduleCategory : ModuleCategory.getAll()) {
            if (!"hud".equalsIgnoreCase(moduleCategory.getName())) continue;

            Point pos = categoryPositions.get(moduleCategory);
            if (pos == null) continue;

            CategoryPanel panel = categoryPanels.get(moduleCategory);
            if (panel != null && panel.mouseScrolled(scaledMouseX, scaledMouseY, verticalAmount, pos.x, pos.y, this.height)) {
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingCategory = false;
        draggedModuleCategory = null;

        if (button == 0) draggingElement = null;

        SettingPanel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).getKeyBind().get() && MC.world != null) {
            MC.setScreen(null);
            return true;
        }
        if (SettingPanel.keyPressed(keyCode)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void playClickSound() {
        MC.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(
                net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0f
        ));
    }
}
