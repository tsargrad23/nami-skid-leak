package me.kiriyaga.nami.core.config;

import com.google.gson.*;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.setting.Setting;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

import static me.kiriyaga.nami.Nami.LOGGER;

public class ModuleConfigReader {
    private final ConfigDirectoryProvider dirs;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ModuleConfigReader(ConfigDirectoryProvider dirs) {
        this.dirs = dirs;
    }

    public void loadModule(Module module) {
        File file = new File(dirs.getModuleConfigDir(), module.getName() + ".json");
        if (!file.exists()) {
            LOGGER.warn("Module config not found: " + module.getName());
            return;
        }

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (root.has("enabled")) {
                boolean enabled = root.get("enabled").getAsBoolean();
                if (enabled != module.isEnabled()) {
                    module.toggle();
                }
            }

            if (root.has("settings")) {
                JsonObject settingsJson = root.getAsJsonObject("settings");
                for (Setting<?> setting : module.getSettings()) {
                    if (settingsJson.has(setting.getName())) {
                        JsonElement value = settingsJson.get(setting.getName());
                        setting.fromJson(value);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to load module config: " + module.getName(), e);
        }
    }
}
