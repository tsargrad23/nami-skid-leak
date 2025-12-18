package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.BreakBlockEvent;
import me.kiriyaga.nami.event.impl.PlaceBlockEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class NoGlitchBlocksModule extends Module {

    private final BoolSetting place = addSetting(new BoolSetting("place", true));
    private final BoolSetting destroy = addSetting(new BoolSetting("destroy", true));
    private final BoolSetting swing = addSetting(new BoolSetting("swing", true));

    public NoGlitchBlocksModule() {
        super("NoGlitchBlocks", "Prevents ghost/place/destroy glitches.", ModuleCategory.of("World"), "noglitchblocks");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockPlace(PlaceBlockEvent event) {
        if (!place.get()) return;
        if (MC.isInSingleplayer()) return;
        if (!(MC.player.getStackInHand(event.getHand()).getItem() instanceof BlockItem)) return;

        event.cancel();

        PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(
                event.getHand(),
                event.getHitResult(),
                0
        );
        MC.getNetworkHandler().sendPacket(packet);
        if (swing.get())
            MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(event.getHand()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BreakBlockEvent event) {
        if (!destroy.get()) return;
        if (MC.isInSingleplayer()) return;

        event.cancel();
    }
}
