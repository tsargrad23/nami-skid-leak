package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.kiriyaga.nami.feature.module.impl.visuals.ViewModelModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {

    @Shadow
    private float equipProgressMainHand;

    @Shadow
    private float equipProgressOffHand;

    @Shadow
    private ItemStack mainHand;

    @Shadow
    private ItemStack offHand;

    @Shadow
    protected abstract boolean shouldSkipHandAnimationOnSwap(ItemStack from, ItemStack to);

    @ModifyArg(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 2), index = 0)
    private float modifyEquipProgressMainhand(float value) {
        ViewModelModule viewModelModule = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        boolean isOldAnimationsEnabled = viewModelModule != null && viewModelModule.isEnabled() && viewModelModule.oldAnimation.get();

        float attackCooldown = MC.player.getAttackCooldownProgress(1f);
        float modifiedValue = isOldAnimationsEnabled ? 1f : attackCooldown * attackCooldown * attackCooldown;

        boolean skipAnimation = shouldSkipHandAnimationOnSwap(mainHand, MC.player.getMainHandStack());

        return (skipAnimation ? modifiedValue : 0f) - equipProgressMainHand;
    }

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                              float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                              VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {

        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        boolean isMainHand = hand == Hand.MAIN_HAND;

        if (vm != null && vm.isEnabled() && !(isMainHand && item.isEmpty() && !vm.hand.get())) {

            matrices.push();

            float mirror = isMainHand ? 1.0f : -1.0f;

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(vm.rotX.get().floatValue()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(vm.rotY.get().floatValue() * mirror));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(vm.rotZ.get().floatValue() * mirror));

            matrices.translate(
                    vm.posX.get().floatValue() * mirror,
                    vm.posY.get().floatValue(),
                    vm.posZ.get().floatValue()
            );

            float s = vm.scale.get().floatValue();
            matrices.scale(s, s, s);
        }
    }

//    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE",
//            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem" +
//                    "(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;" +
//                    "Lnet/minecraft/item/ItemDisplayContext;" +
//                    "Lnet/minecraft/client/util/math/MatrixStack;" +
//                    "Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
//    private void scaleItems(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
//                            float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
//                            VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
//        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
//        if (vm != null && vm.isEnabled()) {
//            float s = vm.scale.get().floatValue();
//            matrices.scale(s, s, s);
//        }
//    }

    @Inject(method = "renderFirstPersonItem", at = @At("TAIL"))
    private void matricesPop(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                             float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                             VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        boolean isMainHand = hand == Hand.MAIN_HAND;

        if (vm != null && vm.isEnabled() && !(isMainHand && item.isEmpty() && !vm.hand.get())) {
            matrices.pop();
        }
    }

    @Inject(method = "applyEatOrDrinkTransformation", at = @At("HEAD"), cancellable = true)
    private void applyEatOrDrinkTransformation(MatrixStack matrixStack, float tickDelta, Arm arm, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        if (vm != null && vm.isEnabled() && !vm.eating.get())
            ci.cancel();
    }

    @WrapOperation(
            method = "applyEatOrDrinkTransformation(Lnet/minecraft/client/util/math/MatrixStack;FLnet/minecraft/util/Arm;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
    private void applyEatOrDrinkTransformation2(MatrixStack matrices, float x, float y, float z, Operation<Void> original) {
        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        if (vm != null && vm.isEnabled() && vm.eating.get()) {
            if (x == 0.0F && z == 0.0F) {
                double mul = vm.eatingBob.get();
                original.call(matrices, x, (float) (y * mul), z);
                return;
            }
        }
        original.call(matrices, x, y, z);
    }

    @WrapWithCondition(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;" +
            "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;" +
            "Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionfc;)V"))
    private boolean renderItem(MatrixStack instance, Quaternionfc quaternion) {
        ViewModelModule vm = MODULE_MANAGER.getStorage().getByClass(ViewModelModule.class);
        return vm == null || !vm.isEnabled() || vm.sway.get();
    }
}
