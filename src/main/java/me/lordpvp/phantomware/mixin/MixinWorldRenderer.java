package me.kiriyaga.nami.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.impl.visuals.FreeLookModule;
import me.kiriyaga.nami.feature.module.impl.visuals.FreecamModule;
import me.kiriyaga.nami.feature.module.impl.visuals.NoWeatherModule;
import me.kiriyaga.nami.feature.module.impl.visuals.ViewClipModule;
import me.kiriyaga.nami.util.MatrixCache;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderTail(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        float tickDelta = tickCounter.getTickProgress(true);

        MatrixStack matrices = new MatrixStack();
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        EVENT_MANAGER.post(new Render3DEvent(matrices, tickDelta, camera, positionMatrix, projectionMatrix));

        matrices.pop();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void captureMatrices(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, GpuBufferSlice fog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
        MatrixCache.positionMatrix = new Matrix4f(positionMatrix);
        MatrixCache.projectionMatrix = new Matrix4f(projectionMatrix);
        MatrixCache.camera = camera;
    }

    @WrapWithCondition(method = "method_62216", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WeatherRendering;renderPrecipitation(Lnet/minecraft/world/World;Lnet/minecraft/client/render/VertexConsumerProvider;IFLnet/minecraft/util/math/Vec3d;)V"))
    private boolean shouldRenderPrecipitation(WeatherRendering instance, World world, VertexConsumerProvider vertexConsumers, int ticks, float tickProgress, Vec3d pos) {
        NoWeatherModule noWeatherModule = MODULE_MANAGER.getStorage().getByClass(NoWeatherModule.class);
        return noWeatherModule == null || !noWeatherModule.isEnabled();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V"), index = 3)
    private boolean renderSetupTerrainModifyArg(boolean spectator) {
        FreecamModule freecamModule = MODULE_MANAGER.getStorage().getByClass(FreecamModule.class);
        FreeLookModule freeLookModule = MODULE_MANAGER.getStorage().getByClass(FreeLookModule.class);
        ViewClipModule viewClipModule = MODULE_MANAGER.getStorage().getByClass(ViewClipModule.class);

        boolean freecam = freecamModule != null && freecamModule.isEnabled();
        boolean freelook = freeLookModule != null && freeLookModule.isEnabled();
        boolean viewclip = viewClipModule != null && viewClipModule.isEnabled() && MC.options.getPerspective() != Perspective.FIRST_PERSON;

        return freecam || spectator || freelook || viewclip;
    }
}
