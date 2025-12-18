package me.lordpvp.phantomware.mixin;

import me.kiriyaga.nami.event.impl.BreakBlockEvent;
import me.kiriyaga.nami.event.impl.PlaceBlockEvent;
import me.kiriyaga.nami.event.impl.StartBreakingBlockEvent;
import me.kiriyaga.nami.feature.module.impl.world.NoBreakDelayModule;
import me.kiriyaga.nami.mixininterface.IClientPlayerInteractionManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.*;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager implements IClientPlayerInteractionManager {
    @Shadow
    private int blockBreakingCooldown;

    private float savedYaw, savedPitch;

    @Shadow protected abstract void syncSelectedSlot();

    @Override
    public void updateSlot() {
        this.syncSelectedSlot();
    }

    @Inject(method = "interactItem", at = @At("HEAD"))
    private void interactItem1(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (player != MC.player) return;
        if (!ROTATION_MANAGER.getStateHandler().isRotating()) return;

        savedYaw = player.getYaw();
        savedPitch = player.getPitch();

        float spoofYaw = ROTATION_MANAGER.getStateHandler().getRotationYaw();
        float spoofPitch = ROTATION_MANAGER.getStateHandler().getRotationPitch();

        player.setYaw(spoofYaw);
        player.setPitch(spoofPitch);
        player.setHeadYaw(ROTATION_MANAGER.getStateHandler().getRenderYaw());
        player.setBodyYaw(ROTATION_MANAGER.getStateHandler().getRenderYaw());
    }

    @Inject(method = "interactItem", at = @At("RETURN"))
    private void interactItem2(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (player != MC.player) return;
        if (!ROTATION_MANAGER.getStateHandler().isRotating()) return;

        player.setYaw(savedYaw);
        player.setPitch(savedPitch);
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> call){
        StartBreakingBlockEvent ev = new StartBreakingBlockEvent(blockPos, direction);
        EVENT_MANAGER.post(ev);

        if (ev.isCancelled())
            call.cancel();
    }

    @Inject(method = "interactBlock", at = @At(value = "HEAD"), cancellable = true)
    private void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        PlaceBlockEvent interactBlockEvent = new PlaceBlockEvent(player, hand, hitResult);
        EVENT_MANAGER.post(interactBlockEvent);

        if (interactBlockEvent.isCancelled()) {
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
        }
    }

    @Inject(method = "breakBlock", at = @At(value = "HEAD"), cancellable = true)
    private void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BreakBlockEvent breakBlockEvent = new BreakBlockEvent(pos);
        EVENT_MANAGER.post(breakBlockEvent);
        if (breakBlockEvent.isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void disableBreakCooldown(CallbackInfoReturnable<Boolean> cir) {
        if (MODULE_MANAGER.getStorage() == null) return;

        //CHAT_MANAGER.sendRaw(""+this.blockBreakingCooldown);

        NoBreakDelayModule noBreakDelay = MODULE_MANAGER.getStorage().getByClass(NoBreakDelayModule.class);
        if (noBreakDelay != null && noBreakDelay.isEnabled()) {
            this.blockBreakingCooldown = 0;
        }
    }
}
