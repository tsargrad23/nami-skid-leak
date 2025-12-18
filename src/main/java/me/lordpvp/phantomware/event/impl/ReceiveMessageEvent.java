package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import net.minecraft.network.message.MessageSignatureData;

public class ReceiveMessageEvent extends Event {

    private final Text message;
    private final MessageSignatureData signatureData;
    private final MessageIndicator indicator;

    public ReceiveMessageEvent(Text message, MessageSignatureData signatureData, MessageIndicator indicator) {
        this.message = message;
        this.signatureData = signatureData;
        this.indicator = indicator;
    }

    public Text getMessage() {
        return message;
    }

    public MessageSignatureData getSignatureData() {
        return signatureData;
    }

    public MessageIndicator getIndicator() {
        return indicator;
    }
}
