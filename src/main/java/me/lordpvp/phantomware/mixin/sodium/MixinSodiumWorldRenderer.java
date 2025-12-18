package me.kiriyaga.nami.mixin.sodium;

import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Pseudo
@Mixin(SodiumWorldRenderer.class)
public class MixinSodiumWorldRenderer {
    @ModifyVariable(method = "setupTerrain", at = @At("HEAD"), argsOnly = true)
    private FogParameters modifyFogParameters(FogParameters fogParameters) {
        if (MODULE_MANAGER.getStorage() == null) return fogParameters;

        NoRenderModule noRender = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);
        if (noRender == null) return fogParameters;

        if (noRender.noFog.get()) {
            return FogParameters.NONE;
        }

        return fogParameters;
    }
}