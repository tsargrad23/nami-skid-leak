package me.kiriyaga.nami.feature.gui.settings;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.KeyBindSetting;
import me.kiriyaga.nami.util.KeyUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.feature.gui.base.GuiConstants.*;

public class KeyBindSettingRenderer implements SettingRenderer<KeyBindSetting> {
    private static KeyBindSetting waitingForKeyBind = null;

    @Override
    public void render(DrawContext context, TextRenderer textRenderer, KeyBindSetting setting, int x, int y, int mouseX, int mouseY) {
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
        if (MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).expandedIdentifier.get()) {
            context.fill(
                    x,
                    y - lineOffset,
                    x + 1,
                    y + HEIGHT,
                    CLICK_GUI.applyFade(textCol.getRGB())
            );
        }

        int textX = x + PADDING + (hovered ? 1 : 0);
        int textY = y + (HEIGHT - 8) / 2;

        String nameStr = hovered
                ? (setting.isHoldMode() ? "Hold" : "Toggle")
                : setting.getName();

        FONT_MANAGER.drawText(
                context,
                nameStr,
                textX,
                textY,
                textColorInt,
                true
        );

        String valueStr;
        if (waitingForKeyBind == setting) {
            valueStr = "Listening...";
        } else {
            String keyName = KeyUtils.getKeyName(setting.get());
            //valueStr = (setting.isHoldMode() ? "hold: " : "toggle: ") + keyName;
            //setting.setName(setting.isHoldMode() ? "hold" : "toggle"); // yes unfortunatelly
            valueStr = keyName;
        }

        String renderStr = valueStr;
        int textWidth = FONT_MANAGER.getWidth(renderStr);
        int valueX = x + WIDTH - PADDING - textWidth;

        FONT_MANAGER.drawText(
                context,
                renderStr,
                valueX,
                textY,
                textColorInt,
                true
        );
    }

    @Override
    public boolean mouseClicked(KeyBindSetting setting, double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY, (int) mouseX, (int) mouseY)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                setting.setHoldMode(!setting.isHoldMode());
                return true;
            }
        }

        if (waitingForKeyBind == null) {
            waitingForKeyBind = setting;
        } else if (waitingForKeyBind == setting) {
            waitingForKeyBind.set(button);
            waitingForKeyBind = null;
        }
        return true;
    }

    @Override
    public void mouseDragged(KeyBindSetting setting, double mouseX) {
    }

    public static boolean keyPressed(int keyCode) {
        if (waitingForKeyBind != null) {
            if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForKeyBind.set(-1);
            } else {
                waitingForKeyBind.set(keyCode);
            }
            waitingForKeyBind = null;
            return true;
        }
        return false;
    }

    private static boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    private ColorModule getColorModule() {
        return MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
    }
}