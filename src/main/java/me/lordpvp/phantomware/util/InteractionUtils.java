package me.lordpvp.phantomware.util;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.feature.module.impl.client.RotationModule;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.util.PacketUtils.sendSequencedPacket;
import static me.kiriyaga.nami.util.RotationUtils.*;
import static net.minecraft.util.Hand.MAIN_HAND;

public class InteractionUtils {

    private static BlockPos currentBreakingBlock = null;
    private static long lastAttackBlockTime = 0;

    public static boolean interactWithEntity(Entity entity, Vec3d hitVec, boolean swing) {
        if (MC.player == null || MC.interactionManager == null) return false;

        EntityHitResult hitResult = new EntityHitResult(entity, hitVec);
        ClientPlayerInteractionManager im = MC.interactionManager;

        im.interactEntityAtLocation(MC.player, entity, hitResult, MAIN_HAND);
        im.interactEntity(MC.player, entity, MAIN_HAND);

        if (swing)
            MC.player.swingHand(MAIN_HAND);

        return true;
    }

    public static void startUsingItem() {
        startUsingItem(MAIN_HAND);
    }

    public static void startUsingItem(Hand hand) {
        if (!MC.player.isUsingItem())
            MC.player.setCurrentHand(hand);
    }

    public static void stopUsingItem() {
        if (MC.player.isUsingItem())
            MC.player.stopUsingItem();
    }

    // TODO: figure out how to place on interactable blocks without manually sneaking

    public static boolean placeBlock(BlockPos pos, int slot,double range, boolean rotate, boolean strictDirection, boolean simulate, boolean swing, String rotationId) {
        if (!MC.world.getBlockState(pos).isReplaceable())
            return false;

        Direction direction = getDirection(pos);
        if (direction == null) {
            return false;
        }

        BlockPos neighbor = pos.offset(direction.getOpposite());
        Direction clickFace = direction;
        Vec3d hitVec = Vec3d.ofCenter(neighbor).add(Vec3d.of(clickFace.getVector()).multiply(0.5));

        // Simplified grim v2 PlacePosition check
        // we do not use all possible eye positions because its just unnecessary
        if (strictDirection) { // todo something while phased
            Vec3d eyePos = MC.player.getEyePos();

            boolean flag = switch (clickFace) { // https://github.com/GrimAnticheat/Grim/blob/fb926ab0fbca081ad765389c541880a4a435fabb/common/src/main/java/ac/grim/grimac/checks/impl/scaffolding/PositionPlace.java#L49
                case NORTH -> eyePos.z <= neighbor.getZ() + 1e-3;
                case SOUTH -> eyePos.z >= neighbor.getZ() + 1 - 1e-3;
                case WEST  -> eyePos.x <= neighbor.getX() + 1e-3;
                case EAST  -> eyePos.x >= neighbor.getX() + 1 - 1e-3;
                case DOWN  -> eyePos.y <= neighbor.getY() + 1e-3;
                case UP    -> eyePos.y >= neighbor.getY() + 1 - 1e-3;
                default -> false;
            };

            if (!flag) {
                return false;
            }
        }

        Vec3d eyePos = MC.player.getEyePos();
        Box blockBox = new Box(neighbor);
        Vec3d lookDir = getClosestPointToEye(eyePos, blockBox).subtract(eyePos).normalize();
        Vec3d reachEnd = eyePos.add(lookDir.multiply(range));

        if (blockBox.raycast(eyePos, reachEnd).isEmpty())
            return false;

        BlockHitResult hitResult = new BlockHitResult(hitVec, clickFace, neighbor, false);
        boolean canPlace = true;

        if (rotate) {
            float yaw = (float) getYawToVec(MC.player, hitVec);
            float pitch = (float) getPitchToVec(MC.player, hitVec);

            if (getDefaultRotationMode() == RotationModule.RotationMode.SILENT)
                ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(rotationId, 8, yaw, pitch));
            else
                ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(rotationId, 8, MC.player, hitVec));

