package me.kiriyaga.nami.feature.module.impl.client;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.Render3DEvent;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;
import me.kiriyaga.nami.util.PredictMovementUtils;
import me.kiriyaga.nami.util.render.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.List;
import java.util.Optional;

import static me.kiriyaga.nami.Nami.*;

//@RegisterModule
public class PredictTestModule extends Module {

    public final IntSetting ticks = addSetting(new IntSetting("Ticks", 3, 1, 20));
    public final BoolSetting predictSelf = addSetting(new BoolSetting("Self", true));
    public final BoolSetting predictOthers = addSetting(new BoolSetting("Others", true));
    public final BoolSetting showBox = addSetting(new BoolSetting("ShowBox", true));
    public final BoolSetting showEye = addSetting(new BoolSetting("ShowEye", true));

    public PredictTestModule() {
        super("PredictTest", ".", ModuleCategory.of("Client"));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (MC.world == null || MC.player == null) return;

        MatrixStack matrices = event.getMatrices();

        if (predictSelf.get())
            renderPredictionForEntity(MC.player, matrices);

        if (predictOthers.get()) {
            List<PlayerEntity> others = ENTITY_MANAGER.getOtherPlayers();
            for (PlayerEntity other : others) {
                if (other.isRemoved()) continue;
                renderPredictionForEntity(other, matrices);
            }
        }
    }

    private void renderPredictionForEntity(Entity entity, MatrixStack matrices) {
        PredictMovementUtils.PredictedEntity initial = new PredictMovementUtils.PredictedEntity(
                entity.getPos(),
                entity.getVelocity(),
                entity.getYaw(),
                entity.getPitch(),
                entity.isOnGround(),
                entity.getStandingEyeHeight()
        );

        PredictMovementUtils.PredictedEntity predicted = PredictMovementUtils.predict(initial, ticks.get(), t -> Vec3d.ZERO);

        if (predicted == null) return;

        if (showBox.get()) {
            Box box = entity.getBoundingBox().offset(predicted.pos.subtract(entity.getPos()));
            RenderUtil.drawBoxFilled(matrices, box, new Color(0, 255, 0, 40));
            RenderUtil.drawBox(matrices, box, new Color(0, 255, 0, 200), 1.5f);
        }

        if (showEye.get()) {
            Vec3d eye = predicted.getEyePos();
            double size = 0.1;
            Box eyeBox = new Box(eye.x - size, eye.y - size, eye.z - size, eye.x + size, eye.y + size, eye.z + size);
            RenderUtil.drawBoxFilled(matrices, eyeBox, new Color(255, 0, 0, 150));
            RenderUtil.drawBox(matrices, eyeBox, new Color(255, 0, 0, 255), 1.0f);
        }
    }
}
