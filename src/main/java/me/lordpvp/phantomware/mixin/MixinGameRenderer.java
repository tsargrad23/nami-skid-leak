package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.kiriyaga.nami.feature.module.impl.miscellaneous.ReachModule;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    public abstract void updateCrosshairTarget(float tickDelta);

    @Shadow
    public abstract void reset();

    @Shadow
    @Final
    private Camera camera;

    @Unique
    private final MatrixStack matrices = new MatrixStack();

    @Shadow
    protected abstract void bobView(MatrixStack matrices, float tickDelta);

    @Shadow
    protected abstract void tiltViewWhenHurt(MatrixStack matrices, float tickDelta);


    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (MODULE_MANAGER.getStorage() == null) return;

        NoRenderModule noRender = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (noRender != null && floatingItem.getItem() == Items.TOTEM_OF_UNDYING && noRender.isEnabled() && noRender.noTotem.get()) {
            info.cancel();
        }
    }

    @Unique
    private boolean freecamSet = false;

    @Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo info) {
        if (MODULE_MANAGER.getStorage() == null) return;

        FreecamModule freecamModule = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);
        if (freecamModule == null || !freecamModule.isEnabled()) return;

        if (client == null) return;

        if (client.getCameraEntity() != null && !freecamSet) {
            info.cancel();

            Entity cameraE = client.getCameraEntity();

            double x = cameraE.getX();
            double y = cameraE.getY();
            double z = cameraE.getZ();
            double lastX = cameraE.lastX;
            double lastY = cameraE.lastY;
            double lastZ = cameraE.lastZ;
            float yaw = cameraE.getYaw();
            float pitch = cameraE.getPitch();
            float lastYaw = cameraE.lastYaw;
            float lastPitch = cameraE.lastPitch;

            cameraE.setPos(freecamModule.getX(), freecamModule.getY() - cameraE.getEyeHeight(cameraE.getPose()), freecamModule.getZ());

            cameraE.lastX = freecamModule.prevPos.x;
            cameraE.lastY = freecamModule.prevPos.y - cameraE.getEyeHeight(cameraE.getPose());
            cameraE.lastZ = freecamModule.prevPos.z;

            cameraE.setYaw(freecamModule.yaw);
            cameraE.setPitch(freecamModule.pitch);
            cameraE.lastYaw = freecamModule.lastYaw;
            cameraE.lastPitch = freecamModule.lastPitch;

            freecamSet = true;

            if (client.gameRenderer != null && client.gameRenderer.getCamera() != null) {
                updateCrosshairTarget(tickDelta);
            }

            freecamSet = false;

            cameraE.setPos(x, y, z);
            cameraE.lastX = lastX;
            cameraE.lastY = lastY;
            cameraE.lastZ = lastZ;
            cameraE.setYaw(yaw);
            cameraE.setPitch(pitch);
            cameraE.lastYaw = lastYaw;
            cameraE.lastPitch = lastPitch;
        }
    }


    @ModifyReturnValue(method = "findCrosshairTarget", at = @At("RETURN"))
    private HitResult findCrosshairTarget(HitResult original, @Local HitResult hitResult) {
        ReachModule reachModule = MODULE_MANAGER.getStorage().getByClass(ReachModule.class);
        if (reachModule == null || !reachModule.isEnabled() || !reachModule.noEntityTrace.get()) {
            return original;
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            boolean playerOnly = reachModule.playerOnly.get();
            boolean pickaxeOnly = reachModule.pickaxeOnly.get();

            var targetEntity = getTargetedEntity();
            var mainHandItem = MC.player.getMainHandStack().getItem();

            boolean lookingAtPlayer = targetEntity != null && targetEntity.isPlayer();
            boolean holdingPickaxe = isPickaxe(mainHandItem);

            if (playerOnly && pickaxeOnly) {
                if (lookingAtPlayer && holdingPickaxe) {
                    return hitResult;
                } else {
                    return original;
                }
            }

            if (playerOnly) {
                if (lookingAtPlayer) {
                    return hitResult;
                } else {
                    return original;
                }
            }

            if (pickaxeOnly) {
                if (holdingPickaxe) {
                    return hitResult;
                } else {
                    return original;
                }
            }

            return hitResult;
        }

        return original;
    }

    private Entity getTargetedEntity() {
        if (MC.crosshairTarget != null && MC.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) MC.crosshairTarget).getEntity();
        }
        return null;
    }

    private boolean isPickaxe(Item item) {
        return item.getDefaultStack().isIn(ItemTags.PICKAXES);
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void bobView(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage() != null && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class) != null && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class).isEnabled() && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class).noBob.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage() != null && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class) != null && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class).isEnabled() && MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class).noTilt.get()) {
            ci.cancel();
        }
    }
}