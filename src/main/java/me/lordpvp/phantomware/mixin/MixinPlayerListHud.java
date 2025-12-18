package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.miscellaneous.BetterTabModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;

import static me.kiriyaga.nami.Nami.FRIEND_MANAGER;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(PlayerListHud.class)
public abstract class MixinPlayerListHud {

    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private static Comparator<PlayerListEntry> ENTRY_ORDERING;

    @Shadow protected abstract Text applyGameModeFormatting(PlayerListEntry entry, MutableText name);

    private final Set<String> cachedFriends = new HashSet<>();
    private long lastFriendCacheUpdate = 0;
    private final long friendCacheInterval = 1000;

    @Inject(method = "collectPlayerEntries", at = @At("HEAD"), cancellable = true)
    private void collectPlayerEntries(CallbackInfoReturnable<List<PlayerListEntry>> info) {
        BetterTabModule betterTab = MODULE_MANAGER.getStorage() != null ? MODULE_MANAGER.getStorage().getByClass(BetterTabModule.class) : null;
        if (betterTab == null || !betterTab.isEnabled()) return;
        if (client == null || client.player == null || client.player.networkHandler == null) return;

        Collection<PlayerListEntry> allEntries = client.player.networkHandler.getListedPlayerListEntries();

        List<PlayerListEntry> result;

        if (betterTab.friendsOnly.get()) {
            long now = System.currentTimeMillis();
            if (now - lastFriendCacheUpdate > friendCacheInterval) {
                cachedFriends.clear();
                FRIEND_MANAGER.getFriends().forEach(friend -> cachedFriends.add(friend.toLowerCase()));
                lastFriendCacheUpdate = now;
            }

            int limit = betterTab.limit.get();
            result = new ArrayList<>(limit);

            for (PlayerListEntry entry : allEntries) {
                String name = entry.getProfile().getName().toLowerCase();
                if (cachedFriends.contains(name)) {
                    result.add(entry);
                    if (result.size() >= limit) break;
                }
            }

            result.sort(ENTRY_ORDERING);

        } else {
            result = allEntries.stream()
                    .limit(betterTab.limit.get())
                    .sorted(ENTRY_ORDERING)
                    .collect(Collectors.toList());
        }

        info.setReturnValue(result);
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    private void getPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> info) {
        BetterTabModule betterTab = MODULE_MANAGER.getStorage() != null ? MODULE_MANAGER.getStorage().getByClass(BetterTabModule.class) : null;
        if (betterTab == null || !betterTab.isEnabled()) return;

        boolean highlightFriends = betterTab.highlighFriends.get();
        String playerName = entry.getProfile().getName();
        boolean isFriend = FRIEND_MANAGER.isFriend(playerName);

        if (highlightFriends && isFriend) {
            MutableText formattedName = Text.empty();

            if (entry.getDisplayName() != null) {
                for (Text sibling : entry.getDisplayName().getSiblings()) {
                    String str = sibling.getString();
                    if (str.equals(playerName)) {
                        formattedName.append(Text.literal(playerName).formatted(Formatting.AQUA));
                    } else if (str.equals("] " + playerName)) {
                        formattedName.append(Text.literal("] ").formatted(Formatting.WHITE))
                                .append(Text.literal(playerName).formatted(Formatting.AQUA));
                    } else {
                        formattedName.append(sibling);
                    }
                }
            } else {
                formattedName = Team.decorateName(entry.getScoreboardTeam(), Text.literal(playerName).formatted(Formatting.AQUA));
            }

            info.setReturnValue(applyGameModeFormatting(entry, formattedName));
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void render(DrawContext drawContext, int width, Scoreboard scoreboard, @Nullable ScoreboardObjective objective, CallbackInfo ci) {
        BetterTabModule betterTab = MODULE_MANAGER.getStorage().getByClass(BetterTabModule.class);
        if (betterTab != null && betterTab.isEnabled()) {
            float scale = betterTab.scale.get().floatValue();

            drawContext.getMatrices().pushMatrix();

            float centerX = width / 2f;
            float centerY = 10f;

            drawContext.getMatrices().translate(centerX, centerY);
            drawContext.getMatrices().scale(scale, scale);
            drawContext.getMatrices().translate(-centerX, -centerY);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render2(DrawContext drawContext, int width, Scoreboard scoreboard, @Nullable ScoreboardObjective objective, CallbackInfo ci) {
        BetterTabModule betterTab = MODULE_MANAGER.getStorage().getByClass(BetterTabModule.class);
        if (betterTab != null && betterTab.isEnabled()) {
            drawContext.getMatrices().popMatrix();
        }
    }
}
