package me.kiriyaga.nami.feature.gui.base;

import me.kiriyaga.nami.feature.gui.base.ButtonWidget;
import me.kiriyaga.nami.feature.gui.base.PanelRenderer;
import me.kiriyaga.nami.util.render.ScissorUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
//TODO: scissors scrolling

import static me.kiriyaga.nami.Nami.*;

public class ConsolePanelRenderer {
    private final PanelRenderer panelRenderer = new PanelRenderer();
    private final List<Text> entries = new ArrayList<>();
    private double targetScrollOffset = 0;

    private final Consumer<String> onAdd;
    private final Consumer<String> onRemove;
    private final Consumer<String> onClick;

    private boolean inputFocused = false;
    private StringBuilder inputBuffer = new StringBuilder();
    private double scrollOffset = 0;
    private final List<ButtonWidget> deleteButtons = new ArrayList<>();

    private int x;
    private int y;
    private final int width;
    private final int height;
    private final int headerHeight = 20;
    private final int inputHeight = 20;

    private ButtonWidget enterButton;

    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public ConsolePanelRenderer(int x, int y, int width, int height, Consumer<String> onAdd, Consumer<String> onRemove, Consumer<String> onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onAdd = onAdd;
        this.onRemove = onRemove;
        this.onClick = onClick;

        this.enterButton = new ButtonWidget("Enter",
                x + width - 2 - 50,
                y + height - 2 - inputHeight,
                50, inputHeight, true,
                () -> {
                    String text = inputBuffer.toString().trim();
                    if (!text.isEmpty() && onAdd != null) {
                        onAdd.accept(text);
                        inputBuffer.setLength(0);
                    }
                });
    }

    public void setEntries(List<Text> items) {
        entries.clear();
        entries.addAll(items);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        int contentY = y + headerHeight + 4;
        int contentHeight = height - headerHeight - inputHeight - 8;

        if (mouseX >= x && mouseX <= x + width && mouseY >= contentY && mouseY <= contentY + contentHeight) {
            int lineHeight = FONT_MANAGER.getHeight() + 4;
            int maxVisible = contentHeight / lineHeight;
            double maxScroll = Math.max(0, entries.size() - maxVisible);

            targetScrollOffset -= scrollDelta * 1.5;
            if (targetScrollOffset < 0) targetScrollOffset = 0;
            if (targetScrollOffset > maxScroll) targetScrollOffset = maxScroll;

            return true;
        }
        return false;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        panelRenderer.renderPanel(context, x, y, width, height, headerHeight);
        panelRenderer.renderHeaderText(context, textRenderer, "Console", x, y, headerHeight, 4);

        int contentY = y + headerHeight + 4;
        int lineHeight = textRenderer.fontHeight + 4;
        int contentHeight = height - headerHeight - inputHeight - 8;
        int maxVisible = contentHeight / lineHeight;

        scrollOffset += (targetScrollOffset - scrollOffset) * 0.3;

        double maxScroll = Math.max(0, entries.size() - maxVisible);
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        int start = (int) Math.floor(scrollOffset);
        double partialOffset = scrollOffset - start;

        ScissorUtil.enable(context, x, contentY, x + width, contentY + contentHeight);

        int drawY = contentY - (int)(partialOffset * lineHeight);

        for (int i = start; i < Math.min(entries.size(), start + maxVisible + 1); i++) {
            Text entry = entries.get(i);
            FONT_MANAGER.drawText(context, entry, x + 4, drawY, 0xFFFFFFFF, false);
            drawY += lineHeight;
        }

        updateDeleteButtons(textRenderer);
        for (ButtonWidget button : deleteButtons) {
            button.render(context, textRenderer, mouseX, mouseY);
        }

        ScissorUtil.disable(context);

        int inputY = y + height - inputHeight - 2;
        int inputX = x + 2;
        int inputBg = inputFocused ? 0x80202020 : 0x80101010;
        int inputWidth = enterButton.getX() - inputX - 1;
        context.fill(inputX, inputY, inputX + inputWidth, inputY + inputHeight, inputBg);

        String inputStr = inputBuffer.toString() + (inputFocused && (System.currentTimeMillis() / 500 % 2 == 0) ? "_" : "");
        FONT_MANAGER.drawText(context, inputStr, inputX + 2, inputY + 4, 0xFFFFFFFF, true);

        enterButton.render(context, textRenderer, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mouseXInt = (int) mouseX;
        int mouseYInt = (int) mouseY;

        for (ButtonWidget widget : deleteButtons) {
            if (widget.mouseClicked(mouseXInt, mouseYInt, button)) {
                return true;
            }
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isHeaderHovered(mouseXInt, mouseYInt)) {
            dragging = true;
            dragOffsetX = mouseXInt - x;
            dragOffsetY = mouseYInt - y;
            return true;
        }

        int inputY = y + height - inputHeight;
        boolean insideInput = mouseX >= x && mouseX <= x + width - 60 && mouseY >= inputY && mouseY <= inputY + inputHeight;

        if (insideInput && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            inputFocused = true;
            return true;
        }

        if (enterButton.mouseClicked(mouseXInt, mouseYInt, button)) {
            inputFocused = false;
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !insideInput) {
            inputFocused = false;
        }

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (dragging) {
            x = (int) (mouseX - dragOffsetX);
            y = (int) (mouseY - dragOffsetY);

            enterButton.setPosition(x + width - 2 - enterButton.getWidth(), y + height - 2 - enterButton.getHeight());
            return true;
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!inputFocused) return false;

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            String text = inputBuffer.toString().trim();
            if (!text.isEmpty() && onAdd != null) {
                onAdd.accept(text);
                inputBuffer.setLength(0);
            }
            inputFocused = false;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            inputBuffer.setLength(0);
            inputFocused = false;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && inputBuffer.length() > 0) {
            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
            return true;
        }

        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!inputFocused) return false;

        if (chr >= 32 && chr != 127) {
            inputBuffer.append(chr);
            return true;
        }
        return false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    public boolean isHeaderHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight;
    }

    private void updateDeleteButtons(TextRenderer textRenderer) {
        deleteButtons.clear();

        int contentY = y + headerHeight + 4;
        int lineHeight = textRenderer.fontHeight + 4;
        int contentHeight = height - headerHeight - inputHeight - 8;
        int maxVisible = contentHeight / lineHeight;

        int start = (int) Math.floor(scrollOffset);
        double partialOffset = scrollOffset - start;
        int end = Math.min(entries.size(), start + maxVisible + 1);

        for (int i = start; i < end; i++) {
            Text entry = entries.get(i);

            int drawY = contentY + (int)((i - start - partialOffset) * lineHeight);

            int buttonWidth = 30;
            int buttonHeight = textRenderer.fontHeight;
            int buttonX = x + width - 2 - buttonWidth;
            int buttonY = drawY;

            final String friendName = entry.getString().split(" ")[0];
            deleteButtons.add(new ButtonWidget("X", buttonX, buttonY, buttonWidth, buttonHeight, true,
                    () -> {
                        if (onRemove != null) onRemove.accept(friendName);
                    }));
        }
    }
}