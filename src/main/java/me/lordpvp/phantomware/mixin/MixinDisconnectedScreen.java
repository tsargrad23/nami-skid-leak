package me.lordpvp.phantomware.mixin;

import me.kiriyaga.nami.feature.module.impl.miscellaneous.AutoReconnectModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.kiriyaga.nami.Nami.LAST_CONNECTION;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(DisconnectedScreen.class)
public abstract class MixinDisconnectedScreen extends Screen {

    @Unique private ButtonWidget reconnectButton;
    @Unique private ButtonWidget toggleButton;
    @Unique private double time = 100.0;

    protected MixinDisconnectedScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        AutoReconnectModule module = MODULE_MANAGER.getStorage().getByClass(AutoReconnectModule.class);
        if (module == null) return;

        if (LAST_CONNECTION != null || !module.hardHide.get()) {
            reconnectButton = ButtonWidget.builder(Text.literal(getReconnectText(module)), button -> tryReconnect())
                    .width(200)
                    .build();

            toggleButton = ButtonWidget.builder(Text.literal(getToggleText(module)), button -> {
                module.toggle();
                toggleButton.setMessage(Text.literal(getToggleText(module)));
                reconnectButton.setMessage(Text.literal(getReconnectText(module)));
                time = module.delay.get() * 20;
            }).width(200).build();

            int centerX = this.width / 2;
            int y = this.height / 2 + 40;

            reconnectButton.setPosition(centerX - 100, y);
            toggleButton.setPosition(centerX - 100, y + 25);

            this.addDrawableChild(reconnectButton);
            this.addDrawableChild(toggleButton);
        }
    }

    @Override
    public void tick() {
        AutoReconnectModule module = MODULE_MANAGER.getStorage().getByClass(AutoReconnectModule.class);
        if (module == null) return;

        if (!module.isEnabled() || LAST_CONNECTION == null || module.hardHide.get()) return;

        if (time <= 0) {
            tryReconnect();
        } else {
            time--;
            if (reconnectButton != null) {
                reconnectButton.setMessage(Text.literal(getReconnectText(module)));
            }
        }
    }

    @Unique
    private String getReconnectText(AutoReconnectModule module) {
        String text = "Reconnect";
        if (module != null && module.isEnabled()) {
            text += " " + String.format("(" + Formatting.WHITE + "%.1fs" + Formatting.RESET + ")", time / 20.0);
        }
        return text;
    }

    @Unique
    private String getToggleText(AutoReconnectModule module) {
        if (module == null) return Formatting.RED + "AutoReconnect";
        return (module.isEnabled() ? Formatting.WHITE : Formatting.RED) + "AutoReconnect";
    }

    @Unique
    private void tryReconnect() {
        if (LAST_CONNECTION == null) return;
        ConnectScreen.connect(
                new TitleScreen(),
                MinecraftClient.getInstance(),
                LAST_CONNECTION.getLeft(),
                LAST_CONNECTION.getRight(),
                false,
                null
        );
    }
}
