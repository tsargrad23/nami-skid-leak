package me.kiriyaga.nami.feature.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.kiriyaga.nami.feature.setting.Setting;
import org.lwjgl.glfw.GLFW;

import static me.kiriyaga.nami.Nami.MC;

public class KeyBindSetting extends Setting<Integer> {

    public static final int KEY_NONE = -1;
    private boolean wasPressedLastTick = false;
    private boolean holdMode = false;

    public KeyBindSetting(String name, int defaultKey) {
        super(name, defaultKey);
    }

    public boolean isPressed() {
        if (value == KEY_NONE) return false;

        if (value == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return MC.mouse.wasLeftButtonClicked();
        }
        if (value == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            return MC.mouse.wasRightButtonClicked();
        }
        if (value == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            return MC.mouse.wasMiddleButtonClicked();
        }

        return GLFW.glfwGetKey(MC.getWindow().getHandle(), value) == GLFW.GLFW_PRESS;
    }


    public String getKeyName() {
        if (value == KEY_NONE) return "none";

        switch (value) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT: return "MOUSE_LEFT";
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT: return "MOUSE_RIGHT";
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE: return "MOUSE_MIDDLE";
            case GLFW.GLFW_MOUSE_BUTTON_4: return "MOUSE_4";
            case GLFW.GLFW_MOUSE_BUTTON_5: return "MOUSE_5";
            case GLFW.GLFW_MOUSE_BUTTON_6: return "MOUSE_6";
            case GLFW.GLFW_MOUSE_BUTTON_7: return "MOUSE_7";
            case GLFW.GLFW_MOUSE_BUTTON_8: return "MOUSE_8";
        }

        String keyName = GLFW.glfwGetKeyName(value, 0);
        return keyName != null ? keyName.toUpperCase() : "KEY_" + value;
    }

    public boolean isHoldMode() {
        return holdMode;
    }

    public void setHoldMode(boolean holdMode) {
        this.holdMode = holdMode;
    }


    public boolean wasPressedLastTick() {
        return wasPressedLastTick;
    }

    public void setWasPressedLastTick(boolean val) {
        this.wasPressedLastTick = val;
    }

    @Override
    public void set(Integer value) {
        this.value = value;
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("value", value);
        obj.addProperty("holdMode", holdMode);
        return obj;
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            this.value = obj.has("value") ? obj.get("value").getAsInt() : KEY_NONE;
            this.holdMode = obj.has("holdMode") && obj.get("holdMode").getAsBoolean();
        } else {
            this.value = KEY_NONE;
            this.holdMode = false;
        }
    }
}
