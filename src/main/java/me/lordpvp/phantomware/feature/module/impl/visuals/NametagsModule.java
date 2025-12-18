package me.kiriyaga.nami.feature.module.impl.visuals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.kiriyaga.nami.core.executable.model.ExecutableThreadType;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.DoubleSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.util.NametagFormatter;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL32C;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;

import static me.kiriyaga.nami.Nami.*;
import static net.caffeinemc.mods.sodium.client.util.FlawlessFrames.isActive;

@RegisterModule
public class NametagsModule extends Module {

    public final BoolSetting self = addSetting(new BoolSetting("Self", false));
    public final BoolSetting players = addSetting(new BoolSetting("Players", true));
    public final BoolSetting hostiles = addSetting(new BoolSetting("Hostiles", false));
    public final BoolSetting neutrals = addSetting(new BoolSetting("Neutrals", false));
    public final BoolSetting passives = addSetting(new BoolSetting("Passives", false));
    public final BoolSetting items = addSetting(new BoolSetting("Items", false));
    public final BoolSetting tamed = addSetting(new BoolSetting("Tamed", false));
    public final BoolSetting pearls = addSetting(new BoolSetting("Pearls", false));
    public final BoolSetting showItems = addSetting(new BoolSetting("Equipment", true));
    public final BoolSetting showHealth = addSetting(new BoolSetting("Health", false));
    public final BoolSetting showGameMode = addSetting(new BoolSetting("Gamemode", false));
    public final BoolSetting showPing = addSetting(new BoolSetting("Ping", true));
    public final BoolSetting showEntityId = addSetting(new BoolSetting("EntityId", false));
    public final EnumSetting<TextFormat> formatting = addSetting(new EnumSetting<>("Format", TextFormat.NONE));
    public final BoolSetting background = addSetting(new BoolSetting("Background", false));
    public final BoolSetting border = addSetting(new BoolSetting("Border", true));
    public final DoubleSetting borderWidth = addSetting(new DoubleSetting("Width", 0.25, 0.11, 1));

    private final NametagFormatter formatter = new NametagFormatter(this);

    public enum TextFormat {
        NONE, BOLD, ITALIC, BOTH
    }

    private static final Map<UUID, String> uuid = new HashMap<>();

