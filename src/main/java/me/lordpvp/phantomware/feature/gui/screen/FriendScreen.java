package me.kiriyaga.nami.feature.gui.screen;

import me.kiriyaga.nami.feature.module.impl.client.ClickGuiModule;
import me.kiriyaga.nami.feature.gui.base.ConsolePanelRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;

import static me.kiriyaga.nami.Nami.*;
public class FriendScreen extends Screen {
    private ConsolePanelRenderer console;

    public FriendScreen() {
        super(Text.literal("NamiFriends"));
    }

    private ClickGuiModule getClickGuiModule() {
        return MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class);
    }

    @Override
    protected void init() {
        super.init();

        if (console == null) {
            int panelWidth = 300;
            int panelHeight = 200;
            int panelX = 20;
            int panelY = 20;

            console = new ConsolePanelRenderer(
                    panelX, panelY, panelWidth, panelHeight,
                    name -> {
                        if (!name.isEmpty()) {
                            FRIEND_MANAGER.addFriend(name);
                            updateEntries();
                        }
                    },
                    name -> {
                        FRIEND_MANAGER.removeFriend(name);
                        updateEntries();
                    },
                    name -> {}
            );
        }

        updateEntries();
    }

    private void updateEntries() {
        List<Text> friends = FRIEND_MANAGER.getFriends().stream()
                .map(f -> {
                    boolean online = isOnline(f);
                    return CAT_FORMAT.format(f + " [" + (online ? "{green}Online" : "{red}Offline") + "{reset}]");
                })
                .collect(Collectors.toList());
        console.setEntries(friends);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ClickGuiModule clickGui = getClickGuiModule();
        updateEntries();
        if (clickGui != null && clickGui.background.get()) {
            int alpha = (clickGui.backgroundAlpha.get() & 0xFF) << 24;
            int color = alpha | 0x101010;
            context.fill(0, 0, this.width, this.height, color);
        }

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(CLICK_GUI.scale, CLICK_GUI.scale);

        int scaledMouseX = (int) (mouseX / CLICK_GUI.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI.scale);
        int scaledWidth = (int) ( this.width / CLICK_GUI.scale);
        int scaledHeight = (int) (this.height / CLICK_GUI.scale);

        int panelWidth = NAVIGATE_PANEL.calcWidth();
        int navX = (scaledWidth - panelWidth) / 2;
        int navY = 1;
        console.render(context, this.textRenderer, scaledMouseX, scaledMouseY);
        NAVIGATE_PANEL.render(context, this.textRenderer, navX, navY, scaledMouseX, scaledMouseY);

        context.getMatrices().popMatrix();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        int scaledMouseX = (int) (mouseX / CLICK_GUI.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI.scale);

        int navX = (int) ((this.width / CLICK_GUI.scale - NAVIGATE_PANEL.calcWidth()) / 2);
        int navY = 1;
        NAVIGATE_PANEL.mouseClicked(scaledMouseX, scaledMouseY, navX, navY, this.textRenderer);

        if (console.mouseClicked(scaledMouseX, scaledMouseY, button)) return true;
        return super.mouseClicked(scaledMouseX, scaledMouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int scaledMouseX = (int) (mouseX / CLICK_GUI.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI.scale);

        if (console.mouseDragged(scaledMouseX, scaledMouseY, deltaX, deltaY)) return true;
        return super.mouseDragged(scaledMouseX, scaledMouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int scaledMouseX = (int) (mouseX / CLICK_GUI.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI.scale);

        if (console.mouseScrolled(scaledMouseX, scaledMouseY, verticalAmount)) return true;

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int scaledMouseX = (int) (mouseX / CLICK_GUI.scale);
        int scaledMouseY = (int) (mouseY / CLICK_GUI.scale);

        console.mouseReleased(scaledMouseX, mouseY, button);
        return super.mouseReleased(scaledMouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (console.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (console.charTyped(chr, modifiers)) return true;
        return super.charTyped(chr, modifiers);
    }

    private boolean isOnline(String name) {
        if (MC.world == null) return false;
        for (AbstractClientPlayerEntity player : MC.world.getPlayers()) {
            if (player.getGameProfile().getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int i, int j, float f) {
        if (MC.world != null && MODULE_MANAGER.getStorage().getByClass(ClickGuiModule.class).blur.get()) {
            this.applyBlur(context);
        }
    }
}