package me.lordpvp.phantomware.util;

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;

public class NamiDimension extends DimensionEffects {
    private final Vec3d baseColor;
    private final double factor;

    public PhantomDimension(Vec3d baseColor, double factor) {
        super(SkyType.NORMAL, true, true);
        this.baseColor = baseColor;
        this.factor = factor;
    }

    @Override
    public Vec3d adjustFogColor(Vec3d vec3d, float f) {
        return baseColor.multiply(factor);
    }

    @Override
    public boolean useThickFog(int camX, int camY) {
        return false;
    }
}
