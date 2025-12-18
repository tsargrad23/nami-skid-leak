package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.DissconectEvent;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.EVENT_MANAGER;

@Mixin(GameMenuScreen.class)
public class MixinGameMenuScreen {
    @Inject(method = "disconnect", at = @At(value = "HEAD"), cancellable = true)
    private static void hookDisconnect(CallbackInfo ci) {
        DissconectEvent ev = new DissconectEvent();
        EVENT_MANAGER.post(ev);

        if (ev.isCancelled())
            ci.cancel();
    }
}