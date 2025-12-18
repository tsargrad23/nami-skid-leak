package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

public class MoveEvent extends Event {
    private MovementType movementType;
    private Vec3d movement;

    public MoveEvent(MovementType movementType, Vec3d movement) {
        this.movementType = movementType;
        this.movement = movement;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public Vec3d getMovement() {
        return movement;
    }

    public void setMovement(Vec3d movement) {
        this.movement = movement;
    }
}
