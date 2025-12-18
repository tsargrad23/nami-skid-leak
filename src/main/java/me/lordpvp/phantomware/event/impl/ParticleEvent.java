package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;

public class ParticleEvent extends Event {
    private final ParticleEffect particle;


    public ParticleEvent(ParticleEffect particle) {
        this.particle = particle;
    }

    public ParticleEffect getParticle() {
        return particle;
    }
}
