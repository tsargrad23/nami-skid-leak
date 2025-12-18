package me.lordpvp.phantomware.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import me.kiriyaga.nami.Nami;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(value = ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private TextFieldWidget textField;
    @Shadow private ParseResults<CommandSource> parse;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow private ChatInputSuggestor.SuggestionWindow window;
    @Shadow private boolean completingSuggestions;

    @Shadow
    protected abstract void showCommandSuggestions();

    @Inject(
        method = "refresh",
        at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false),
        cancellable = true
    )
    private void onRefresh(CallbackInfo ci, @Local StringReader reader) {
        String text = this.textField.getText();
        String prefix = Nami.COMMAND_MANAGER.getExecutor().getPrefix();

        if (text.startsWith(prefix) && reader.getCursor() == 0) {
            reader.setCursor(prefix.length());

            CommandSource source = this.client.getNetworkHandler().getCommandSource();
            this.parse = Nami.COMMAND_MANAGER.getSuggester().getDispatcher().parse(reader, source);

            int cursor = this.textField.getCursor();
            if (cursor >= prefix.length() && (this.window == null || !this.completingSuggestions)) {
                this.pendingSuggestions = Nami.COMMAND_MANAGER.getSuggester().getDispatcher().getCompletionSuggestions(this.parse, cursor);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.showCommandSuggestions();
                    }
                });
            }
            ci.cancel();
        }
    }
}
