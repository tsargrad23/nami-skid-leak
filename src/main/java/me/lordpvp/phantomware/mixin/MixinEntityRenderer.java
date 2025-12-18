package me.lordpvp.phantomware.mixin;

import me.kiriyaga.nami.feature.module.impl.visuals.NametagsModule;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    private void onRenderLabel(T entity, CallbackInfoReturnable<Text> cir) {
        NametagsModule nametagsModule = MODULE_MANAGER.getStorage() != null
                ? MODULE_MANAGER.getStorage().getByClass(NametagsModule.class)
                : null;

        if (nametagsModule != null && nametagsModule.isEnabled()) {
            cir.setReturnValue(null);
            return;
        }
    }
}
