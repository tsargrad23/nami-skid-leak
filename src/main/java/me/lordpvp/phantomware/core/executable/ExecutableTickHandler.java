package me.kiriyaga.nami.core.executable;

import me.kiriyaga.nami.core.executable.model.ExecutableRequest;
import me.kiriyaga.nami.core.executable.model.ExecutableThreadType;
import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.event.impl.PostTickEvent;
import me.kiriyaga.nami.event.impl.Render2DEvent;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.kiriyaga.nami.Nami.EVENT_MANAGER;

public class ExecutableTickHandler {

    private final ExecutableStateHandler stateHandler;
    private final ExecutableRequestHandler requestHandler;

    private ExecutorService asyncExecutor;

    public ExecutableTickHandler(ExecutableStateHandler stateHandler, ExecutableRequestHandler requestHandler) {
        this.stateHandler = stateHandler;
        this.requestHandler = requestHandler;
    }

    public void init() {
        EVENT_MANAGER.register(this);
    }

    private ExecutorService getAsyncExecutor() {
        if (asyncExecutor == null || asyncExecutor.isShutdown()) {
            asyncExecutor = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    r -> {
                        Thread t = new Thread(r, "NamiAsyncThread");
                        t.setDaemon(true);
                        return t;
                    }
            );
        }
        return asyncExecutor;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreTick(PreTickEvent event) {
        execute(ExecutableThreadType.PRE_TICK);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPostTick(PostTickEvent event) {
        execute(ExecutableThreadType.POST_TICK);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRender2D(Render2DEvent event) {
        execute(ExecutableThreadType.RENDER_2D);
    }

    private void execute(ExecutableThreadType type) {
        for (ExecutableRequest req : stateHandler.getActiveRequests()) {
            if (req.ticksDelay > 0) {
                req.ticksDelay--;
                continue;
            }

            try {
                if (req.runnable != null) {
                    if (req.type == ExecutableThreadType.ASYNC) {
                        getAsyncExecutor().submit(() -> {
                            try {
                                req.runnable.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } else if (req.type == type) {
                        req.runnable.run();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (req.repeat) {
                req.ticksDelay = req.initialDelay;
            } else {
                stateHandler.getActiveRequests().remove(req);
            }
        }
    }

    public void shutdown() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdownNow();
        }
    }
}
