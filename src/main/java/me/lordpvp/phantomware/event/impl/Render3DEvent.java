package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class Render3DEvent extends Event {
    private final MatrixStack matrices;
    private final float tickDelta;
    private final Camera camera;
    private final Matrix4f positionMatrix;
    private final Matrix4f projectionMatrix;

    public Render3DEvent(MatrixStack matrices, float tickDelta, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
        this.camera = camera;
        this.positionMatrix = positionMatrix;
        this.projectionMatrix = projectionMatrix;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public Camera getCamera() {
        return camera;
    }

    public Matrix4f getPositionMatrix() {
        return positionMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}


