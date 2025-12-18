package me.lordpvp.phantomware.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {

    @ModifyExpressionValue(
            method = "getFogBuffer",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/fog/FogRenderer;fogEnabled:Z")
    )
    private boolean modifyFogEnabled(boolean original) {
        NoRenderModule noRender = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (noRender != null && noRender.isEnabled() && noRender.noFog.get()) {
            return false;
        }
        return original;
    }
}
