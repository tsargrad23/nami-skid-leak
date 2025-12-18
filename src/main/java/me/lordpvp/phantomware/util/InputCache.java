package me.lordpvp.phantomware.util;

public final class InputCache { //  yes we need this due to move fix

    public static boolean forward;
    public static boolean back;
    public static boolean left;
    public static boolean right;

    private InputCache() {}

    public static void update(boolean forwardKey, boolean backKey, boolean leftKey, boolean rightKey) {
        forward = forwardKey;
        back = backKey;
        left = leftKey;
        right = rightKey;
    }

    public static boolean isMoving() {return forward || back || left || right;}
}
