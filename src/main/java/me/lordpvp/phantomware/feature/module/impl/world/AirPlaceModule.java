package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.awt.*;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;
import static me.kiriyaga.nami.util.InteractionUtils.airPlace;

@RegisterModule
public class AirPlaceModule extends Module {

    private final DoubleSetting range = addSetting(new DoubleSetting("Range", 3.0, 2.0, 7.0));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 4, 1, 10));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    private final BoolSetting grim = addSetting(new BoolSetting("Grim", false));
    private final BoolSetting airOnly = addSetting(new BoolSetting("AirOnly", false));

    public int cooldown = 0;
    private BlockPos renderPos = null;

    public AirPlaceModule() {
        super("AirPlace", "Allows placing blocks mid-air.", ModuleCategory.of("World"), "airplace");
    }

    @Override
    public void onDisable() {
        cooldown = 0;
        renderPos = null;
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null || MC.interactionManager == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (!(MC.crosshairTarget instanceof BlockHitResult hit) || !checkPosReplaceableOrAir(hit.getBlockPos())) {
            renderPos = null;
            return;
        }

        ItemStack mainStack = MC.player.getMainHandStack();
        if (mainStack.isEmpty() || !(mainStack.getItem() instanceof BlockItem)){
            renderPos = null;
            return;
        }

        HitResult ray = MC.player.raycast(range.get(), 1.0f, !airOnly.get());
        if (!(ray instanceof BlockHitResult target)) {
            renderPos = null;
            return;
        }

        BlockPos targetPos = BlockPos.ofFloored(target.getPos());
        if (!checkPosReplaceableOrAir(targetPos) || hasEntity(targetPos)) {
            renderPos = null;
            return;
        }

        renderPos = targetPos;

        if (!MC.options.useKey.isPressed()){
            return;
        }
        cooldown = delay.get();

        airPlace(target, grim.get(), swing.get());
    }

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MC.player == null || MC.world == null || renderPos == null) return;

        if (!checkPosReplaceableOrAir(renderPos)) return;
        if (hasEntity(renderPos)) return;

        MatrixStack matrices = event.getMatrices();

        ColorModule colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        Color color = colorModule.getStyledGlobalColor();
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 60);

        Box box = new Box(renderPos);

        RenderUtil.drawBox(matrices, box, fillColor, color, 1.5f, true, true);
    }

    private boolean hasEntity(BlockPos pos) {
        for (Entity entity : MC.world.getEntities()) {
            if (entity.getBoundingBox().intersects(new Box(pos))) return true;
        }
        return false;
    }

    private boolean checkPosReplaceableOrAir(BlockPos pos) {
        BlockState state = MC.world.getBlockState(pos);
        if (airOnly.get()) {
            return state.isAir();
        } else {
            return state.isReplaceable();
        }
    }
}