package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.kiriyaga.nami.feature.module.impl.client.RotationModule;
import me.kiriyaga.nami.feature.module.impl.movement.ElytraFlyModule;
import me.kiriyaga.nami.feature.module.impl.movement.HighJumpModule;
import me.kiriyaga.nami.feature.module.impl.movement.NoJumpDelayModule;
import me.kiriyaga.nami.feature.module.impl.movement.NoLevitation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;


@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    private float originalYaw;
    @Shadow
    private int jumpingCooldown;
    private float originalPitch;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void travelPreHook(Vec3d movementInput, CallbackInfo ci) {
        if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().player != (Object)this) return;
        if (MODULE_MANAGER.getStorage() == null) return;
        RotationModule rotationModule = MODULE_MANAGER.getStorage().getByClass(RotationModule.class);
        if (rotationModule == null || !rotationModule.moveFix.get()) return;
        if (ROTATION_MANAGER == null || !ROTATION_MANAGER.getStateHandler().isRotating()) return;

        originalYaw = super.getYaw();
        originalPitch = super.getPitch();

        float spoofYaw = ROTATION_MANAGER.getStateHandler().getRotationYaw();
        float spoofPitch = ROTATION_MANAGER.getStateHandler().getRotationPitch();

        this.setYaw(spoofYaw);
        this.setPitch(spoofPitch);
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void travelPostHook(Vec3d movementInput, CallbackInfo ci) {
        if (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().player != (Object)this) return;
        if (ROTATION_MANAGER == null || !ROTATION_MANAGER.getStateHandler().isRotating()) return;

        if (MODULE_MANAGER.getStorage() == null) return;
        RotationModule rotationModule = MODULE_MANAGER.getStorage().getByClass(RotationModule.class);
        if (rotationModule == null || !rotationModule.moveFix.get()) return;

        this.setYaw(originalYaw);
        this.setPitch(originalPitch);
    }

    @ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
    private float jumpFix(float originalYaw) {
        if ((Object)this != MinecraftClient.getInstance().player) return originalYaw;
        return ROTATION_MANAGER != null ? ROTATION_MANAGER.getStateHandler().getRotationYaw() : originalYaw;
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 2, shift = At.Shift.BEFORE))
    private void doItemUse(CallbackInfo info) {
        NoJumpDelayModule module = MODULE_MANAGER.getStorage() != null ? MODULE_MANAGER.getStorage().getByClass(NoJumpDelayModule.class) : null;
        if (module != null && module.isEnabled()) {
            jumpingCooldown = 0;
        }
    }

    @Inject(at = @At("HEAD"), method = "isGliding()Z", cancellable = true)
    private void isGlidingZ(CallbackInfoReturnable<Boolean> cir) {
        ElytraFlyModule elytraFlyModule = MODULE_MANAGER.getStorage() != null ? MODULE_MANAGER.getStorage().getByClass(ElytraFlyModule.class) : null;
        if (elytraFlyModule != null && MC.player != null && elytraFlyModule.mode.get() == ElytraFlyModule.FlyMode.BOUNCE &&
                (Object)this == MinecraftClient.getInstance().player && elytraFlyModule.isEnabled() &&
                MC.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        HighJumpModule mod = MODULE_MANAGER.getStorage() != null ? MODULE_MANAGER.getStorage().getByClass(HighJumpModule.class) : null;
        if (mod == null || !mod.isEnabled()) return;

        float jumpBoost = mod.height.get().floatValue();

        Vec3d vel = this.getVelocity();
        this.setVelocity(vel.x, Math.max(jumpBoost, vel.y), vel.z);

        if (this.isSprinting()) {
            float yawRad = this.getYaw() * 0.017453292F;
            this.addVelocityInternal(new Vec3d(-MathHelper.sin(yawRad) * 0.2, 0.0, MathHelper.cos(yawRad) * 0.2));
        }

        this.velocityDirty = true;
        ci.cancel();
    }

//    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
//    private void setSprinting(boolean sprinting, CallbackInfo ci) {
//        if ((Object)this != MinecraftClient.getInstance().player) return;
//
//        RotationManagerModule rotationModule = MODULE_MANAGER.getStorage() != null ? MODULE_MANAGER.getStorage().getByClass(RotationManagerModule.class) : null;
//        if (rotationModule == null || ROTATION_MANAGER == null || !ROTATION_MANAGER.getStateHandler().isRotating() || !rotationModule.sprintFix.get())
//            return;
//
//        if (sprinting && MC.player.input != null) {
//            Vec2f movement = MC.player.input.getMovementInput();
//            float forward = movement.x;
//            float sideways = movement.y;
//
//            if (forward == 0 && sideways == 0) {
//                ci.cancel();
//                super.setSprinting(false);
//                return;
//            }
//
//            float spoofYaw = lastSendedYaw;
//            float realYaw = MC.player.getYaw();
//
//            Vec3d localMovement = new Vec3d(sideways, 0, forward);
//            Vec3d globalMovement = localToGlobal(localMovement, realYaw);
//
//            Vec3d localRelativeToSpoof = globalToLocal(globalMovement, spoofYaw);
//
//            double moveAngleRad = Math.atan2(localRelativeToSpoof.z, localRelativeToSpoof.x);
//            float moveAngleDeg = (float) Math.toDegrees(moveAngleRad);
//            moveAngleDeg = MathHelper.wrapDegrees(moveAngleDeg);
//
//            if (Math.abs(moveAngleDeg) > 33f) {
//                ci.cancel();
//                super.setSprinting(false);
//            }
//        }
//    }

    @ModifyReturnValue(method = "hasStatusEffect", at = @At("RETURN"))
    private boolean hasStatusEffect(boolean original, RegistryEntry<StatusEffect> effect) {
        if ((Object) this instanceof PlayerEntity player &&
                player == MC.player) {

            NoLevitation nl = MODULE_MANAGER.getStorage().getByClass(NoLevitation.class);
            if (nl != null && nl.isEnabled()) {
                RegistryKey<StatusEffect> slowFallKey = StatusEffects.SLOW_FALLING.getKey().orElse(null);
                if (nl.noSlowFall.get() && slowFallKey != null && effect.matchesKey(slowFallKey))
                    return false;

                RegistryKey<StatusEffect> levitationKey = StatusEffects.LEVITATION.getKey().orElse(null); // this just removes levitation damage reducing
                if (levitationKey != null && effect.matchesKey(levitationKey))
                    return false;
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "getStatusEffect", at = @At("RETURN"))
    private StatusEffectInstance getStatusEffect(StatusEffectInstance original, RegistryEntry<StatusEffect> effect) {
        NoLevitation nl = MODULE_MANAGER.getStorage().getByClass(NoLevitation.class);
        if (nl != null && nl.isEnabled()) {
            if (effect == StatusEffects.LEVITATION)
                return null;
//            if (nl.noSlowFall.get() && effect.value == StatusEffects.SLOW_FALLING)
//                return null;
        }
        return original;
    }
}
