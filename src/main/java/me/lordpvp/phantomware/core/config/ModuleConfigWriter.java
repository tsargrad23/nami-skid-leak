package me.kiriyaga.nami.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.setting.Setting;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

import static me.kiriyaga.nami.Nami.LOGGER;

public class ModuleConfigWriter {
    private final ConfigDirectoryProvider dirs;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ModuleConfigWriter(ConfigDirectoryProvider dirs) {
        this.dirs = dirs;
    }

    public void saveModule(Module module) {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", module.isEnabled());

        JsonObject settings = new JsonObject();
        for (Setting<?> s : module.getSettings()) {
            settings.add(s.getName(), s.toJson());
        }

        json.add("settings", settings);

        File file = new File(dirs.getModuleConfigDir(), module.getName() + ".json");
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(json, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save module config: " + module.getName(), e);
        }
    }
}
