package me.kiriyaga.nami.core.executable.model;

public class ExecutableRequest {
    public Runnable runnable;
    public int ticksDelay;
    public final int initialDelay;
    public final ExecutableThreadType type;
    public final boolean repeat;

    public ExecutableRequest(Runnable runnable, int ticksDelay, ExecutableThreadType type, boolean repeat) {
        this.runnable = runnable;
        this.ticksDelay = ticksDelay;
        this.initialDelay = ticksDelay;
        this.type = type;
        this.repeat = repeat;
    }
}
