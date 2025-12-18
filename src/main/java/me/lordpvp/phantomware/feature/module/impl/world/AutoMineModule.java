package me.kiriyaga.nami.feature.module.impl.world;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PacketReceiveEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.event.impl.StartBreakingBlockEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.EnchantmentUtils;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.awt.*;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.InventoryUtils.isBroken;
import static me.kiriyaga.nami.util.PacketUtils.sendSequencedPacket;
import static me.kiriyaga.nami.util.RotationUtils.*;

@RegisterModule
public class AutoMineModule extends Module {
    public enum Rotate { NORMAL, HOLD, NONE}
    public enum Swap { NONE, NORMAL, SILENT121, SILENT}
    public enum EchestPriority {FORTUNE, SILK}

    private final DoubleSetting range = addSetting(new DoubleSetting("Range", 4.5, 2.0, 7.0));
    private final DoubleSetting speed = addSetting(new DoubleSetting("Speed", 1.0, 0.7, 1.0));
    public final EnumSetting<Swap> swap = addSetting(new EnumSetting<>("Swap", Swap.NORMAL));
    public final EnumSetting<Rotate> rotate = addSetting(new EnumSetting<>("Rotate", Rotate.NORMAL));
    private final BoolSetting grim = addSetting(new BoolSetting("Grim", false));
    private final BoolSetting instant = addSetting(new BoolSetting("Instant", true));
    private final BoolSetting swing = addSetting(new BoolSetting("Swing", false));
    private final BoolSetting async = addSetting(new BoolSetting("Async", true));
    public final EnumSetting<EchestPriority> echestPriority = addSetting(new EnumSetting<>("Echest", EchestPriority.SILK));
    private final IntSetting damageThreshold = addSetting(new IntSetting("Durability", 3, 0, 15));


    private BlockBreakingTask currentTask;
    private BlockBreakingTask doubleMineTask;

    private int shouldSwapBack = -1;

    // Thats first packet mine i made like in my whole life, its bad, and there is issues, im gonna finish it, and maybe rewrite from scratch later
    public AutoMineModule() {
        super("AutoMine", "Automatically mines specified blocks for easier mining.", ModuleCategory.of("World"));
        echestPriority.setShowCondition(()-> swap.get() != Swap.NONE);
        damageThreshold.setShowCondition(()-> swap.get() != Swap.NONE);
    }

