package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.MouseClickEvent;
import me.kiriyaga.nami.event.impl.MouseScrollEvent;
import me.kiriyaga.nami.event.impl.RenderScreenEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.EVENT_MANAGER;
import static me.kiriyaga.nami.Nami.MC;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        MouseClickEvent event = new MouseClickEvent(mouseX, mouseY, button);
        EVENT_MANAGER.post(event);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        MouseScrollEvent event = new MouseScrollEvent(mouseX, mouseY, verticalAmount);
        EVENT_MANAGER.post(event);
    }
}
