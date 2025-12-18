package me.kiriyaga.nami.core.config;

import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static me.kiriyaga.nami.Nami.LOGGER;

public class FriendStorage {
    private final ConfigDirectoryProvider dirs;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FriendStorage(ConfigDirectoryProvider dirs) {
        this.dirs = dirs;
    }

    public void save(Set<String> friends) {
        JsonArray array = new JsonArray();
        for (String f : friends) {
            array.add(f.toLowerCase());
        }

        File file = dirs.getFriendFile();
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(array, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save friends.json", e);
        }
    }

    public Set<String> load() {
        Set<String> friends = new HashSet<>();
        File file = dirs.getFriendFile();

        if (!file.exists()) return friends;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (element.isJsonArray()) {
                for (JsonElement e : element.getAsJsonArray()) {
                    if (e.isJsonPrimitive()) {
                        friends.add(e.getAsString().toLowerCase());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load friends.json", e);
        }

        return friends;
    }

    public boolean isFriend(String name) {
        return load().contains(name.toLowerCase());
    }
}
