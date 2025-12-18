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
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.interactWithEntity;
import static me.kiriyaga.nami.util.RotationUtils.*;

@RegisterModule
public class AutoBreedModule extends Module {

    private final DoubleSetting range = addSetting(new DoubleSetting("Range", 2, 1.0, 5.0));
    private final IntSetting delay = addSetting(new IntSetting("Delay", 10, 1, 20));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", false));

    private final Set<Integer> animalsFed = new HashSet<>();
    private int breedCooldown = 0;

    public AutoBreedModule() {
        super("AutoBreed", "Automatically breeds nearby animals.", ModuleCategory.of("World"), "autobreed");
    }

    @Override
    public void onDisable() {
        animalsFed.clear();
        breedCooldown = 0;
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        animalsFed.removeIf(id -> {
            Entity e = MC.world.getEntityById(id);
            return e == null || !e.isAlive() || MC.player.squaredDistanceTo(e) > range.get() * range.get();
        });

        if (breedCooldown > 0) {
            breedCooldown--;
            return;
        }

        for (Entity entity : ENTITY_MANAGER.getPassive()) {
            if (!(entity instanceof AnimalEntity animal)) continue;
            if (!animal.isAlive() || animal.isBaby() || animal.isInLove() || !animal.canEat()) continue;
            if (animalsFed.contains(animal.getId())) continue;

            double distance = MC.player.squaredDistanceTo(animal);
            if (distance > range.get() * range.get()) continue;

            int foodSlot = getBreedingItemSlot(animal);
            if (foodSlot == -1) continue;

            int currentSlot = MC.player.getInventory().getSelectedSlot();
            if (currentSlot != foodSlot) {
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(foodSlot);
                breedCooldown = delay.get();
                return;
            }

            Vec3d center = getEntityCenter(animal);

            if (rotate.get()) {
                ROTATION_MANAGER.getRequestHandler().submit(
                        new RotationRequest(
                                AutoBreedModule.class.getName(),
                                2,
                                (float) getYawToVec(MC.player, center),
                                (float) getPitchToVec(MC.player, center)
                        )
                );
                if (!ROTATION_MANAGER.getRequestHandler().isCompleted(AutoBreedModule.class.getName())) return;
            }

            interactWithEntity(animal, center, swing.get());

            animalsFed.add(animal.getId());
            breedCooldown = delay.get();
            break;
        }
    }

    private int getBreedingItemSlot(AnimalEntity animal) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (!stack.isEmpty() && animal.isBreedingItem(stack)) {
                return i;
            }
        }
        return -1;
    }
}