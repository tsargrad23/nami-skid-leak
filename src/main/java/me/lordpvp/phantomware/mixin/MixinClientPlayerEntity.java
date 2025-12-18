package me.lordpvp.phantomware.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.kiriyaga.nami.event.impl.*;
import me.kiriyaga.nami.feature.module.impl.client.DebugModule;
import me.kiriyaga.nami.feature.module.impl.client.RotationModule;
import me.kiriyaga.nami.feature.module.impl.movement.NoSlowModule;
import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {

    @Shadow public abstract void move(MovementType type, Vec3d movement);

    private float originalSilentPitch;
    private float originalYaw, originalPitch;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickHookPre(CallbackInfo ci) {

        EVENT_MANAGER.post(new PreTickEvent());
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickHookPost(CallbackInfo ci) {

        EVENT_MANAGER.post(new PostTickEvent());
    }

    @Inject(method = "pushOutOfBlocks", at = @At(value = "HEAD"), cancellable = true)
    private void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
        BlockPushEvent pushOutOfBlocksEvent = new BlockPushEvent();
        EVENT_MANAGER.post(pushOutOfBlocksEvent);

        if (pushOutOfBlocksEvent.isCancelled())
            ci.cancel();
    }

    @Inject(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
    private void onMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        MoveEvent moveEvent = new MoveEvent(movementType, movement);
        EVENT_MANAGER.post(moveEvent);

        if (moveEvent.isCancelled()) {
            ci.cancel();
            return;
        }

        Vec3d newMovement = moveEvent.getMovement();
        if (!newMovement.equals(movement)) {
            this.move(movementType, newMovement);
            ci.cancel();
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void preSendMovementPackets(CallbackInfo ci) {
        if (!ROTATION_MANAGER.getStateHandler().isRotating()) {
            ROTATION_MANAGER.getStateHandler().setServerYaw(MC.player.getYaw()); // we do update server rotations even tho no rotation is proceeded
            ROTATION_MANAGER.getStateHandler().setServerPitch(MC.player.getPitch());
            ROTATION_MANAGER.getStateHandler().setServerDeltaYaw(0f); // delta without rotations almost always lower then 30, its almost impossible without hacks to reach
            return;
        }

        originalYaw = MC.player.getYaw();
        originalPitch = MC.player.getPitch();

        float newYaw = ROTATION_MANAGER.getStateHandler().getRotationYaw();
        float newPitch = ROTATION_MANAGER.getStateHandler().getRotationPitch();
        MC.player.setYaw(newYaw);
        MC.player.setPitch(newPitch);

        MC.player.setBodyYaw(ROTATION_MANAGER.getStateHandler().getRenderYaw());
        MC.player.setHeadYaw(ROTATION_MANAGER.getStateHandler().getRenderYaw());

        float deltaYaw = newYaw - ROTATION_MANAGER.getStateHandler().getServerYaw();
        //float deltaPitch = newPitch - ROTATION_MANAGER.getStateHandler().getServerPitch();

        ROTATION_MANAGER.getStateHandler().setServerDeltaYaw(deltaYaw);

        ROTATION_MANAGER.getStateHandler().setServerYaw(newYaw);
        ROTATION_MANAGER.getStateHandler().setServerPitch(newPitch);
    }

    @Inject(method = "sendMovementPackets", at = @At("TAIL"))
    private void postSendMovementPackets(CallbackInfo ci) {
        if (!ROTATION_MANAGER.getStateHandler().isRotating())
            return;

        MODULE_MANAGER.getStorage().getByClass(DebugModule.class).debugRot(Text.of(
                "post yaw=" + MC.player.getYaw() + ", pitch=" + MC.player.getPitch() + "\n "
        ));

        MC.player.setYaw(originalYaw);
        MC.player.setPitch(originalPitch);
    }

    // Do not ask me exactly why is it so weird, it just works
    // overall silent rotations sucks, another super cool bypass, works really weirdly
    // i hope it gets fucking patched in 1.22/1.23
    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void sendMovementPackets1(CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage().getByClass(RotationModule.class).rotation.get() == RotationModule.RotationMode.SILENT
        && ROTATION_MANAGER.getStateHandler().getSilentSyncRequired()) {
            this.originalSilentPitch = MC.player.getPitch();
            this.lastPitchClient = -9999;
            MC.player.setPitch(this.originalSilentPitch + 1f);
        }
    }

    @Inject(method = "sendMovementPackets", at = @At("RETURN"))
    private void sendMovementPackets2(CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage().getByClass(RotationModule.class).rotation.get() == RotationModule.RotationMode.SILENT
                && ROTATION_MANAGER.getStateHandler().getSilentSyncRequired()) {
            MC.player.setPitch(this.originalSilentPitch);
            ROTATION_MANAGER.getStateHandler().setSilentSyncRequired(false);
        }
    }

    @Inject(method = "applyMovementSpeedFactors", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec2f;multiply(F)Lnet/minecraft/util/math/Vec2f;", ordinal = 1), cancellable = true)    private void onApplyMovementSpeedFactors(Vec2f vec2f, CallbackInfoReturnable<Vec2f> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (self instanceof PlayerEntity player && player.isUsingItem() && !player.hasVehicle()) {
            ItemUseSlowEvent event = new ItemUseSlowEvent(player, player.getActiveItem());
            EVENT_MANAGER.post(event);

            if (event.isCancelled()) {
                Vec2f vec2f2 = vec2f.multiply(0.98F);
                cir.setReturnValue(applyDirectionalMovementSpeedFactors(vec2f2));
            }
        }
    }

    @Shadow
    private static Vec2f applyDirectionalMovementSpeedFactors(Vec2f vec2f) {
        throw new AssertionError();
    }

    @Shadow private float lastPitchClient;

    @Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
    private void shouldSlowDown(CallbackInfoReturnable<Boolean> info) {
        if (MODULE_MANAGER == null || MODULE_MANAGER.getStorage() == null || MODULE_MANAGER.getStorage().getByClass(NoSlowModule.class) == null || !MODULE_MANAGER.getStorage().getByClass(NoSlowModule.class).isEnabled() || !MODULE_MANAGER.getStorage().getByClass(NoSlowModule.class).fastCrawl.get())
            return;

        boolean b = !MC.player.isCrawling();
        if (b) return;

        info.setReturnValue(b);
    }

    @ModifyExpressionValue(method = "tickNausea", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"))
    private Screen tickNausea(Screen s) {
        if (MODULE_MANAGER == null)
            return s;

        if (MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class).isEnabled() && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class).portalGui.get())
            return null;

        return s;
    }
}
