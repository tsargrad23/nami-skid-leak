package me.lordpvp.phantomware.util;
import org.lwjgl.glfw.GLFW;

public class KeyUtils {
    public static int parseKey(String keyName) {
        keyName = keyName.toUpperCase();

        try {
            return Integer.parseInt(keyName);
        } catch (NumberFormatException ignored) {}

        switch (keyName) {
            case "LCTRL": return GLFW.GLFW_KEY_LEFT_CONTROL;
            case "RCTRL": return GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LSHIFT": return GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RSHIFT": return GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LALT": return GLFW.GLFW_KEY_LEFT_ALT;
            case "RALT": return GLFW.GLFW_KEY_RIGHT_ALT;
            case "SPACE": return GLFW.GLFW_KEY_SPACE;
            case "ENTER": return GLFW.GLFW_KEY_ENTER;
            case "TAB": return GLFW.GLFW_KEY_TAB;
            case "ESC": case "ESCAPE": return GLFW.GLFW_KEY_ESCAPE;
            case "UP": return GLFW.GLFW_KEY_UP;
            case "DOWN": return GLFW.GLFW_KEY_DOWN;
            case "LEFT": return GLFW.GLFW_KEY_LEFT;
            case "RIGHT": return GLFW.GLFW_KEY_RIGHT;
            case "BACKSPACE": return GLFW.GLFW_KEY_BACKSPACE;
            case "DELETE": return GLFW.GLFW_KEY_DELETE;
            case "INSERT": return GLFW.GLFW_KEY_INSERT;
            case "HOME": return GLFW.GLFW_KEY_HOME;
            case "END": return GLFW.GLFW_KEY_END;
            case "PAGEUP": return GLFW.GLFW_KEY_PAGE_UP;
            case "PAGEDOWN": return GLFW.GLFW_KEY_PAGE_DOWN;

            case "MOUSELEFT":
            case "MOUSE_1":
            case "MBUTTON1":
            case "LEFTCLICK": return GLFW.GLFW_MOUSE_BUTTON_LEFT;

            case "MOUSERIGHT":
            case "MOUSE_2":
            case "MBUTTON2":
            case "RIGHTCLICK": return GLFW.GLFW_MOUSE_BUTTON_RIGHT;

            case "MOUSEMIDDLE":
            case "MOUSE_3":
            case "MBUTTON3":
            case "MIDDLECLICK": return GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

            case "MOUSE4": return GLFW.GLFW_MOUSE_BUTTON_4;
            case "MOUSE5": return GLFW.GLFW_MOUSE_BUTTON_5;
        }

        if (keyName.length() == 1) {
            char c = keyName.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                return GLFW.GLFW_KEY_A + (c - 'A');
            }
            if (c >= '0' && c <= '9') {
                return GLFW.GLFW_KEY_0 + (c - '0');
            }
        }

        return -1;
    }

    public static String getKeyName(int keyCode) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCTRL";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_ALT: return "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "RALT";
            case GLFW.GLFW_KEY_SPACE: return "SPACE";
            case GLFW.GLFW_KEY_ENTER: return "ENTER";
            case GLFW.GLFW_KEY_TAB: return "TAB";
            case GLFW.GLFW_KEY_ESCAPE: return "ESC";
            case GLFW.GLFW_KEY_UP: return "UP";
            case GLFW.GLFW_KEY_DOWN: return "DOWN";
            case GLFW.GLFW_KEY_LEFT: return "LEFT";
            case GLFW.GLFW_KEY_RIGHT: return "RIGHT";
            case GLFW.GLFW_KEY_BACKSPACE: return "BACKSPACE";
            case GLFW.GLFW_KEY_DELETE: return "DELETE";
            case GLFW.GLFW_KEY_INSERT: return "INSERT";
            case GLFW.GLFW_KEY_HOME: return "HOME";
            case GLFW.GLFW_KEY_END: return "END";
            case GLFW.GLFW_KEY_PAGE_UP: return "PAGEUP";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "PAGEDOWN";

            case GLFW.GLFW_MOUSE_BUTTON_LEFT: return "MOUSELEFT";
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT: return "MOUSERIGHT";
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE: return "MOUSEMIDDLE";
            case GLFW.GLFW_MOUSE_BUTTON_4: return "MOUSE4";
            case GLFW.GLFW_MOUSE_BUTTON_5: return "MOUSE5";
        }

        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char)('A' + (keyCode - GLFW.GLFW_KEY_A)));
        }
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            return String.valueOf((char)('0' + (keyCode - GLFW.GLFW_KEY_0)));
        }

        if (keyCode == -1)
            return "NONE";

        return "KEY_" + keyCode;
    }

}