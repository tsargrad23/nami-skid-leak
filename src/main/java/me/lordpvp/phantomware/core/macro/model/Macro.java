package me.kiriyaga.nami.core.macro.model;

public class Macro {
    private final int keyCode;
    private final String message;

    public Macro(int keyCode, String message) {
        this.keyCode = keyCode;
        this.message = message;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public String getMessage() {
        return message;
    }
}
