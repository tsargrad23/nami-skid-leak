package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.ColorModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.Arrays;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InteractionUtils.placeBlock;

@RegisterModule
public class ScaffoldModule extends Module {

    public final DoubleSetting range = addSetting(new DoubleSetting("Range", 3.00, 1.0, 6.0));
    private final IntSetting delay = addSetting(new IntSetting("Delay", 0, 0, 5));
    private final IntSetting shiftTicks = addSetting(new IntSetting("ShiftTicks", 1, 1, 8));
    private final BoolSetting rotate = addSetting(new BoolSetting("Rotate", true));
    private final BoolSetting strictDirection = addSetting(new BoolSetting("StrictDirection", false));
    private final BoolSetting simulate = addSetting(new BoolSetting("Simulate", false));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", false));
    public final WhitelistSetting whitelist = addSetting(new WhitelistSetting("WhiteList", false, WhitelistSetting.Type.BLOCK));
    private final BoolSetting render = addSetting(new BoolSetting("Render", false));

    private int cooldown = 0;
    private BlockPos renderPos = null;

    public ScaffoldModule() {
        super("Scaffold", "Automatically scaffolds using specified blocks.", ModuleCategory.of("World"));
    }

    @Override
    public void onDisable() {
        cooldown = 0;
        renderPos = null;
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent event) {
        if (MC.player == null || MC.world == null) {
            cooldown = 0;
            renderPos = null;
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            renderPos = null;
            return;
        }
        BlockPos[] corners = getPlacements();
        int blocksPlaced = 0;
        int slot = getSlot();
        if (slot == -1) {
            renderPos = null;
            return;
        }

        renderPos = null;
        for (BlockPos pos : corners) {
            BlockPos targetPos = pos.down();

            if (hasEntity(targetPos))
                continue;

            renderPos = targetPos;

            if (placeBlock(targetPos, slot, range.get(), rotate.get(), strictDirection.get(), simulate.get(), swing.get(), this.name))
                blocksPlaced++;

            if (blocksPlaced >= shiftTicks.get()) break;
        }

        if (blocksPlaced > 0) cooldown = delay.get();
    }

    @SubscribeEvent
    public void onRender(Render3DEvent event) {
        if (MC.player == null || MC.world == null || renderPos == null || !render.get()) return;

        MatrixStack matrices = event.getMatrices();

        ColorModule colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);
        Color color = colorModule.getStyledGlobalColor();
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 60);

        Box box = new Box(renderPos);

        RenderUtil.drawBox(matrices, box, fillColor, color, 1.5f, true, true);
    }

    private int getSlot() {
        int selectedSlot = MC.player.getInventory().getSelectedSlot();

        if (!MC.player.getInventory().getStack(selectedSlot).isEmpty()) {
            Block block = Block.getBlockFromItem(MC.player.getInventory().getStack(selectedSlot).getItem());
            if (block != Blocks.AIR) {
                Identifier blockId = Registries.BLOCK.getId(block);
                if (!whitelist.get() || whitelist.isWhitelisted(blockId)) {
                    return selectedSlot;
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            if (MC.player.getInventory().getStack(i).isEmpty()) continue;

            Block block = Block.getBlockFromItem(MC.player.getInventory().getStack(i).getItem());
            if (block == Blocks.AIR) continue;

            Identifier blockId = Registries.BLOCK.getId(block);
            if (whitelist.get() && !whitelist.isWhitelisted(blockId)) continue;

            return i;
        }

        return -1;
    }

    private BlockPos[] getPlacements() {
        double minX = MC.player.getBoundingBox().minX;
        double maxX = MC.player.getBoundingBox().maxX;
        double minZ = MC.player.getBoundingBox().minZ;
        double maxZ = MC.player.getBoundingBox().maxZ;
        int y = (int) Math.floor(MC.player.getY());

        BlockPos[] possiblePositions = new BlockPos[] {new BlockPos((int) Math.floor(minX), y, (int) Math.floor(minZ)), new BlockPos((int) Math.floor(minX), y, (int) Math.floor(maxZ)), new BlockPos((int) Math.floor(maxX), y, (int) Math.floor(minZ)), new BlockPos((int) Math.floor(maxX), y, (int) Math.floor(maxZ))};

        return Arrays.stream(possiblePositions)
                .filter(pos -> MC.world.getBlockState(pos.down()).isAir()) // yes i know its cringe sorry
                .toArray(BlockPos[]::new);
    }

    private boolean hasEntity(BlockPos pos) {
        for (Entity entity : MC.world.getEntities()) {
            if (entity.squaredDistanceTo(MC.player) > 100) continue;

            if (entity.getBoundingBox().intersects(new Box(pos))) {
                return true;
            }
        }
        return false;
    }
}