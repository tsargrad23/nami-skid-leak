package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.RenderScreenEvent;
import me.kiriyaga.nami.feature.module.impl.visuals.NoRenderModule;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), cancellable = true)
    public void noBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage() == null) return;

        NoRenderModule m = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);

        if (m != null && m.isEnabled() && m.noBackground.get() && MC.world != null) {
            ci.cancel();
        }
    }

    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    private void renderInGameBackground(DrawContext drawContext, CallbackInfo ci) {
        if (MODULE_MANAGER.getStorage() == null) return;

        NoRenderModule m = MODULE_MANAGER.getStorage().getByClass(NoRenderModule.class);

        if (m != null && m.isEnabled() && m.noBackground.get() && MC.world != null) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EVENT_MANAGER.post(new RenderScreenEvent(context, null, mouseX, mouseY));
    }
}

