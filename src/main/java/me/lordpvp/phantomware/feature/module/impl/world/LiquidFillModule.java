package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.*;
import java.util.List;

import static me.kiriyaga.nami.util.InteractionUtils.airPlace;
import static me.kiriyaga.nami.util.RotationUtils.*;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class LiquidFillModule extends Module {

    public enum LiquidType {
        WATER, LAVA, BOTH
    }

    // TODO: shift ticks, or maybe not?
    private final DoubleSetting range = addSetting(new DoubleSetting("Range", 5.0, 1.0, 6.0));
    public final IntSetting delay = addSetting(new IntSetting("Delay", 4, 1, 10));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", true));
    private final BoolSetting grim = addSetting(new BoolSetting("Grim", false));
    private final EnumSetting<LiquidType> liquidType = addSetting(new EnumSetting<>("Liquid", LiquidType.BOTH));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));

    private int cooldown = 0;
    private BlockPos renderPos = null;

    public LiquidFillModule() {
        super("LiquidFill", "Automatically fills nearby liquids with blocks.", ModuleCategory.of("World"), "liquidfill");
    }

    @Override
    public void onDisable() {
        cooldown = 0;
        renderPos = null;
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null || MC.interactionManager == null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        int blockSlot = findBlockInHotbar();
        if (blockSlot == -1) {
            renderPos = null;
            return;
        }

        int r = (int) Math.ceil(range.get());
        BlockPos playerPos = MC.player.getBlockPos();

        List<BlockPos> positions = new ArrayList<>();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    positions.add(playerPos.add(x, y, z));
                }
            }
        }

        Vec3d playerVec = Vec3d.of(playerPos); // fucking why i need this
        positions.sort(Comparator.comparingDouble(pos -> Vec3d.of(pos).squaredDistanceTo(playerVec)));

        boolean placed = false;

        for (BlockPos pos : positions) {
            BlockState state = MC.world.getBlockState(pos);
            if (hasEntity(pos)) continue;

            boolean shouldPlace = switch (liquidType.get()) {
                case WATER -> state.getBlock() == Blocks.WATER && state.get(FluidBlock.LEVEL) == 0;
                case LAVA -> state.getBlock() == Blocks.LAVA && state.get(FluidBlock.LEVEL) == 0;
                case BOTH -> (state.getBlock() == Blocks.WATER && state.get(FluidBlock.LEVEL) == 0)
                        || (state.getBlock() == Blocks.LAVA && state.get(FluidBlock.LEVEL) == 0);
            };

            if (!shouldPlace) continue;

            renderPos = pos;

            if (rotate.get()) {
                ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(
                        LiquidFillModule.class.getName(),
                        3,
                        (float) getYawToVec(MC.player, Vec3d.of(pos)),
                        (float) getPitchToVec(MC.player, Vec3d.of(pos))
                ));
            }

            if (!rotate.get() || ROTATION_MANAGER.getRequestHandler().isCompleted(LiquidFillModule.class.getName())) {

                int currentSlot = MC.player.getInventory().getSelectedSlot();
                if (currentSlot != blockSlot)
                    INVENTORY_MANAGER.getSlotHandler().attemptSwitch(blockSlot);

                BlockHitResult hit = new BlockHitResult(Vec3d.of(pos).add(0.5,0.5,0.5), Direction.UP, pos, false);

                airPlace(hit, grim.get(), swing.get());

                if (currentSlot != MC.player.getInventory().getSelectedSlot())
                    INVENTORY_MANAGER.getSlotHandler().attemptSwitch(currentSlot);

                cooldown = delay.get();
                placed = true;
                break;
            }
        }

        if (!placed) renderPos = null;
    }

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MC.player == null || MC.world == null || renderPos == null) return;

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

    private int findBlockInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block != Blocks.AIR && block.getDefaultState().isOpaqueFullCube()) {
                    return i;
                }
            }
        }
        return -1;
    }
}