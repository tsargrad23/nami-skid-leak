package me.kiriyaga.nami.core.executable;

import me.kiriyaga.nami.core.executable.model.ExecutableThreadType;
import me.kiriyaga.nami.core.executable.model.ExecutableRequest;

import java.util.function.Consumer;

public class ExecutableRequestHandler {

    private final ExecutableStateHandler stateHandler;

    public ExecutableRequestHandler(ExecutableStateHandler stateHandler) {
        this.stateHandler = stateHandler;
    }

    public void submit(Runnable runnable, int delayTicks, ExecutableThreadType type) {
        stateHandler.addRequest(new ExecutableRequest(runnable, delayTicks, type, false));
    }

    public void submitRepeating(Runnable runnable, int delayTicks, ExecutableThreadType type) {
        stateHandler.addRequest(new ExecutableRequest(runnable, delayTicks, type, true));
    }

    public void submitCustom(Consumer<ExecutableRequest> configurator) {
        ExecutableRequest request = new ExecutableRequest(null, 0, ExecutableThreadType.PRE_TICK, false);
        configurator.accept(request);
        stateHandler.addRequest(request);
    }
}
