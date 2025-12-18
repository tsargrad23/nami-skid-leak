package me.kiriyaga.nami.core;

import me.kiriyaga.nami.Nami;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render2DEvent;
import me.kiriyaga.nami.feature.module.impl.client.TargetModule;
import me.kiriyaga.nami.util.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static me.kiriyaga.nami.Nami.*;

public class EntityManager {

    private int maxIdleTicks = 500;
    private int idleTicksCounter = 0;

    private List<Entity> allEntities = List.of();
    private List<PlayerEntity> players = List.of();
    private List<PlayerEntity> otherPlayers = List.of();
    private List<Entity> hostile = List.of();
    private List<Entity> neutral = List.of();
    private List<Entity> passive = List.of();
    private List<ItemEntity> droppedItems = List.of();
    private List<Entity> endCrystals = List.of();

    private TargetModule entityManagerModule = null;

    public void init() {
        Nami.EVENT_MANAGER.register(this);
    }

    public void markRequested() {
        idleTicksCounter = 0;
    }

    public Entity getTarget() {
        if (MC.player == null || MC.world == null || entityManagerModule == null)
            return null;

        markRequested();

        List<Entity> candidates = allEntities.stream()
                .filter(e -> e != MC.player)
                .filter(e -> {
                    if (e instanceof LivingEntity) {
                        LivingEntity le = (LivingEntity) e;
                        if (!le.isAlive()) return false;
                        if (e.age < entityManagerModule.minTicksExisted.get().intValue()) return false;
                        double distSq = e.squaredDistanceTo(MC.player);
                        if (distSq > entityManagerModule.targetRange.get() * entityManagerModule.targetRange.get()) return false;

                        return (entityManagerModule.targetPlayers.get() && e instanceof PlayerEntity && !FRIEND_MANAGER.isFriend(e.getName().getString()))
                                || (entityManagerModule.targetHostiles.get() && EntityUtils.isHostile(e))
                                || (entityManagerModule.targetNeutrals.get() && EntityUtils.isNeutral(e))
                                || (entityManagerModule.targetPassives.get() && EntityUtils.isPassive(e));
                    }

                    if (entityManagerModule.targetPrijectiles.get()) {
                        return (e instanceof ShulkerBulletEntity) || (e instanceof FireballEntity);
                    }

                    return false;
                })
                .collect(Collectors.toList());

        switch (entityManagerModule.priority.get()) {
            case HEALTH:
                return candidates.stream()
                        .filter(e -> e instanceof LivingEntity)
                        .min(Comparator.comparingDouble(e -> ((LivingEntity) e).getHealth()))
                        .orElse(null);

            case DISTANCE:
                return candidates.stream()
                        .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(MC.player)))
                        .orElse(null);

            case SMART:
                List<Entity> players = candidates.stream()
                        .filter(e -> e instanceof PlayerEntity && !FRIEND_MANAGER.isFriend(e.getName().getString()))
                        .sorted(Comparator.comparingDouble(e -> e.squaredDistanceTo(MC.player)))
                        .toList();

                if (!players.isEmpty()) return players.get(0);

                List<Entity> creepers = candidates.stream()
                        .filter(e -> e instanceof CreeperEntity)
                        .filter(e -> e.squaredDistanceTo(MC.player) <= 3 * 3) // yeah its not accurate at all, but its not required here i guess?
                        .sorted(Comparator.comparingDouble(e -> e.squaredDistanceTo(MC.player)))
                        .toList();

                if (!creepers.isEmpty()) return creepers.get(0);

                List<Entity> projectiles = candidates.stream()
                        .filter(e -> e instanceof ShulkerBulletEntity || e instanceof FireballEntity)
                        .sorted(Comparator.comparingDouble(e -> e.squaredDistanceTo(MC.player)))
                        .toList();

                if (!projectiles.isEmpty()) return projectiles.get(0);

                List<Entity> others = candidates.stream()
                        .filter(e -> !(e instanceof PlayerEntity)
                                && !(e instanceof ShulkerBulletEntity)
                                && !(e instanceof FireballEntity))
                        .toList();

                return others.stream()
                        .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(MC.player)))
                        .orElse(null);
        }

        return null;
    }

    public List<Entity> getAllEntities() {
        markRequested();
        return allEntities;
    }

    public List<PlayerEntity> getPlayers() {
        markRequested();
        return players;
    }

    public List<PlayerEntity> getOtherPlayers() {
        markRequested();
        return otherPlayers;
    }

    public List<Entity> getHostile() {
        markRequested();
        return hostile;
    }

    public List<Entity> getNeutral() {
        markRequested();
        return neutral;
    }

    public List<Entity> getPassive() {
        markRequested();
        return passive;
    }

    public List<ItemEntity> getDroppedItems() {
        markRequested();
        return droppedItems;
    }

    public List<Entity> getEndCrystals() {
        markRequested();
        return endCrystals;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onFrame(Render2DEvent event) {
        if (MC.world == null) {
            clearData();
            idleTicksCounter = 0;
            return;
        }

        entityManagerModule = MODULE_MANAGER.getStorage().getByClass(TargetModule.class);

        maxIdleTicks = MODULE_MANAGER.getStorage().getByClass(TargetModule.class).maxIdleTicks.get();

        if (idleTicksCounter < maxIdleTicks) {
            updateAll();
            idleTicksCounter++;
        } else {
            clearData();
        }
    }

    private void updateAll() {
        allEntities = EntityUtils.getAllEntities();
        players = EntityUtils.getPlayers();
        otherPlayers = EntityUtils.getOtherPlayers();

        hostile = allEntities.stream().filter(EntityUtils::isHostile).toList();
        neutral = allEntities.stream().filter(EntityUtils::isNeutral).toList();
        passive = allEntities.stream().filter(EntityUtils::isPassive).toList();
        droppedItems = allEntities.stream()
                .filter(e -> e instanceof ItemEntity)
                .map(e -> (ItemEntity) e)
                .toList();
        endCrystals = allEntities.stream()
                .filter(e -> e instanceof net.minecraft.entity.decoration.EndCrystalEntity)
                .toList();
    }

    private void clearData() {
        allEntities = List.of();
        players = List.of();
        otherPlayers = List.of();
        hostile = List.of();
        neutral = List.of();
        passive = List.of();
        droppedItems = List.of();
        endCrystals = List.of();
    }
}