package me.kiriyaga.nami.feature.module.impl.miscellaneous;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.RotationModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.RotationUtils.*;

@RegisterModule
public class EndermanAgro extends Module {


    public EndermanAgro() {
        super("EndermanAgro", "Automatically looks into nearby enderman eyes.", ModuleCategory.of("Miscellaneous"), "autoenderman");
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) return;
        if (MC.player.isCreative() || MC.player.isGliding()) return;

        ItemStack helmet = MC.player.getEquippedStack(EquipmentSlot.HEAD);
        if (helmet.getItem() == Items.CARVED_PUMPKIN) return;

        EndermanEntity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : ENTITY_MANAGER.getNeutral()) {
            if (!(entity instanceof EndermanEntity enderman)) continue;

            double distance = MC.player.squaredDistanceTo(enderman);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = enderman;
            }
        }

        if (closest != null) {
            Vec3d eyes = closest.getEyePos();

            ROTATION_MANAGER.getRequestHandler().submit(
                    new RotationRequest(
                            EndermanAgro.class.getName(),
                            2,
                            (float) getYawToVec(MC.player, eyes),
                            (float) getPitchToVec(MC.player, eyes),
                            RotationModule.RotationMode.MOTION
                    )
            );
        }
    }
}