    @Override
    public void onDisable() {
        if (currentTask != null && currentTask.isStarted()) {
            abortMining(currentTask);
        }
        currentTask = null;
        doubleMineTask = null;
        shouldSwapBack = -1;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onTick(PreTickEvent event) {
        if (MC.world == null || MC.player == null)
            return;

        if (shouldSwapBack != -1)
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(shouldSwapBack);

        shouldSwapBack = -1;

        if (currentTask != null)
            handleMiningTick(currentTask);

        if (doubleMineTask != null)
            handleDoubleMine(doubleMineTask);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockStartBreak(StartBreakingBlockEvent event) {
        BlockState state = MC.world.getBlockState(event.blockPos);

        if (state.getBlock().getHardness() == -1.0f || state.isAir()) {
            return;
        }

        event.cancel();

        if (swing.get())
            MC.player.swingHand(Hand.MAIN_HAND);

        if (currentTask != null) {
            if (currentTask.getBlockPos().equals(event.blockPos)) return;

            if (doubleMineTask == null) {
                doubleMineTask = new BlockBreakingTask(currentTask.getBlockPos(), currentTask.getFacing(), 1.0f);
                doubleMineTask.setProgress(currentTask.getProgress());
            }
            abortMining(currentTask);
        }

        currentTask = new BlockBreakingTask(event.blockPos, event.direction, speed.get().floatValue());
        startMining(currentTask);

        float damageDelta = calculateBlockDamage(currentTask.getBlockState(), MC.world, currentTask.getBlockPos());
        if (damageDelta >= 0.100f)
            finishMining(currentTask);
    }

    @SubscribeEvent
    public void onRender3DEvent(Render3DEvent event) {
        if (currentTask != null)
            renderProgress(event,currentTask);

        if (doubleMineTask != null)
            renderProgress(event,doubleMineTask);
    }

    private void renderProgress(Render3DEvent event, BlockBreakingTask task) {
        BlockPos pos = task.getBlockPos();
        VoxelShape shape = task.isInstantRemine() ? VoxelShapes.fullCube() : task.getBlockState().getOutlineShape(MC.world, pos);

        if (shape.isEmpty()) shape = VoxelShapes.fullCube();

        Box bb = shape.getBoundingBox();
        Box worldBox = new Box(
                pos.getX() + bb.minX, pos.getY() + bb.minY, pos.getZ() + bb.minZ,
                pos.getX() + bb.maxX, pos.getY() + bb.maxY, pos.getZ() + bb.maxZ
        );

        Vec3d center = worldBox.getCenter();

        float partialTicks = event.getTickDelta();
        float currentProgress = task.getProgress();
        float previousProgress = task.getPreviousProgress();
        float interpolatedProgress = previousProgress + (currentProgress - previousProgress) * partialTicks;

        float scale = MathHelper.clamp(interpolatedProgress / task.getTargetSpeed(), 0, 1.0f);

        double dx = (bb.maxX - bb.minX) / 2.0;
        double dy = (bb.maxY - bb.minY) / 2.0;
        double dz = (bb.maxZ - bb.minZ) / 2.0;

        Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);

        float t = MathHelper.clamp((scale - 0.5f) * 2f, 0f, 1f);
        int maxColor = 200;
        int r = (int) (maxColor * (1 - t));
        int g = (int) (maxColor * t);
        int b = 0;

        Color fillColor = new Color(r, g, b, 60);
        Color outlineColor = new Color(r, g, b, 255);

        RenderUtil.drawBox(event.getMatrices(), scaled, fillColor, outlineColor, 1.5, true, true);
    }

    private void handleMiningTick(BlockBreakingTask task) {
        Vec3d eyePos = MC.player.getEyePos();
        Box blockBox = new Box(task.getBlockPos());
        Vec3d lookDir = getClosestPointToEye(eyePos, blockBox).subtract(eyePos).normalize();
        Vec3d reachEnd = eyePos.add(lookDir.multiply(range.get()));
        boolean insideBox = blockBox.contains(eyePos);

        if (!insideBox && blockBox.raycast(eyePos, reachEnd).isEmpty()) {
            abortMining(task);
            currentTask = null;
            return;
        }

        if (task.getBlockState().isAir()) {
            if (instant.get()) {
                task.markInstantRemine();
                task.setProgress(1.0f);
            } else
                task.resetProgress();
            return;
        }

        if (swing.get())
            MC.player.swingHand(Hand.MAIN_HAND);

        if (rotate.get() == Rotate.HOLD)
            ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(this.name, 8, getYawToVec(MC.player, getClosestPointToEye(eyePos, blockBox)), getPitchToVec(MC.player, getClosestPointToEye(eyePos, blockBox))));


        float damageDelta = calculateBlockDamage(task.getBlockState(), MC.world, task.getBlockPos());
        if (task.incrementProgress(damageDelta) >= task.getTargetSpeed() || task.isInstantRemine()) {
            finishMining(task);
        }
    }

    private void handleDoubleMine(BlockBreakingTask task) {
        Vec3d eyePos = MC.player.getEyePos();
        Box blockBox = new Box(task.getBlockPos());
        Vec3d lookDir = getClosestPointToEye(eyePos, blockBox).subtract(eyePos).normalize();
        Vec3d reachEnd = eyePos.add(lookDir.multiply(range.get()));
        boolean insideBox = blockBox.contains(eyePos);

        if (!insideBox && blockBox.raycast(eyePos, reachEnd).isEmpty()) {
            doubleMineTask = null;
            return;
        }

        if (task.getBlockState().isAir()) {
            doubleMineTask = null;
            return;
        }

        float damageDelta = calculateBlockDamage(task.getBlockState(), MC.world, task.getBlockPos());
        int prev = MC.player.getInventory().getSelectedSlot();

        if (task.incrementProgress(damageDelta) >= task.getTargetSpeed()) {
            if (swap.get() == Swap.SILENT121 || swap.get() == Swap.SILENT) {
                int slot = getSlot(task.getBlockState());
                if (slot == MC.player.getInventory().getSelectedSlot())
                    return;

                shouldSwapBack = MC.player.getInventory().getSelectedSlot();
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(slot);
            }
        }

        if (swap.get() == Swap.SILENT) {
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(prev);
            shouldSwapBack = -1;
        }
    }

