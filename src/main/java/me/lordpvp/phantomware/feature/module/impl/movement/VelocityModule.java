package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.core.rotation.model.RotationRequest;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.*;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.module.impl.client.RotationModule;
import me.kiriyaga.nami.feature.setting.impl.*;
import me.kiriyaga.nami.mixin.*;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;

import java.util.*;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class VelocityModule extends Module {

    private enum Mode { VANILLA, WALLS, GRIM }

    private final EnumSetting<Mode> mode = addSetting(new EnumSetting<>("Mode", Mode.WALLS));
    private final DoubleSetting horizontalPercent = addSetting(new DoubleSetting("Horizontal", 100.0, 0.0, 100.0));
    private final DoubleSetting verticalPercent = addSetting(new DoubleSetting("Vertical", 100.0, 0.0, 100.0));
    private final BoolSetting handleKnockback = addSetting(new BoolSetting("Knockback", true));
    private final BoolSetting handleExplosions = addSetting(new BoolSetting("Explosion", true));
    private final BoolSetting concealMotion = addSetting(new BoolSetting("Conceal", false));
    private final BoolSetting requireGround = addSetting(new BoolSetting("GroundOnly", false));
    private final BoolSetting cancelEntityPush = addSetting(new BoolSetting("EntityPush", true));
    private final BoolSetting cancelBlockPush = addSetting(new BoolSetting("BlockPush", true));
    private final BoolSetting cancelLiquidPush = addSetting(new BoolSetting("LiquidPush", true));
    private final BoolSetting cancelFishHook = addSetting(new BoolSetting("RodPush", false));

    private boolean pendingConcealment = false;
    private boolean pendingVelocity = false;

    public VelocityModule() {super("Velocity", "Reduces incoming velocity effects.", ModuleCategory.of("Movement"), "antiknockback");}

    @Override
    public void onEnable() {
        pendingVelocity = false;
    }

    @Override
    public void onDisable() {
        flushPendingVelocity();
        pendingConcealment = false;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreTick(PreTickEvent event) {
        flushPendingVelocity();
        pendingConcealment = false;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPacketReceive(PacketReceiveEvent event) {
        if (MC.player == null || MC.world == null) return;

        Packet<?> packet = event.getPacket();

        if (packet instanceof PlayerPositionLookS2CPacket && concealMotion.get()) {
            pendingConcealment = true;
        }

        if (packet instanceof EntityVelocityUpdateS2CPacket vel && handleKnockback.get()) {
            handleVelocityPacket(event, vel);
        } else if (packet instanceof ExplosionS2CPacket explosion && handleExplosions.get()) {
            handleExplosionPacket(event, explosion);
        } else if (packet instanceof BundleS2CPacket bundle) {
            handleBundlePacket(event, bundle);
        } else if (packet instanceof EntityStatusS2CPacket status
                && status.getStatus() == EntityStatuses.PULL_HOOKED_ENTITY
                && cancelFishHook.get()) {
            handleFishHookPacket(event, status);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onEntityPush(EntityPushEvent event) {
        if (cancelEntityPush.get() && event.getTarget().equals(MC.player)) {
            event.cancel();
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onBlockPush(BlockPushEvent event) {
        if (cancelBlockPush.get()) {
            event.cancel();
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onFluidPush(LiquidPushEvent event) {
        if (cancelLiquidPush.get()) {
            event.cancel();
        }
    }

    private void handleVelocityPacket(PacketReceiveEvent event, EntityVelocityUpdateS2CPacket packet) {
        if (packet.getEntityId() != MC.player.getId()) return;

        if (pendingConcealment && isZeroVelocity(packet)) {
            pendingConcealment = false;
            return;
        }

        switch (mode.get()) {
            case VANILLA -> processVelocityVanilla(event, packet);
            case WALLS -> processVelocityWalls(event, packet);
            case GRIM -> processVelocityGrim(event);
        }
    }

    private void handleExplosionPacket(PacketReceiveEvent event, ExplosionS2CPacket packet) {
        switch (mode.get()) {
            case VANILLA -> processExplosionVanilla(event, packet);
            case WALLS -> processExplosionWalls(event, packet);
            case GRIM -> processExplosionGrim(event);
        }
    }

    private void handleBundlePacket(PacketReceiveEvent event, BundleS2CPacket bundle) {
        List<Packet<?>> filtered = new ArrayList<>();

        for (Packet<?> packet : bundle.getPackets()) {
            if (packet instanceof ExplosionS2CPacket exp && handleExplosions.get()) {
                processBundleExplosion(filtered, exp);
            } else if (packet instanceof EntityVelocityUpdateS2CPacket vel && handleKnockback.get()) {
                processBundleVelocity(filtered, vel);
            } else {
                filtered.add(packet);
            }
        }

        ((BundlePacketAccessor) bundle).setIterable(filtered);
    }

    private void handleFishHookPacket(PacketReceiveEvent event, EntityStatusS2CPacket status) {
        Entity entity = status.getEntity(MC.world);
        if (entity instanceof FishingBobberEntity hook && hook.getHookedEntity() == MC.player) {
            event.cancel();
        }
    }

    private void processVelocityVanilla(PacketReceiveEvent event, EntityVelocityUpdateS2CPacket packet) {
        if (isNoVelocityConfigured()) {
            event.cancel();
        } else {
            scaleVelocityPacket(packet);
        }
    }

    private void processVelocityWalls(PacketReceiveEvent event, EntityVelocityUpdateS2CPacket packet) {
        if (!isPhased() || (requireGround.get() && !MC.player.isOnGround())) return;
        processVelocityVanilla(event, packet);
    }

    private void processVelocityGrim(PacketReceiveEvent event) {
        if (!FLAG_MANAGER.hasElapsedSinceSetback(100)) return;
        event.cancel();
        pendingVelocity = true;
    }

    private void processExplosionVanilla(PacketReceiveEvent event, ExplosionS2CPacket packet) {
        if (isNoVelocityConfigured()) {
            event.cancel();
        } else {
            scaleExplosionPacket(packet);
        }
    }

    private void processExplosionWalls(PacketReceiveEvent event, ExplosionS2CPacket packet) {
        if (!isPhased()) return;
        processExplosionVanilla(event, packet);
    }

    private void processExplosionGrim(PacketReceiveEvent event) {
        if (!FLAG_MANAGER.hasElapsedSinceSetback(100)) return;
        event.cancel();
        pendingVelocity = true;
    }

    private void processBundleExplosion(List<Packet<?>> filtered, ExplosionS2CPacket packet) {
        switch (mode.get()) {
            case VANILLA -> {
                if (!isNoVelocityConfigured()) scaleExplosionPacket(packet);
                else return;
            }
            case WALLS -> {
                if (!isPhased()) { filtered.add(packet); return; }
                if (!isNoVelocityConfigured()) scaleExplosionPacket(packet);
                else return;
            }
            case GRIM -> {
                if (!FLAG_MANAGER.hasElapsedSinceSetback(100)) { filtered.add(packet); return; }
                pendingVelocity = true;
                return;
            }
        }
        filtered.add(packet);
    }

    private void processBundleVelocity(List<Packet<?>> filtered, EntityVelocityUpdateS2CPacket packet) {
        if (packet.getEntityId() != MC.player.getId()) {
            filtered.add(packet);
            return;
        }

        switch (mode.get()) {
            case VANILLA -> {
                if (!isNoVelocityConfigured()) scaleVelocityPacket(packet);
                else return;
            }
            case WALLS -> {
                if (!isPhased() || (requireGround.get() && !MC.player.isOnGround())) {
                    filtered.add(packet);
                    return;
                }
                if (!isNoVelocityConfigured()) scaleVelocityPacket(packet);
                else return;
            }
            case GRIM -> {
                if (!FLAG_MANAGER.hasElapsedSinceSetback(100)) { filtered.add(packet); return; }
                pendingVelocity = true;
                return;
            }
        }

        filtered.add(packet);
    }

    private void flushPendingVelocity() {
        if (!pendingVelocity) return;
        if (mode.get() == Mode.GRIM) {
            sendRotationFix();
        }
        pendingVelocity = false;
    }

    private void sendRotationFix() { // somehow it happens, needs tests on grim v2 asap
        float yaw = ROTATION_MANAGER.getStateHandler().getServerYaw();
        float pitch = ROTATION_MANAGER.getStateHandler().getServerPitch();

        ROTATION_MANAGER.getRequestHandler().submit(new RotationRequest(this.name, 0, yaw, pitch, RotationModule.RotationMode.SILENT));
        MC.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                MC.player.isCrawling() ? MC.player.getBlockPos() : MC.player.getBlockPos().up(),
                Direction.DOWN
        ));
    }

    private boolean isZeroVelocity(EntityVelocityUpdateS2CPacket packet) {
        return packet.getVelocityX() == 0 && packet.getVelocityY() == 0 && packet.getVelocityZ() == 0;
    }

    private boolean isNoVelocityConfigured() {
        return horizontalPercent.get() == 0 && verticalPercent.get() == 0;
    }

    private boolean isPhased() {
        ClientPlayerEntity player = MC.player;
        if (player == null || MC.world == null) return false;

        Box box = player.getBoundingBox();
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.ceil(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    VoxelShape shape = MC.world.getBlockState(pos).getCollisionShape(MC.world, pos);
                    if (!shape.isEmpty() && shape.getBoundingBox().offset(pos).intersects(box)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void scaleVelocityPacket(EntityVelocityUpdateS2CPacket packet) {
        int scaledX = (int) (packet.getVelocityX() * (horizontalPercent.get() / 100.0));
        int scaledY = (int) (packet.getVelocityY() * (verticalPercent.get() / 100.0));
        int scaledZ = (int) (packet.getVelocityZ() * (horizontalPercent.get() / 100.0));

        ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityX(scaledX);
        ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityY(scaledY);
        ((EntityVelocityUpdateS2CPacketAccessor) packet).setVelocityZ(scaledZ);
    }

    private void scaleExplosionPacket(ExplosionS2CPacket packet) {
        ExplosionS2CPacketAccessor accessor = (ExplosionS2CPacketAccessor) (Object) packet;
        accessor.getPlayerKnockback().ifPresent(original -> {
            Vec3d scaled = new Vec3d(
                    original.x * (horizontalPercent.get() / 100.0),
                    original.y * (verticalPercent.get() / 100.0),
                    original.z * (horizontalPercent.get() / 100.0)
            );
            accessor.setPlayerKnockback(Optional.of(scaled));
        });
    }
}