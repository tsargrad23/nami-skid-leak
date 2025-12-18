package me.lordpvp.phantomware.mixin;

import me.kiriyaga.nami.feature.module.impl.client.FastLatencyModule;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(DebugHud.class)
public class MixinDebugHud {

    @Inject(method = "shouldShowPacketSizeAndPingCharts", at = @At("HEAD"), cancellable = true)
    private void shouldShowPacketSizeAndPingCharts(CallbackInfoReturnable<Boolean> cir) {
        var config = MODULE_MANAGER.getStorage().getByClass(FastLatencyModule.class);
        if (config != null && config.fastLatencyMode.get() == FastLatencyModule.FastLatencyMode.NEW) {
            cir.setReturnValue(true);
        }
    }
}
