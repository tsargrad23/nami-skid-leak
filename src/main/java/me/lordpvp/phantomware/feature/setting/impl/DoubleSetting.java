package me.kiriyaga.nami.feature.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import me.kiriyaga.nami.feature.setting.Setting;

public class DoubleSetting extends Setting<Double> {

    private final double min, max;

    public DoubleSetting(String name, double defaultValue, double min, double max) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public void set(Double value) {
        this.value = Math.max(min, Math.min(max, value));
    }

    public void fromJson(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            set(json.getAsDouble());
        }
    }

    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }
}
