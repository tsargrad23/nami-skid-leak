package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.kiriyaga.nami.event.impl.LedgeClipEvent;
import me.kiriyaga.nami.event.impl.LiquidPushEvent;
import me.kiriyaga.nami.event.impl.SprintResetEvent;
import me.kiriyaga.nami.feature.module.impl.miscellaneous.ReachModule;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    private void clipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        LedgeClipEvent ledgeClipEvent = new LedgeClipEvent();
        EVENT_MANAGER.post(ledgeClipEvent);

        if (ledgeClipEvent.isCancelled()) {
            cir.setReturnValue(ledgeClipEvent.getClipped());
        }
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private Vec3d attack(Vec3d original) {
        if ((Object)this == MC.player) {
            SprintResetEvent sprintResetEvent = new SprintResetEvent();
            EVENT_MANAGER.post(sprintResetEvent);
            if (!sprintResetEvent.isCancelled()) {
                return original.multiply(0.6, 1.0, 0.6);
            }
        }
        return original;
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V"))
    private boolean attack(boolean original) {
        if ((Object)this == MC.player) {
            SprintResetEvent sprintResetEvent = new SprintResetEvent();
            EVENT_MANAGER.post(sprintResetEvent);
            if (!sprintResetEvent.isCancelled()) {
                return false;
            }
        }
        return original;
    }

    @Inject(method = "isPushedByFluids", at = @At("HEAD"), cancellable = true)
    private void isPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this != MC.player)
            return;

        LiquidPushEvent pushFluidsEvent = new LiquidPushEvent();
        EVENT_MANAGER.post(pushFluidsEvent);
        if (pushFluidsEvent.isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @ModifyReturnValue(method = "getBlockInteractionRange", at = @At("RETURN"))
    private double getBlockInteractionRange(double d) {
        if (MODULE_MANAGER == null || MODULE_MANAGER.getStorage() == null || MODULE_MANAGER.getStorage().getByClass(ReachModule.class) == null || !MODULE_MANAGER.getStorage().getByClass(ReachModule.class).isEnabled())
            return d;

        return MODULE_MANAGER.getStorage().getByClass(ReachModule.class).block.get() + d;
    }

    @ModifyReturnValue(method = "getEntityInteractionRange", at = @At("RETURN"))
    private double getEntityInteractionRange(double d) {
        if (MODULE_MANAGER == null || MODULE_MANAGER.getStorage() == null || MODULE_MANAGER.getStorage().getByClass(ReachModule.class) == null || !MODULE_MANAGER.getStorage().getByClass(ReachModule.class).isEnabled())
            return d;

        return MODULE_MANAGER.getStorage().getByClass(ReachModule.class).entity.get() + d;
    }
}