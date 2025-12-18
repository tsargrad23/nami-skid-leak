package me.kiriyaga.nami.feature.module.impl.combat;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.PacketUtils.sendSequencedPacket;

@RegisterModule
public class AutoBowReleaseModule extends Module {
    public enum TpsMode {NONE, LATEST, AVERAGE}

    private final IntSetting ticks = addSetting(new IntSetting("Delay", 3, 0, 25));
    private final EnumSetting<TpsMode> tpsMode = addSetting(new EnumSetting<>("TPS", TpsMode.NONE));

    private float ticker = 0f;

    public AutoBowReleaseModule() {
        super("AutoBowRelease", "Automatically releases bow after holding for a set time.", ModuleCategory.of("Combat"), "autbowrelease");
    }

    @Override
    public void onEnable() {
        ticker = 0f;
    }

    @SubscribeEvent
    public void onTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null || !MC.player.isUsingItem()) return;

        Item usedItem = MC.player.getActiveItem().getItem();
        if (usedItem != Items.BOW && usedItem != Items.TRIDENT) return;

        float tps = switch (tpsMode.get()) {
            case LATEST -> TICK_MANAGER.getLatestTPS();
            case AVERAGE -> TICK_MANAGER.getAverageTPS();
            default -> 20.0f;
        };

        ticker += tps / 20.0f;

        if (ticker >= ticks.get()) {
            ticker = 0f;

            MC.getNetworkHandler().sendPacket(
                    new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN)
            );
            MC.player.stopUsingItem();
            sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(MC.player.getActiveHand(), id, ROTATION_MANAGER.getStateHandler().getServerYaw(), ROTATION_MANAGER.getStateHandler().getServerPitch()));

        }
    }
}