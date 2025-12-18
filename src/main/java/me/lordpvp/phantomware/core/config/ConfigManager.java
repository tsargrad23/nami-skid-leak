package me.kiriyaga.nami.core.config;

import com.google.gson.*;
import me.kiriyaga.nami.core.macro.MacroManager;
import me.kiriyaga.nami.feature.module.Module;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

public class ConfigManager {

    private final ConfigDirectoryProvider dirProvider = new ConfigDirectoryProvider();
    private final ModuleConfigWriter moduleWriter = new ModuleConfigWriter(dirProvider);
    private final ModuleConfigReader moduleLoader = new ModuleConfigReader(dirProvider);
    private final ConfigSerializer configSerializer = new ConfigSerializer(dirProvider);
    private final FriendStorage friendStorage = new FriendStorage(dirProvider);
    private final MacroStorage macroStorage = new MacroStorage(dirProvider);

    public ConfigDirectoryProvider getDirectoryProvider() {
        return dirProvider;
    }

    public ModuleConfigWriter getModuleWriter() {
        return moduleWriter;
    }

    public ModuleConfigReader getModuleLoader() {
        return moduleLoader;
    }

    public ConfigSerializer getConfigSerializer() {
        return configSerializer;
    }

    public FriendStorage getFriendStorage() {
        return friendStorage;
    }

    public MacroStorage getMacroStorage() {
        return macroStorage;
    }

    public void saveModules() {
        for (Module module : MODULE_MANAGER.getStorage().getAll()) {
            moduleWriter.saveModule(module);
        }
    }

    public void loadModules() {
        for (Module module : MODULE_MANAGER.getStorage().getAll()) {
            moduleLoader.loadModule(module);
        }
    }

    public void saveConfig(String name) {
        configSerializer.save(name);
    }

    public void loadConfig(String name) {
        configSerializer.load(name);
    }

    public void saveFriends(Set<String> friends) {
        friendStorage.save(friends);
    }

    public Set<String> loadFriends() {
        return friendStorage.load();
    }

    public boolean isFriend(String name) {
        return friendStorage.isFriend(name);
    }

    public void saveName(String nameValue) {
        File file = new File(dirProvider.getBaseDir(), "name.json");
        JsonObject json = new JsonObject();
        json.addProperty("name", nameValue);

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save name.json", e);
        }
    }

    public String loadName() {
        File file = new File(dirProvider.getBaseDir(), "name.json");
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return json.has("name") ? json.get("name").getAsString() : null;
        } catch (Exception e) {
            LOGGER.error("Failed to load name.json", e);
            return null;
        }
    }

    public void savePrefix(String prefix) {
        File file = new File(dirProvider.getBaseDir(), "prefix.json");
        JsonObject json = new JsonObject();
        json.addProperty("prefix", prefix);

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save prefix.json", e);
        }
    }

    public String loadPrefix() {
        File file = new File(dirProvider.getBaseDir(), "prefix.json");
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return json.has("prefix") ? json.get("prefix").getAsString() : null;
        } catch (Exception e) {
            LOGGER.error("Failed to load prefix.json", e);
            return null;
        }
    }

    public void saveMacros() {
        macroStorage.save(MACRO_MANAGER.getAll());
    }

    public void loadMacros() {
        MACRO_MANAGER.clear();
        MACRO_MANAGER.getAll().addAll(macroStorage.load());
    }
}
