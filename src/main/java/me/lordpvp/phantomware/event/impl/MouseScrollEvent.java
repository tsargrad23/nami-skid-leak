package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;

public class MouseScrollEvent extends Event {
    private final double mouseX;
    private final double mouseY;
    private final double amount;

    public MouseScrollEvent(double mouseX, double mouseY, double amount) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.amount = amount;
    }

    public double mouseX() {
        return mouseX;
    }

    public double mouseY() {
        return mouseY;
    }

    public double amount() {
        return amount;
    }
}
