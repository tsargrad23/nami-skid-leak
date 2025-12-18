package me.kiriyaga.nami.core.module;

import me.kiriyaga.nami.feature.module.Module;

import static me.kiriyaga.nami.Nami.LOGGER;

public class ModuleManager {

    private final ModuleStorage storage = new ModuleStorage();

    public void init() {
        ModuleRegistry.registerAnnotatedModules(storage);
        LOGGER.info("Registered " + storage.size() + " modules.");
    }

    public ModuleStorage getStorage() {
        return storage;
    }

    public void registerModule(Module module) {
        storage.add(module);
    }

    public void unregisterModule(Module module) {
        storage.remove(module);
    }
}
