package me.kiriyaga.nami.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.setting.Setting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static me.kiriyaga.nami.Nami.LOGGER;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

public class ConfigSerializer {
    private final ConfigDirectoryProvider dirs;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigSerializer(ConfigDirectoryProvider dirs) {
        this.dirs = dirs;
    }

    public void save(String configName) {
        JsonObject root = new JsonObject();

        JsonObject modules = new JsonObject();
        for (Module m : MODULE_MANAGER.getStorage().getAll()) {
            JsonObject mod = new JsonObject();
            mod.addProperty("enabled", m.isEnabled());

            JsonObject settings = new JsonObject();
            for (Setting<?> s : m.getSettings()) {
                if (s instanceof me.kiriyaga.nami.feature.setting.impl.KeyBindSetting ||
                        s instanceof me.kiriyaga.nami.feature.setting.impl.ColorSetting) {
                    continue;
                }
                settings.add(s.getName(), s.toJson());
            }

            mod.add("settings", settings);
            modules.add(m.getName(), mod);
        }

        root.add("modules", modules);

        File file = new File(dirs.getConfigSaveDir(), configName + ".json");
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save config " + configName, e);
        }
    }

    public void load(String configName) {
        File file = new File(dirs.getConfigSaveDir(), configName + ".json");
        if (!file.exists()) {
            LOGGER.warn("Config file not found: " + configName);
            return;
        }

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            JsonObject modules = root.getAsJsonObject("modules");
            for (Module m : MODULE_MANAGER.getStorage().getAll()) {
                if (!modules.has(m.getName())) continue;

                JsonObject mod = modules.getAsJsonObject(m.getName());

                boolean enabled = mod.get("enabled").getAsBoolean();
                if (enabled != m.isEnabled()) m.toggle();

                JsonObject settings = mod.getAsJsonObject("settings");
                for (Setting<?> s : m.getSettings()) {
                    if (s instanceof me.kiriyaga.nami.feature.setting.impl.KeyBindSetting ||
                            s instanceof me.kiriyaga.nami.feature.setting.impl.ColorSetting) {
                        continue;
                    }
                    if (settings.has(s.getName())) {
                        s.fromJson(settings.get(s.getName()));
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to load config " + configName, e);
        }
    }
    public List<String> listConfigs() {
        File dir = dirs.getConfigSaveDir();
        if (!dir.exists() || !dir.isDirectory()) {
            return List.of();
        }

        return Arrays.stream(dir.listFiles((d, name) -> name.endsWith(".json")))
                .map(f -> f.getName().replaceFirst("\\.json$", ""))
                .collect(Collectors.toList());
    }
}