            canPlace = ROTATION_MANAGER.getRequestHandler().isCompleted(rotationId);
        }

        boolean result = false;
        if (canPlace) {
            int prev = MC.player.getInventory().getSelectedSlot();
            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(slot);

            if (simulate)
                MC.interactionManager.interactBlock(MC.player, MAIN_HAND, hitResult);
            else
                sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(MAIN_HAND, hitResult, id));

            if (swing)
                MC.player.swingHand(MAIN_HAND);

            result = true;

            INVENTORY_MANAGER.getSlotHandler().attemptSwitch(prev);
        }

        return result;
    }

    public static Direction getDirection(BlockPos blockPos) {
        for (final Direction direction : Direction.values()) {
            final BlockState state = MC.world.getBlockState(blockPos.offset(direction));
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                continue;
            }

            Direction opposite = direction.getOpposite();

            return opposite;
        }
        return null;
    }

    private static RotationModule.RotationMode getDefaultRotationMode() {
        RotationModule module = MODULE_MANAGER.getStorage().getByClass(RotationModule.class);
        return module != null ? module.rotation.get() : RotationModule.RotationMode.MOTION;
    }

    public static void airPlace(BlockHitResult target, boolean grim, boolean swing) {
        if (grim) {
            MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));

            MC.interactionManager.interactBlock(MC.player, Hand.OFF_HAND, target);
            if (swing)
                MC.player.swingHand(Hand.MAIN_HAND, false);

            MC.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.OFF_HAND));
            MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
        } else {
            MC.interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, target);
            if (swing)
                MC.player.swingHand(Hand.MAIN_HAND);
        }
    }

    public static boolean breakBlock(BlockPos pos, double range, boolean rotate, boolean swing, boolean grim, String rotationId) {
        if (MC.player == null || MC.interactionManager == null)
            return false;

        if (isBlockAirOrFluid(pos)) {
            if (currentBreakingBlock != null && currentBreakingBlock.equals(pos)) {
                currentBreakingBlock = null;
            }
            return false;
        }

        Vec3d eyePos = MC.player.getEyePos();
        Box blockBox = new Box(pos);
        Vec3d lookDir = getClosestPointToEye(eyePos, blockBox).subtract(eyePos).normalize();
        Vec3d reachEnd = eyePos.add(lookDir.multiply(range));

        if (blockBox.raycast(eyePos, reachEnd).isEmpty())
            return false;

        Direction direction = Direction.UP;

        if (rotate) {
            ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(
                    rotationId,
                    3,
                    (float) getYawToVec(MC.player, Vec3d.ofCenter(pos)),
                    (float) getPitchToVec(MC.player, Vec3d.ofCenter(pos))
            ));

            if (!ROTATION_MANAGER.getRequestHandler().isCompleted(rotationId)) {
                return false;
            }
        }

        long now = System.currentTimeMillis();

        if (currentBreakingBlock == null || !currentBreakingBlock.equals(pos)) {
            boolean instant = MC.world.getBlockState(pos).calcBlockBreakingDelta(MC.player, MC.world, pos) >= 1.0f;

            if (instant) {
                currentBreakingBlock = null;
                MC.interactionManager.attackBlock(pos, direction);
                if (swing) MC.player.swingHand(Hand.MAIN_HAND);
                return true;
            } else {
                long attackCooldown = grim ? 250 : 0;
                if (now - lastAttackBlockTime >= attackCooldown) {
                    currentBreakingBlock = pos;
                    MC.interactionManager.attackBlock(pos, direction);
                    lastAttackBlockTime = now;
                }
            }
        } else {
            boolean success = MC.interactionManager.updateBlockBreakingProgress(pos, direction);
            if (!success) {
                currentBreakingBlock = null;
                return false;
            }
        }

        if (swing) MC.player.swingHand(Hand.MAIN_HAND);
        return true;
    }

    private static boolean isBlockAirOrFluid(BlockPos pos) {
        if (MC.world.getBlockState(pos).isAir()) {
            return true;
        }
        FluidState fluidState = MC.world.getFluidState(pos);
        return !fluidState.isEmpty();
    }
}
