package me.lordpvp.phantomware.mixin;

import me.kiriyaga.nami.event.impl.EntitySpawnEvent;
import me.kiriyaga.nami.feature.module.impl.visuals.SkyColorModule;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {

    @Unique
    private final DimensionEffects dimensionEffectEnd = new DimensionEffects.End();

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void addEntity(Entity entity, CallbackInfo ci) {
        if (entity == null)
            return;

        EntitySpawnEvent ev = new EntitySpawnEvent(entity);
        EVENT_MANAGER.post(ev);

        if (ev.isCancelled()) // you dont actually need this
            ci.cancel();
    }


    @Inject(method = "getDimensionEffects", at = @At("HEAD"), cancellable = true)
    private void getDimensionEffects(CallbackInfoReturnable<DimensionEffects> ci) {
        SkyColorModule sc = MODULE_MANAGER.getStorage().getByClass(SkyColorModule.class);

        if (MC.world == null || sc == null || !sc.isEnabled())
            return;

        if (sc.endSky.get()) {
            ci.setReturnValue(dimensionEffectEnd);
        return;
        }
        if (sc.dimension != null)
            ci.setReturnValue(sc.dimension);
    }
}