package me.lordpvp.phantomware.mixin;

import me.kiriyaga.nami.event.impl.ReceiveMessageEvent;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(ChatHud.class)
public abstract class MixinChatHud {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        if (signatureData != null) {
            if (CHAT_MANAGER.transientSignature != null && signatureData.equals(CHAT_MANAGER.transientSignature)) {
                return;
            }

            if (CHAT_MANAGER.persistentMessages.containsValue(signatureData)) {
                return;
            }
        }

        ReceiveMessageEvent event = new ReceiveMessageEvent(message, signatureData, indicator);
        EVENT_MANAGER.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
