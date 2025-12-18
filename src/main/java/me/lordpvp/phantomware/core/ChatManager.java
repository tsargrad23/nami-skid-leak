package me.kiriyaga.nami.core;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.mixin.ChatHudAccessor;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

import java.security.SecureRandom;
import java.util.*;

import static me.kiriyaga.nami.Nami.*;

public class ChatManager {

    /**
     * Usage:
     * .sendRaw() just sends raw message without tracking
     *
     * .sendPersistent() non timed/replaced by default message, but is tracked and we can delete it by its key
     * Why? Because we want to track session joins/leaves, for example, and clean "outdated" info, but without
     * affecting other messages. Can be replaced by key.
     *
     * .removePersistent() all by its name
     *
     * .sendTransient() transient message, that will be deleted and replaced while new transient message appears     *
     *
     * .clear() clears all messages (contained by us btw)
     *
     */

    public final Map<String, MessageSignatureData> persistentMessages = new HashMap<>();
    public MessageSignatureData transientSignature = null;

    private final List<Text> allMessages = new ArrayList<>(); // theese are always key == null

    public void init(){
        EVENT_MANAGER.register(this);
    }

    private MessageSignatureData generateSignature() {
        byte[] data = new byte[256];
        new SecureRandom().nextBytes(data);
        return new MessageSignatureData(data);
    }

    public void sendRaw(String message) {
        sendRaw(Text.literal(message), true);
    }

    public void sendRaw(String message, boolean prefix) {
        sendRaw(Text.literal(message), prefix);
    }

    public void sendRaw(Text message) {
        sendRaw(message, true);
    }

    public void sendRaw(Text message, boolean prefix) {
        if (MC == null || MC.inGameHud == null || getChatHud() == null) return;
        Text text = prefix ? prefix().copy().append(message) : message;
        getChatHud().addMessage(text);
    }

    public void sendPersistent(String key, String message) {
        sendPersistent(key, Text.literal(message), true);
    }

    public void sendPersistent(String key, String message, boolean prefix) {
        sendPersistent(key, Text.literal(message), prefix);
    }

    public void sendPersistent(String key, Text message) {
        sendPersistent(key, message, true);
    }

    public void sendPersistent(String key, Text message, boolean prefix) {
        if (MC == null || MC.inGameHud == null || getChatHud() == null) return;

        ChatHud chatHud = getChatHud();

        if (persistentMessages.containsKey(key)) {
            removeSilently(persistentMessages.get(key));
        }

        Text text = prefix ? prefix().copy().append(message) : message;
        MessageSignatureData signature = generateSignature();
        MessageIndicator indicator = indicator();

        chatHud.addMessage(text, signature, indicator);
        persistentMessages.put(key, signature);
    }

    public void sendTransient(String message) {
        sendTransient(Text.literal(message), true);
    }

    public void sendTransient(String message, boolean prefix) {
        sendTransient(Text.literal(message), prefix);
    }

    public void sendTransient(Text message) {
        sendTransient(message, true);
    }

    public void sendTransient(Text message, boolean prefix) {
        if (MC == null || MC.inGameHud == null || getChatHud() == null) return;

        ChatHud chatHud = getChatHud();

        if (transientSignature != null) {
            removeSilently(transientSignature);
            transientSignature = null;
        }

        Text text = prefix ? prefix().copy().append(message) : message;
        MessageSignatureData signature = generateSignature();
        MessageIndicator indicator = indicator();

        chatHud.addMessage(text, signature, indicator);
        transientSignature = signature;
    }

    public void removePersistent(String key) {
        if (MC == null || MC.inGameHud == null || getChatHud() == null) return;

        if (persistentMessages.containsKey(key)) {
            removeSilently(persistentMessages.get(key));
            persistentMessages.remove(key);
        }
    }

    public void clear() {
        if (MC == null || MC.inGameHud == null || getChatHud() == null) return;

        ChatHud chatHud = getChatHud();
        for (MessageSignatureData sig : persistentMessages.values()) {
            chatHud.removeMessage(sig);
            removeSilently(sig);
        }
        persistentMessages.clear();

        if (transientSignature != null) {
            removeSilently(transientSignature);
            transientSignature = null;
        }

        allMessages.clear();
    }

    private ChatHud getChatHud() {
        return MC.inGameHud.getChatHud();
    }

    public void removeSilently(MessageSignatureData signature) {
        if (MC == null || MC.inGameHud == null || getChatHud() == null) return;

        ChatHud hud = MC.inGameHud.getChatHud();
        ChatHudAccessor accessor = (ChatHudAccessor) hud;

        accessor.getMessages().removeIf(line -> signature.equals(line.comp_915()));

        accessor.getVisibleMessages().removeIf(visible -> {
            for (ChatHudLine line : accessor.getMessages()) {
                if (signature.equals(line.comp_915())) {
                    return visible.comp_896().equals(line.comp_893());
                }
            }
            return false;
        });
        accessor.callRefresh();

        allMessages.remove(signature);
    }

    private MessageIndicator indicator() {
        int global = MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor().getRGB() & 0x00FFFFFF;
        return new MessageIndicator(global, null, Text.literal(DISPLAY_NAME), NAME);
    }

    private Text prefix() {
        return CAT_FORMAT.format("{s}[{g}" + NAME + "{s}] {reset}");
    }

    public void removeByText(String text) {
        if (MC == null || MC.inGameHud == null || getChatHud() == null) return;

        ChatHud hud = getChatHud();
        ChatHudAccessor accessor = (ChatHudAccessor) hud;

        accessor.getMessages().removeIf(line -> line.comp_893().getString().equals(text));

        accessor.getVisibleMessages().removeIf(visible -> visible.comp_896().toString().equals(text));

        allMessages.removeIf(t -> t.getString().equals(text));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreTick(PreTickEvent ev) {
        if (MC == null || MC.inGameHud == null || getChatHud() == null) return;

        ChatHud hud = getChatHud();
        ChatHudAccessor accessor = (ChatHudAccessor) hud;

        allMessages.clear();
        for (ChatHudLine line : accessor.getMessages()) {
            Text text = line.comp_893();

            if (persistentMessages.containsValue(line.comp_915())) continue;
            if (transientSignature != null && transientSignature.equals(line.comp_915())) continue;

            allMessages.add(text);
        }
    }

    public List<Text> getAllMessages() {
        return allMessages;
    }
}