package me.kiriyaga.nami.core.config;

import com.google.gson.*;
import me.kiriyaga.nami.core.macro.model.Macro;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MacroStorage {
    private final File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public MacroStorage(ConfigDirectoryProvider dirs) {
        this.file = new File(dirs.getBaseDir(), "macros.json");
    }

    public void save(List<Macro> macros) {
        JsonArray array = new JsonArray();
        for (Macro macro : macros) {
            JsonObject obj = new JsonObject();
            obj.addProperty("keyCode", macro.getKeyCode());
            obj.addProperty("message", macro.getMessage());
            array.add(obj);
        }

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(array, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Macro> load() {
        List<Macro> macros = new ArrayList<>();
        if (!file.exists()) return macros;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement el : array) {
                JsonObject obj = el.getAsJsonObject();
                macros.add(new Macro(
                        obj.get("keyCode").getAsInt(),
                        obj.get("message").getAsString()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return macros;
    }
}