    private void startMining(BlockBreakingTask task) {
        if (task.getBlockState().isAir()) return;

        if (swap.get() == Swap.NORMAL)
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(getSlot(task.getBlockState()));

        if (grim.get())
            sendDestroyPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, task);

        sendDestroyPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, task);
        sendDestroyPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, task);

        task.markStarted();
    }

    private void abortMining(BlockBreakingTask task) {
        if (!task.isStarted() || task.getBlockState().isAir() || task.isInstantRemine() || task.getProgress() >= 1.0f)
            return;

        if (grim.get())
            sendDestroyPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, task);

        if (swing.get())
            MC.player.swingHand(Hand.MAIN_HAND);

        sendDestroyPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, task);
    }

    private void finishMining(BlockBreakingTask task) {
        if (!task.isStarted() || task.getBlockState().isAir()) return;

        if (currentTask.lastBrokenCount == currentTask.brokenCount && !async.get())
            return;

        Vec3d eyePos = MC.player.getEyePos();
        Box blockBox = new Box(task.getBlockPos());

        if (rotate.get() == Rotate.NORMAL)
            ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(this.name, 8, getYawToVec(MC.player, getClosestPointToEye(eyePos, blockBox)), getPitchToVec(MC.player, getClosestPointToEye(eyePos, blockBox))));

        if (rotate.get() == Rotate.NORMAL && !ROTATION_MANAGER.getRequestHandler().isCompleted(this.name))
            return;

        int prev = MC.player.getInventory().getSelectedSlot();
        if (swap.get() == Swap.SILENT121 || swap.get() == Swap.SILENT) {
            int slot = getSlot(task.getBlockState());
            if (slot != MC.player.getInventory().getSelectedSlot()) {
                if (currentTask.brokenCount < 2 || !currentTask.isInstantRemine())
                    shouldSwapBack = MC.player.getInventory().getSelectedSlot();
                INVENTORY_MANAGER.getSlotHandler().attemptSwitch(slot);
            }
        }

        if (grim.get())
            sendDestroyPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, task);

        if (swing.get())
            MC.player.swingHand(Hand.MAIN_HAND);

        sendDestroyPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, task);

        if (swap.get() == Swap.SILENT121 && currentTask.isInstantRemine() && currentTask.brokenCount >= 2) {
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(prev);

        }

        if (swap.get() == Swap.SILENT) {
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(prev);
            shouldSwapBack = -1;
        }
            currentTask.markLastBroken();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (currentTask == null) return;

        if (event.getPacket() instanceof BlockUpdateS2CPacket blockPacket) {
            BlockPos pos = blockPacket.getPos();

            if (pos.equals(currentTask.getBlockPos())) {
                currentTask.markBroken();
            }
        }
    }



    private void sendDestroyPacket(PlayerActionC2SPacket.Action action, BlockBreakingTask task) {
        sendSequencedPacket(id -> new PlayerActionC2SPacket(action, task.getBlockPos(), task.getFacing(), id));
    }

    private float calculateBlockDamage(BlockState state, BlockView world, BlockPos pos) {
        float hardness = state.getHardness(world, pos);
        if (hardness == -1.0f) return 0.0f;

        int divisor = canHarvest(state) ? 30 : 100;
        return getMiningSpeed(state) / hardness / divisor;
    }

    private boolean canHarvest(BlockState state) {
        if (state.isToolRequired()) {
            ItemStack held = MC.player.getMainHandStack();
            if (swap.get() == Swap.SILENT121 || swap.get() == Swap.SILENT) {
                held = MC.player.getInventory().getStack(getSlot(state));
            }
                return held.isSuitableFor(state);
        }
        return true;
    }

    private int getSlot(BlockState targetState) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = MC.player.getInventory().getStack(slot);
            if (stack.isEmpty() || isBroken(stack, damageThreshold.get()))continue;

            boolean matchesPriority = switch (echestPriority.get()) {
                case SILK -> EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.SILK_TOUCH) > 0;
                case FORTUNE -> EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.FORTUNE) > 0;
            };

            if (matchesPriority) return slot;
        }

        int bestSlot = MC.player.getInventory().getSelectedSlot();
        float bestSpeed = 1.0f;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = MC.player.getInventory().getStack(slot);
            if (stack.isEmpty() || isBroken(stack, damageThreshold.get())) continue;

            float speed = getToolSpeed(stack, targetState);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = slot;
            }
        }

        return bestSlot;
    }

    private float getToolSpeed(ItemStack stack, BlockState state) {
        if (!stack.isSuitableFor(state)) return 1.0f;

        float efficiency = EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);
        return stack.getMiningSpeedMultiplier(state) * (1 + efficiency * 0.2f);
    }

    private float getMiningSpeed(BlockState state) {
        ItemStack stack = MC.player.getMainHandStack();

        if (swap.get() == Swap.SILENT121 || swap.get() == Swap.SILENT)
            stack = MC.player.getInventory().getStack(getSlot(state));

        float speed = stack.getMiningSpeedMultiplier(state);

        if (speed > 1.0f) {
            int level = EnchantmentUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);
            if (level > 0 && !stack.isEmpty()) {
                speed += (level * level + 1);
            }
        }

        if (StatusEffectUtil.hasHaste(MC.player)) {
            int amplifier = StatusEffectUtil.getHasteAmplifier(MC.player) + 1;
            speed *= 1.0f + amplifier * 0.2f;
        }

        if (MC.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float multiplier = switch (MC.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 0.00081f;
            };
            speed *= multiplier;
        }

        boolean noAquaAffinity = EnchantmentUtils.getEnchantmentLevel(
                MC.player.getEquippedStack(EquipmentSlot.HEAD), Enchantments.AQUA_AFFINITY) == 0;
        if (MC.player.isSubmergedIn(FluidTags.WATER) && noAquaAffinity) {
            speed /= 5.0f;
        }

        if (!MC.player.isOnGround()) {
            speed /= 5.0f;
        }

        return speed;
    }

    public static class BlockBreakingTask {
        private final BlockPos blockPos;
        private final Direction facing;
        private final float targetSpeed;

        private float progress;
        private float previousProgress;
        private boolean instantRemine;
        private boolean started;
        private int brokenCount;
        private int lastBrokenCount;

        public BlockBreakingTask(BlockPos pos, Direction face, float speed) {
            this.blockPos = pos;
            this.facing = face;
            this.targetSpeed = speed;
            brokenCount = 0;
            lastBrokenCount = -1;
        }

        public BlockPos getBlockPos() { return blockPos; }
        public Direction getFacing() { return facing; }
        public float getTargetSpeed() { return targetSpeed; }
        public BlockState getBlockState() { return MC.world.getBlockState(blockPos); }

        public boolean isStarted() { return started; }
        public void markStarted() { this.started = true; }

        public float getProgress() { return progress; }
        public float getPreviousProgress() { return previousProgress; }
        public float incrementProgress(float delta) {
            this.previousProgress = progress;
            return (progress += delta);
        }
        public void setProgress(float value) {
            this.previousProgress = progress;
            this.progress = value;
        }

        public void resetProgress() {
            this.progress = 0.0f;
            this.previousProgress = 0.0f;
            this.instantRemine = false;
        }

        public boolean isInstantRemine() { return instantRemine; }
        public void markInstantRemine() { this.instantRemine = true; }

        public int getBrokenCount() { return brokenCount; }
        public void markBroken() { brokenCount++; }

        public int getLastBrokenCount() { return lastBrokenCount; }
        public void markLastBroken() { lastBrokenCount = brokenCount; }

    }
}
