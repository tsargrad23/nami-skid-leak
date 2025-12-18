package me.lordpvp.phantomware.mixin;

import me.kiriyaga.nami.Nami;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.Pair;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen {

    @Inject(method = "connect", at = @At("HEAD"))
    private static void onConnect(
            Screen screen,
            MinecraftClient client,
            ServerAddress address,
            ServerInfo info,
            boolean quickPlay,
            CookieStorage cookieStorage, // 1.21.5 loved
            CallbackInfo ci
    ) {
        Nami.LAST_CONNECTION = new Pair<>(address, info);
    }
}