    public NametagsModule() {
        super("Nametags", "Draws nametags above certain entities.", ModuleCategory.of("Render"));
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        if (MC.world == null || MC.player == null) return;

        int i = 0;

        MatrixStack matrices = event.getMatrices();

        if (players.get()) {
            for (PlayerEntity player : ENTITY_MANAGER.getPlayers()) {
                if (player == MC.player) continue;
                i++;
                renderEntityNametag(player, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (self.get() && !MC.options.getPerspective().isFirstPerson()){
            i++;
            renderEntityNametag(MC.player, event.getTickDelta(), matrices, 30, null);
        }

        if (hostiles.get()) {
            for (var entity : ENTITY_MANAGER.getHostile()) {
                if (entity.isInvisible()) continue;
                i++;
                renderEntityNametag(entity, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (neutrals.get()) {
            for (var entity : ENTITY_MANAGER.getNeutral()) {
                if (entity.isInvisible()) continue;
                i++;
                renderEntityNametag(entity, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (passives.get()) {
            for (var entity : ENTITY_MANAGER.getPassive()) {
                if (entity.isInvisible()) continue;
                i++;
                renderEntityNametag(entity, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (items.get()) {
            for (var entity : ENTITY_MANAGER.getDroppedItems()) {
                if (entity.isInvisible()) continue;
                i++;
                renderEntityNametag(entity, event.getTickDelta(), matrices, 30, null);
            }
        }

        if (tamed.get()) {

            for (var entity : ENTITY_MANAGER.getAllEntities()) {

                @Nullable LazyEntityReference<LivingEntity> owner;

                if (entity instanceof TameableEntity tameable) {
                    owner = tameable.getOwnerReference();
                } else {
                    continue;
                }

                if (owner == null)
                    return;

                UUID uuid = owner.getUuid();

                String ownerName;

                if (NametagsModule.uuid.containsKey(uuid)) {
                    ownerName = NametagsModule.uuid.get(uuid);
                } else {
                    ownerName = "Owned by ";

                    EXECUTABLE_MANAGER.getRequestHandler().submit(() -> {

                        if (isActive()) {
                            try {
                                String urlStr = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "");
                                URL url = new URL(urlStr);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");
                                connection.setConnectTimeout(5000);
                                connection.setReadTimeout(5000);

                                int status = connection.getResponseCode();

                                if (status == 200) {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    StringBuilder responseBuilder = new StringBuilder();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        responseBuilder.append(line);
                                    }
                                    reader.close();

                                    String response = responseBuilder.toString();

                                    if (response != null && !response.isEmpty()) {
                                        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                                        if (json.has("name")) {
                                            String name = json.get("name").getAsString();
                                            NametagsModule.uuid.put(uuid, name);
                                        } else {
                                            NametagsModule.uuid.put(uuid, "Failed to get name");
                                        }
                                    } else {
                                        NametagsModule.uuid.put(uuid, "Failed to get name");
                                    }
                                } else {
                                    NametagsModule.uuid.put(uuid, "Failed to get name");
                                }

                                connection.disconnect();
                            } catch (Exception e) {
                                NametagsModule.uuid.put(uuid, "Failed to get name");
                            }
                        } else {
                        }
                    }, 0, ExecutableThreadType.ASYNC);
                }

                i++;
                renderEntityNametag(entity, "Owned by " + ownerName, event.getTickDelta(), matrices, 30, null);
            }
    }


        if (pearls.get()) {
            for (var entity : ENTITY_MANAGER.getAllEntities()) {
                if (!(entity instanceof EnderPearlEntity pearl)) continue;
                if (pearl.getOwner() == null) continue;
                i++;
                renderEntityNametag(pearl, pearl.getOwner().getName().getString(), event.getTickDelta(), matrices, 30, null);
            }
        }
        this.setDisplayInfo(String.valueOf(i));
    }

    private void renderEntityNametag(Entity entity, float tickDelta, MatrixStack matrices, float scale, Color forcedColor) {
        renderEntityNametag(entity, entity.getName().getString(), tickDelta, matrices, scale, forcedColor);
    }

    private void renderEntityNametag(Entity entity, String name, float tickDelta, MatrixStack matrices, float scale, Color forcedColor) {
        Vec3d camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

        double baseHeightOffset = entity.isSneaking() ? entity.getBoundingBox().getLengthY() : entity.getBoundingBox().getLengthY() + 0.3;

        double interpX = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
        double interpY = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
        double interpZ = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());

        float distance = (float) camPos.distanceTo(new Vec3d(interpX, interpY, interpZ));
        double distanceYOffset = distance * 0.02;

        Vec3d pos = new Vec3d(
                interpX,
                interpY + baseHeightOffset + distanceYOffset,
                interpZ
        );

        float dynamicScale = 0.0018f + (scale / 10000.0f) * distance;
        if (distance <= 8.0f) dynamicScale = 0.0245f;

        Text displayName;

        if (entity instanceof PlayerEntity player) {
            displayName = formatter.formatPlayer(player);

            if (showHealth.get()) {
                displayName = Text.literal("").append(displayName).append(Text.literal(" ")).append(formatter.getHealthText(player));
            }
            if (showPing.get()) {
                displayName = Text.literal("").append(displayName).append(Text.literal(" ")).append(formatter.formatPing(player));
            }
            if (showGameMode.get()) {
                displayName = Text.literal("").append(displayName).append(Text.literal(" ")).append(formatter.formatGameMode(player));
            }
            if (showEntityId.get()) {
                displayName = Text.literal("").append(displayName).append(Text.literal(" ")).append(formatter.formatEntityId(entity));
            }
        } else if (entity instanceof ItemEntity itemEntity) {
            displayName = formatter.formatItem(itemEntity);
        } else if (name != null) {
            displayName = Text.literal(name);
        } else {
            displayName = formatter.formatEntity(entity);
        }

        Text colored = formatter.formatWithColor(displayName, forcedColor, entity);

        RenderUtil.drawText3D(matrices, colored, pos, dynamicScale, background.get(), border.get(), borderWidth.get().floatValue());

        if (showItems.get() && entity instanceof PlayerEntity player) {
            renderPlayerItems(player, matrices, tickDelta, scale);
        }
    }

    private void renderPlayerItems(PlayerEntity player, MatrixStack matrices, float tickDelta, float baseScale) {
        List<ItemStack> items = Arrays.asList(
                player.getMainHandStack(),
                player.getEquippedStack(EquipmentSlot.HEAD),
                player.getEquippedStack(EquipmentSlot.CHEST),
                player.getEquippedStack(EquipmentSlot.LEGS),
                player.getEquippedStack(EquipmentSlot.FEET),
                player.getOffHandStack()
        );

        List<ItemStack> nonEmptyItems = items.stream().filter(stack -> !stack.isEmpty()).toList();
        int itemCount = nonEmptyItems.size();
        if (itemCount == 0) return;

        double interpMinX = MathHelper.lerp(tickDelta, player.lastRenderX, player.getX()) - player.getWidth() / 2.0;
        double interpMinY = MathHelper.lerp(tickDelta, player.lastRenderY, player.getY());
        double interpMinZ = MathHelper.lerp(tickDelta, player.lastRenderZ, player.getZ()) - player.getWidth() / 2.0;

        double interpMaxX = interpMinX + player.getWidth();
        double interpMaxY = interpMinY + player.getHeight();
        double interpMaxZ = interpMinZ + player.getWidth();

        double baseX = (interpMinX + interpMaxX) / 2.0;
        double baseY = interpMaxY + (player.isSneaking() ? 0.0 : 0.3);
        double baseZ = (interpMinZ + interpMaxZ) / 2.0;

        Vec3d camPos = MC.getEntityRenderDispatcher().camera.getPos();
        Camera camera = MC.gameRenderer.getCamera();
        float pitch = camera.getPitch();
        float yaw = camera.getYaw();

        Vec3d lookDir = Vec3d.fromPolar(pitch, yaw).normalize().negate();
        Vec3d camRight = lookDir.crossProduct(new Vec3d(0, 1, 0)).normalize();

        int renderIndex = 0;
        for (ItemStack stack : nonEmptyItems) {
            renderItemWithDepthIsolation(stack, matrices, baseX, baseY, baseZ, renderIndex, itemCount, camPos, camRight, lookDir, baseScale);
            renderIndex++;
        }
    }

    private void renderItemWithDepthIsolation(
            ItemStack stack,
            MatrixStack matrices,
            double baseX, double baseY, double baseZ,
            int renderIndex, int itemCount,
            Vec3d camPos, Vec3d camRight, Vec3d lookDir,
            float baseScale
    ) {
        Vec3d itemPosBase = new Vec3d(baseX, baseY, baseZ);
        float distance = (float) camPos.distanceTo(itemPosBase);
        float dynamicScale = 0.0018f + (baseScale / 10000.0f) * distance;
        if (distance <= 8.0f) dynamicScale = 0.0245f;

        double itemSpacing = dynamicScale * 12.0;

        double verticalOffset = dynamicScale * 10.0 + distance * 0.02;

        double offsetX = (renderIndex - (itemCount - 1) / 2.0) * itemSpacing;

        Vec3d itemPos = itemPosBase.add(camRight.multiply(offsetX)).add(0, verticalOffset, 0);

        //GL32C.glDisable(GL32C.GL_DEPTH_TEST);
        //GL32C.glDepthMask(false);
        //GL32C.glDepthFunc(GL32C.GL_ALWAYS);
        GL32C.glDepthRange(1.0, 0.1);

        RenderUtil.renderItem3D(stack, matrices, itemPos, dynamicScale, lookDir);
        
        //GL32C.glDepthFunc(GL32C.GL_LEQUAL);
        //GL32C.glDepthMask(true);
        //GL32C.glEnable(GL32C.GL_DEPTH_TEST);
        GL32C.glDepthRange(0.0, 1.0);
    }
}