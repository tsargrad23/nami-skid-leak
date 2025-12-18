package me.kiriyaga.nami.core.module;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.ModuleCategory;

import java.util.*;

public class ModuleStorage {

    private final List<Module> modules = new ArrayList<>();
    private final Map<String, Module> modulesByName = new HashMap<>();
    private final Map<Class<? extends Module>, Module> modulesByClass = new HashMap<>();

    public void add(Module module) {
        modules.add(module);
        modulesByName.put(module.getName(), module);
        modulesByName.put(module.getName().replace(" ", ""), module);
        modulesByClass.put(module.getClass(), module);
    }

    public void remove(Module module) {
        modules.remove(module);
        modulesByName.remove(module.getName());
        modulesByName.remove(module.getName().replace(" ", ""));
        modulesByClass.remove(module.getClass());
        module.setEnabled(false);
    }

    public List<Module> getAll() {
        return modules;
    }

    public <T extends Module> T getByClass(Class<T> clazz) {
        return clazz.cast(modulesByClass.get(clazz));
    }

    public Module getByName(String name) {
        if (name == null) return null;
        String lower = name;
        Module m = modulesByName.get(lower);
        if (m != null) return m;
        return modulesByName.get(lower.replace(" ", ""));
    }

    public List<Module> getByCategory(ModuleCategory category) {
        return modules.stream()
                .filter(m -> m.getCategory().equals(category))
                .sorted(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public int size() {
        return modules.size();
    }
}