package me.kiriyaga.nami.core.executable;

public class ExecutableManager {

    private final ExecutableStateHandler stateHandler = new ExecutableStateHandler();
    private final ExecutableRequestHandler requestHandler = new ExecutableRequestHandler(stateHandler);
    private final ExecutableTickHandler tickHandler = new ExecutableTickHandler(stateHandler, requestHandler);

    public void init() {
        tickHandler.init();
    }

    public ExecutableStateHandler getStateHandler() {
        return stateHandler;
    }

    public ExecutableRequestHandler getRequestHandler() {
        return requestHandler;
    }

    public ExecutableTickHandler getTickHandler() {
        return tickHandler;
    }
}
