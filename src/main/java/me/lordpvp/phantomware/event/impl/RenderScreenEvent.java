package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class RenderScreenEvent extends Event {
    private final DrawContext drawContext;
    private final RenderTickCounter renderTickCounter;
    private final int mouseX;
    private final int mouseY;

    public RenderScreenEvent(DrawContext drawContext, RenderTickCounter renderTickCounter, int mouseX, int mouseY) {
        this.drawContext = drawContext;
        this.renderTickCounter = renderTickCounter;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public DrawContext getDrawContext() {
        return drawContext;
    }

    public RenderTickCounter getRenderTickCounter() {
        return renderTickCounter;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }
}