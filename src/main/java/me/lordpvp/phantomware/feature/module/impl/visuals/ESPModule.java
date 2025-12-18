package me.kiriyaga.nami.feature.module.impl.visuals;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
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
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.NametagFormatter.*;

@RegisterModule
public class ESPModule extends Module {

    public static enum RenderMode {
        OUTLINE,
        BOX
    }

    public final BoolSetting showPlayers = addSetting(new BoolSetting("Players", true));
    public final BoolSetting showPeacefuls = addSetting(new BoolSetting("Peacefuls", true));
    public final BoolSetting showNeutrals = addSetting(new BoolSetting("Neutrals", false));
    public final BoolSetting showHostiles = addSetting(new BoolSetting("Hostiles", false));
    public final BoolSetting showItems = addSetting(new BoolSetting("Items", true));
    public final BoolSetting itemBoundingBox = addSetting(new BoolSetting("ItemBoundingBox", true));
    public final BoolSetting showMobSpawns = addSetting(new BoolSetting("MobSpawn", false));
    public final IntSetting mobSpawnLightThreshold = addSetting(new IntSetting("SpawnLight", 7, 0, 15));
    public final EnumSetting<RenderMode> renderMode = addSetting(new EnumSetting<>("Mode", RenderMode.OUTLINE));
    public final DoubleSetting outlineDistance = addSetting(new DoubleSetting("Distance", 52, 15, 256));
    public final BoolSetting smoothAppear = addSetting(new BoolSetting("Smooth", true));

    public ESPModule() {
        super("ESP", "Highlights certain entities.", ModuleCategory.of("Render"), "esp", "wh", "boxes");
        smoothAppear.setShowCondition(() -> (renderMode.get() == RenderMode.BOX || showItems.get() && itemBoundingBox.get()));
        outlineDistance.setShowCondition(() -> renderMode.get() == RenderMode.OUTLINE);
        itemBoundingBox.setShowCondition(() -> showItems.get());
        mobSpawnLightThreshold.setShowCondition(showMobSpawns::get);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (MC == null || MC.world == null || MC.player == null) return;

        this.setDisplayInfo(renderMode.get().toString());


        if (showMobSpawns.get())
            renderMob(event);

        if (renderMode.get() == RenderMode.BOX) {
            renderBoxes(event);
            return;
        }

        if (renderMode.get() == RenderMode.OUTLINE) {
            if (itemBoundingBox.get()) {
                renderItemBoxes(event);
            }
        }
    }

    private void renderBoxes(Render3DEvent event) {
        MatrixStack matrices = event.getMatrices();
        float partialTicks = event.getTickDelta();
        ColorModule colorModule = MODULE_MANAGER.getStorage().getByClass(ColorModule.class);

        for (Entity entity : getEntitiesToRender()) {
            if (entity.isRemoved() || !entity.isAlive()) continue;

            Color color = getColorForEntity(entity, colorModule);
            drawBox(entity, color, matrices, partialTicks);
        }
    }

    private Set<Entity> getEntitiesToRender() {
        Set<Entity> entities = new HashSet<>();

        if (showPlayers.get()) entities.addAll(ENTITY_MANAGER.getOtherPlayers());
        if (showPeacefuls.get()) entities.addAll(ENTITY_MANAGER.getPassive());
        if (showNeutrals.get()) entities.addAll(ENTITY_MANAGER.getNeutral());
        if (showHostiles.get()) entities.addAll(ENTITY_MANAGER.getHostile());
        if (showItems.get()) entities.addAll(ENTITY_MANAGER.getDroppedItems());

        if (renderMode.get() == RenderMode.OUTLINE) {
            double maxDistSq = outlineDistance.get() * outlineDistance.get();
            entities.removeIf(entity -> MC.player.squaredDistanceTo(entity) > maxDistSq);
        }

        return entities;
    }

    private Color getColorForEntity(Entity entity, ColorModule colorModule) {
        if (entity instanceof PlayerEntity) {
            return colorModule.getStyledGlobalColor();
        } else if (ENTITY_MANAGER.getPassive().contains(entity)) {
            return COLOR_PASSIVE;
        } else if (ENTITY_MANAGER.getNeutral().contains(entity)) {
            return COLOR_NEUTRAL;
        } else if (ENTITY_MANAGER.getHostile().contains(entity)) {
            return COLOR_HOSTILE;
        } else if (entity instanceof ItemEntity) {
            return COLOR_ITEM;
        }
        return Color.WHITE;
    }

    private void renderItemBoxes(Render3DEvent event) {
        MatrixStack matrices = event.getMatrices();
        float partialTicks = event.getTickDelta();

        for (Entity entity : ENTITY_MANAGER.getDroppedItems()) {
            if (!showItems.get() || entity.isRemoved() || !entity.isAlive()) continue;

            Color color = COLOR_ITEM;
            drawBox(entity, color, matrices, partialTicks);
        }
    }

