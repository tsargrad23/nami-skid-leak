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
import net.minecraft.entity.passive.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.interactWithEntity;
import static me.kiriyaga.nami.util.RotationUtils.*;

@RegisterModule
public class AutoMountModule extends Module {

    private final DoubleSetting range = addSetting(new DoubleSetting("Range", 2, 1.0, 10.0));
    private final IntSetting delay = addSetting(new IntSetting("Delay", 10, 1, 20));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", false));

    private int actionCooldown = 0;

    public AutoMountModule() {
        super("AutoMount", "Automatically mounts nearby entities.", ModuleCategory.of("World"), "mount", "automount");
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;

        if (MC.player.hasVehicle()) return;

        if (actionCooldown > 0) {
            actionCooldown--;
            return;
        }

        for (Entity entity : MC.world.getEntities()) {
            if (entity == null || entity == MC.player || !entity.isAlive() || entity.hasPassengers()) continue;

            if (!(entity instanceof HorseEntity || entity instanceof PigEntity || entity instanceof StriderEntity ||
                    entity instanceof LlamaEntity || entity instanceof DonkeyEntity ||
                    entity instanceof BoatEntity || entity instanceof MinecartEntity)) continue;

            double distSq = MC.player.squaredDistanceTo(entity);
            if (distSq > range.get() * range.get()) continue;

            Vec3d center = getEntityCenter(entity);

            if (rotate.get()) {
                ROTATION_MANAGER.getRequestHandler().submit(
                        new RotationRequest(
                                AutoMountModule.class.getName(),
                                2,
                                (float) getYawToVec(MC.player, center),
                                (float) getPitchToVec(MC.player, center)
                        )
                );

                if (!ROTATION_MANAGER.getRequestHandler().isCompleted(AutoMountModule.class.getName())) return;
            }

            interactWithEntity(entity, center, swing.get());

            actionCooldown = delay.get();
            break;
        }
    }
}
