package me.lordpvp.phantomware.util.container;

import me.kiriyaga.nami.feature.module.impl.client.DebugModule;
import net.fabricmc.fabric.mixin.transfer.ContainerComponentAccessor;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.ColorHelper;

import java.util.*;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class ContainerUtils {

    public static boolean hasItems(ItemStack stack) {
        ComponentMap components = stack.getComponents();
        return components.contains(DataComponentTypes.CONTAINER);
    }

    public static boolean openContainer(ItemStack stack) {
        if (!hasItems(stack)) {
            MODULE_MANAGER.getStorage().getByClass(DebugModule.class).debugPeek(Text.of("peek not a container " + stack));
            return false;
        }

        ItemStack[] contents = new ItemStack[27];
        getItemsInContainerItem(stack, contents);

        ContainerScreen.open(stack, contents);

        MODULE_MANAGER.getStorage().getByClass(DebugModule.class).debugPeek(Text.of("peek opened container preview for " + stack));
        return true;
    }

    public static void getItemsInContainerItem(ItemStack itemStack, ItemStack[] items) {
        Arrays.fill(items, ItemStack.EMPTY);

        ComponentMap components = itemStack.getComponents();

        if (components.contains(DataComponentTypes.CONTAINER)) {
            ContainerComponentAccessor container = (ContainerComponentAccessor) (Object) components.get(DataComponentTypes.CONTAINER);
            DefaultedList<ItemStack> stacks = container.fabric_getStacks();

            for (int i = 0; i < stacks.size() && i < items.length; i++) {
                items[i] = stacks.get(i);
            }

            MODULE_MANAGER.getStorage().getByClass(DebugModule.class).debugPeek(Text.of("peek got " + stacks.size() + " items from container  " + itemStack));
        }
    }

    public static int DyeColorToARGB(DyeColor color) {
        switch (color) {
            case WHITE: return ColorHelper.getArgb(255, 255, 255, 255);
            case ORANGE: return ColorHelper.getArgb(255, 216, 127, 51);
            case MAGENTA: return ColorHelper.getArgb(255, 178, 76, 216);
            case LIGHT_BLUE: return ColorHelper.getArgb(255, 102, 153, 216);
            case YELLOW: return ColorHelper.getArgb(255, 229, 229, 51);
            case LIME: return ColorHelper.getArgb(255, 127, 204, 25);
            case PINK: return ColorHelper.getArgb(255, 242, 127, 165);
            case GRAY: return ColorHelper.getArgb(255, 76, 76, 76);
            case LIGHT_GRAY: return ColorHelper.getArgb(255, 153, 153, 153);
            case CYAN: return ColorHelper.getArgb(255, 76, 127, 153);
            case PURPLE: return ColorHelper.getArgb(255, 127, 63, 178);
            case BLUE: return ColorHelper.getArgb(255, 51, 76, 178);
            case BROWN: return ColorHelper.getArgb(255, 102, 76, 51);
            case GREEN: return ColorHelper.getArgb(255, 102, 127, 51);
            case RED: return ColorHelper.getArgb(255, 153, 51, 51);
            case BLACK: return ColorHelper.getArgb(255, 25, 25, 25);
            default: return ColorHelper.getArgb(255, 128, 128, 128);
        }
    }
}
