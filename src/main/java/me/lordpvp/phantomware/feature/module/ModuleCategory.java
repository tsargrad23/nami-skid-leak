package me.kiriyaga.nami.feature.module;

import java.util.*;

public class ModuleCategory {
    private static final Map<String, ModuleCategory> CATEGORIES = new LinkedHashMap<>();

    private static final List<String> FIXED_ORDER = List.of(
            "Combat", "Miscellaneous", "Movement", "Render", "World", "HUD", "Client"
    );

    private final String name;

    private ModuleCategory(String name) {
        this.name = name;
    }

    public static ModuleCategory of(String name) {
        return CATEGORIES.computeIfAbsent(name, ModuleCategory::new);
    }

    public static List<ModuleCategory> getAll() {
        List<ModuleCategory> sorted = new ArrayList<>();

        Set<String> added = new HashSet<>();

        for (String key : FIXED_ORDER) {
            ModuleCategory cat = CATEGORIES.get(key);
            if (cat != null) {
                sorted.add(cat);
                added.add(cat.name);
            }
        }

        for (ModuleCategory cat : CATEGORIES.values()) {
            if (!added.contains(cat.name)) {
                sorted.add(cat);
            }
        }

        return sorted;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}