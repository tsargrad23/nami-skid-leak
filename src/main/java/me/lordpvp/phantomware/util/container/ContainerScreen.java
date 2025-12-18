package me.lordpvp.phantomware.util.container;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

import static me.kiriyaga.nami.Nami.MC;
import static net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED;
 // TODO: unblock actions, and change the slot count, so people can see shulkers and manage inventory
public class ContainerScreen extends ShulkerBoxScreen {
    private static final Identifier TEXTURE = Identifier.of("textures/gui/container/shulker_box.png");
    private final ItemStack[] contents;
    private final ItemStack containerStack;
    private static final Deque<Screen> screenStack = new ArrayDeque<>();

    public ContainerScreen(ItemStack containerStack, ItemStack[] contents) {
        super(new ShulkerBoxScreenHandler(0, MC.player.getInventory(), new SimpleInventory(contents)),
                MC.player.getInventory(),
                Text.translatable(containerStack.getItemName().getString()));
        this.containerStack = containerStack;
        this.contents = contents;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, backgroundWidth, backgroundHeight, 256, 256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    public static void open(ItemStack stack, ItemStack[] contents) {
        Screen current = MC.currentScreen;
        if (current != null && !(current instanceof ContainerScreen)) {
            screenStack.push(current);
        }

        MC.setScreen(new ContainerScreen(stack, contents));
    }

    @Override
    public void close() {
        if (!screenStack.isEmpty()) {
            MC.setScreen(screenStack.pop());
        } else
            super.close();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 || button == 0)
             return false;

        super.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        KeyBinding keyBindEscape = MC.options.inventoryKey;
        int escKey = keyBindEscape.getDefaultKey().getCode();

        if (keyCode == escKey || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        return true;
    }
}
