package me.kiriyaga.nami.feature.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.kiriyaga.nami.feature.setting.Setting;

import java.awt.*;

public class ColorSetting extends Setting<Color> {

    private final boolean hasAlpha;

    public ColorSetting(String name, Color defaultValue, boolean hasAlpha) {
        super(name, defaultValue);
        this.hasAlpha = hasAlpha;
    }

    public int getRed() {
        return value.getRed();
    }

    public int getGreen() {
        return value.getGreen();
    }

    public int getBlue() {
        return value.getBlue();
    }

    public int getAlpha() {
        return hasAlpha ? value.getAlpha() : 255;
    }

    public void setValue(int r, int g, int b, int a) {
        this.value = new Color(r, g, b, hasAlpha ? a : 255);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            int r = obj.has("r") ? obj.get("r").getAsInt() : 255;
            int g = obj.has("g") ? obj.get("g").getAsInt() : 255;
            int b = obj.has("b") ? obj.get("b").getAsInt() : 255;
            int a = hasAlpha && obj.has("a") ? obj.get("a").getAsInt() : 255;
            this.value = new Color(r, g, b, a);
        }
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("r", value.getRed());
        obj.addProperty("g", value.getGreen());
        obj.addProperty("b", value.getBlue());
        if (hasAlpha) obj.addProperty("a", value.getAlpha());
        return obj;
    }
}
