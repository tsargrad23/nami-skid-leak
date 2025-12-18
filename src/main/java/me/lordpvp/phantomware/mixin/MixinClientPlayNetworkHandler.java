package me.lordpvp.phantomware.mixin;

import me.kiriyaga.nami.event.impl.ChatMessageEvent;
import me.kiriyaga.nami.event.impl.ChunkDataEvent;
import me.kiriyaga.nami.event.impl.WorldTimeUpdateEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.*;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Shadow
    private ClientWorld world;

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void onSendChatMessage(String message, CallbackInfo ci) {
        ChatMessageEvent event = new ChatMessageEvent(message);
        EVENT_MANAGER.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo info) {
        if (world == null) return;

        WorldChunk chunk = world.getChunk(packet.getChunkX(), packet.getChunkZ());
        if (chunk == null || chunk.isEmpty()) return;

        EVENT_MANAGER.post(new ChunkDataEvent(chunk));
    }

    @Inject(method = "onWorldTimeUpdate", at = @At("HEAD"), cancellable = true)
    private void onWorldTimeUpdate(WorldTimeUpdateS2CPacket worldTimeUpdateS2CPacket, CallbackInfo ci) {
        WorldTimeUpdateEvent event = new WorldTimeUpdateEvent();
        EVENT_MANAGER.post(event);

        if (event.isCancelled()) ci.cancel();
    }
}
