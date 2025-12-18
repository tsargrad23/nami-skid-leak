package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.KeyBindSetting;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.RotationUtils.getLookVectorFromYawPitch;

@RegisterModule
public class ClickPearlModule extends Module {

    public enum GroundAction { PEARL, WIND, EXP, NONE }

    public enum GlidingAction  { NONE, FIREWORK, WIND}

    private final EnumSetting<GroundAction> groundAction = addSetting(new EnumSetting<>("Ground", GroundAction.NONE));
    private final EnumSetting<GlidingAction> glidingAction = addSetting(new EnumSetting<>("Gliding", GlidingAction.FIREWORK));
    private final BoolSetting checkCooldown = addSetting(new BoolSetting("CheckCooldown", true));
    private final BoolSetting entityCheck = addSetting(new BoolSetting("EntityCheck", true));
    private final KeyBindSetting useKey = addSetting(new KeyBindSetting("Use", KeyBindSetting.KEY_NONE));

    public ClickPearlModule() {
        super("ClickPearl", "Uses configured item when pressing key.", ModuleCategory.of("Combat"), "clickpearl");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private void onTick(PreTickEvent ev) {
        if (!isEnabled()) return;
        if (MC.world == null || MC.player == null) return;

        boolean pressed = useKey.isPressed();

        if (pressed && !useKey.wasPressedLastTick()) {
            if (MC.player.isGliding()) {
                useGlide();
            } else {
                use();
            }
        }

        useKey.setWasPressedLastTick(pressed);
    }

    private void use() {
        Item item = switch (groundAction.get()) {
            case PEARL -> Items.ENDER_PEARL;
            case WIND -> Items.WIND_CHARGE;
            case EXP -> Items.EXPERIENCE_BOTTLE;
            case NONE -> null;
        };

        if (item == null) return;

        if (checkCooldown.get() && MC.player.getItemCooldownManager().isCoolingDown(item.getDefaultStack())) {
            return;
        }

        if (entityCheck.get() && !canCastRay()) {
            return;
        }

        useItem(item);
    }

    private void useGlide() {
        Item item = switch (glidingAction.get()) {
            case FIREWORK -> Items.FIREWORK_ROCKET;
            case WIND -> Items.WIND_CHARGE;
            case NONE -> null;
        };

        if (item == null) return;

        if (checkCooldown.get() && MC.player.getItemCooldownManager().isCoolingDown(item.getDefaultStack())) {
            return;
        }

        useItem(item);
    }

    private void useItem(Item item) {
        int hotbarSlot = getSlotInHotbar(item);

        if (hotbarSlot != -1) {
            int prevSlot = MC.player.getInventory().getSelectedSlot();
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(hotbarSlot);
            MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(prevSlot);
            return;
        }

        int invSlot = getSlotInInventory(item);
        if (invSlot != -1) {
            int selectedHotbarIndex = MC.player.getInventory().getSelectedSlot(); // 0–8
            int containerInvSlot = convertSlot(invSlot);

            INVENTORY_MANAGER.getClickHandler().swapSlot(containerInvSlot, selectedHotbarIndex);

            MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);

            INVENTORY_MANAGER.getClickHandler().swapSlot(containerInvSlot, selectedHotbarIndex);
        }
    }

    private boolean canCastRay() {
        double rayRange = 6.0;

        for (Entity entity : MC.world.getEntities()) {
            if (entity == MC.player) continue;
            if (MC.player.squaredDistanceTo(entity) > 100) continue;

            EntityHitResult hitResult = raycastTarget(MC.player, entity, rayRange,
                    ROTATION_MANAGER.getStateHandler().getServerYaw(),
                    ROTATION_MANAGER.getStateHandler().getServerPitch());

            if (hitResult != null)
                return false;
        }
        return true;
    }


    @Override
    public void onEnable() {
        useKey.setWasPressedLastTick(false);
    }

    private int getSlotInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    private int getSlotInInventory(Item item) {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    private int convertSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }

    private EntityHitResult raycastTarget(Entity player, Entity target, double reach, float yaw, float pitch) {
        Vec3d eyePos = player.getCameraPosVec(1.0f);
        Vec3d look = getLookVectorFromYawPitch(yaw, pitch);
        Vec3d reachEnd = eyePos.add(look.multiply(reach));

        Box targetBox = target.getBoundingBox();

        if (targetBox.raycast(eyePos, reachEnd).isPresent()) {
            return new EntityHitResult(target);
        }

        return null;
    }
}