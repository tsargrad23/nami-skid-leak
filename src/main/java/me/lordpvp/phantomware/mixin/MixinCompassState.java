package me.lordpvp.phantomware.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.item.property.numeric.CompassState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(CompassState.class)
public abstract class MixinCompassState {
    @ModifyExpressionValue(method = "getBodyYaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getBodyYaw()F"))
    private static float callLivingEntityGetYaw(float original) {
        FreecamModule freecamModule = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);
        if (freecamModule != null && freecamModule.isEnabled() && MC != null && MC.gameRenderer != null && MC.gameRenderer.getCamera() != null) {
            return MC.gameRenderer.getCamera().getYaw();
        }
        return original;
    }

    @ModifyReturnValue(method = "getAngleTo(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)D", at = @At("RETURN"))
    private static double modifyGetAngleTo(double original, Entity entity, BlockPos pos) {
        FreecamModule freecamModule = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);
        if (freecamModule != null && freecamModule.isEnabled() && MC != null && MC.gameRenderer != null && MC.gameRenderer.getCamera() != null) {
            Vec3d vec3d = Vec3d.ofCenter(pos);
            Camera camera = MC.gameRenderer.getCamera();
            return Math.atan2(vec3d.getZ() - camera.getPos().z, vec3d.getX() - camera.getPos().x) / (float) (Math.PI * 2);
        }

        return original;
    }
}