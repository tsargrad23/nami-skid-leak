package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.interactWithEntity;
import static me.kiriyaga.nami.util.RotationUtils.*;

@RegisterModule
public class AutoNametagModule extends Module {

    private final BoolSetting nametagged = addSetting(new BoolSetting("Nametagged", false));
    private final DoubleSetting range = addSetting(new DoubleSetting("Range", 5.0, 1.0, 10.0));
    private final IntSetting delay = addSetting(new IntSetting("Delay", 10, 1, 20));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));

    private int swapCooldown = 0;

    public AutoNametagModule() {
        super("AutoNametag", "Automatically renames nearby entities with nametags.", ModuleCategory.of("World"), "nametag", "autoname", "autonametag");
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (swapCooldown > 0) {
            swapCooldown--;
            return;
        }

        for (Entity entity : MC.world.getEntities()) {
            if (entity == null || entity == MC.player) continue;
            if (entity.getCustomName() != null && !nametagged.get()) continue;
            if (entity instanceof VillagerEntity || entity instanceof EnderPearlEntity || entity instanceof EnderDragonEntity) continue;

            double distance = MC.player.squaredDistanceTo(entity);
            if (distance > range.get() * range.get()) continue;

            int nameTagSlot = getNameTagSlot();
            if (nameTagSlot == -1) continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != nameTagSlot) {
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(nameTagSlot);
                swapCooldown = delay.get();
                return;
            }

            Vec3d center = getEntityCenter(entity);

            if (rotate.get()) {
                ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(
                        AutoNametagModule.class.getName(),
                        2,
                        (float) getYawToVec(MC.player, center),
                        (float) getPitchToVec(MC.player, center)
                ));

                if (!ROTATION_MANAGER.getRequestHandler().isCompleted(AutoNametagModule.class.getName())) return;
            }

            interactWithEntity(entity, center, swing.get());

            swapCooldown = delay.get();
            break;
        }
    }

    private int getNameTagSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() == Items.NAME_TAG) return i;
        }
        return -1;
    }
}