    private void drawBox(Entity entity, Color color, MatrixStack matrices, float partialTicks) {
        double interpX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * partialTicks;
        double interpY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * partialTicks;
        double interpZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * partialTicks;

        Box box = entity.getBoundingBox().offset(
                interpX - entity.getX(),
                interpY - entity.getY(),
                interpZ - entity.getZ()
        );

        int alphaCoef = smoothAppear.get() ? Math.min(entity.age * 10, 255) : 255;

        int filledAlpha = Math.min(alphaCoef, 75);
        RenderUtil.drawBoxFilled(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), filledAlpha));
        RenderUtil.drawBox(matrices, box, new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaCoef), 1.5f);
    }

    public static Color getESPColor(Entity entity) {
        ESPModule esp = MODULE_MANAGER.getStorage().getByClass(ESPModule.class);
        if (esp == null || !esp.isEnabled()) return null;

        double d = MODULE_MANAGER.getStorage().getByClass(ESPModule.class).outlineDistance.get();

        if (MC.getCameraEntity().distanceTo(entity) > d)
            return null;

        if (entity == null || entity.isRemoved() || !entity.isAlive()) return null;

        if (entity instanceof PlayerEntity) {
            if (!esp.showPlayers.get()) return null;
            return MODULE_MANAGER.getStorage().getByClass(ColorModule.class).getStyledGlobalColor();
        }

        if (esp.showPeacefuls.get() && ENTITY_MANAGER.getPassive().contains(entity)) return COLOR_PASSIVE;
        if (esp.showNeutrals.get() && ENTITY_MANAGER.getNeutral().contains(entity)) return COLOR_NEUTRAL;
        if (esp.showHostiles.get() && ENTITY_MANAGER.getHostile().contains(entity)) return COLOR_HOSTILE;
        if (entity instanceof ItemEntity) {
            if (!esp.showItems.get()) return null;
            if (esp.itemBoundingBox.get()) return null;
            return COLOR_ITEM;
        }
        return null;
    }

    public static boolean canMobSpawn(BlockPos pos, int spawnLightLimit) {
        BlockState state = MC.world.getBlockState(pos);
        BlockState below = MC.world.getBlockState(pos.down());
        Block blockBelow = below.getBlock();

        boolean isSnowLayer = state.getBlock() instanceof SnowBlock && state.get(SnowBlock.LAYERS) == 1;
        if (!state.isAir() && !isSnowLayer) return false;
        if (blockBelow == Blocks.BEDROCK || blockBelow == Blocks.BARRIER || blockBelow instanceof TransparentBlock || blockBelow instanceof ScaffoldingBlock)
            return false;

        if (!(blockBelow == Blocks.SOUL_SAND || blockBelow == Blocks.MUD || (blockBelow instanceof SlabBlock && below.get(SlabBlock.TYPE) == SlabType.TOP) || (blockBelow instanceof StairsBlock && below.get(StairsBlock.HALF) == BlockHalf.TOP) || below.isOpaqueFullCube()))
            return false;

        int block = MC.world.getLightLevel(LightType.BLOCK, pos);
        //int sky = MC.world.getLightLevel(LightType.SKY, pos);

        return block <= spawnLightLimit;
    }

    private void renderMob(Render3DEvent event) {
        MatrixStack matrices = event.getMatrices();
        World world = MC.world;
        BlockPos playerPos = MC.player.getBlockPos();
        int lightLimit = mobSpawnLightThreshold.get();

        for (int x = -9; x <= 9; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -9; z <= 9; z++) {
                    BlockPos pos = playerPos.add(x, y, z);

                    if (canMobSpawn(pos, lightLimit)) {
                        int blockLight = world.getLightLevel(pos);

                        double renderX = pos.getX() + 0.5;
                        double renderY = pos.getY() + 1.0;
                        double renderZ = pos.getZ() + 0.5;
                        Vec3d camPos = MC.gameRenderer.getCamera().getPos();

                        float distance = (float) camPos.distanceTo(new Vec3d(renderX, renderY, renderZ));
                        int scale = 30;

                        float dynamicScale = 0.0018f + (scale / 10000.0f) * distance;
                        if (distance <= 8.0f) dynamicScale = 0.0245f;

                        Vec3d renderPos = new Vec3d(renderX, renderY, renderZ);
                        Text text = Text.of(String.valueOf(blockLight));
                        RenderUtil.drawText3D(matrices, text, renderPos, dynamicScale, false, false, 1);
                    }
                }
            }
        }
    }
}
