package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.miscellaneous.UnfocusedFpsModule;
import net.minecraft.client.option.InactivityFpsLimiter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(InactivityFpsLimiter.class)
public class MixinInactivityFpsLimiter {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void updateHead(CallbackInfoReturnable<Integer> info) {
        if (MODULE_MANAGER.getStorage() == null) return;

        UnfocusedFpsModule module = MODULE_MANAGER.getStorage().getByClass(UnfocusedFpsModule.class);
        if (module == null) return;

        if (module.isEnabled() && (MC == null || !MC.isWindowFocused())) {
            info.setReturnValue(module.limit.get());
        }
    }
}