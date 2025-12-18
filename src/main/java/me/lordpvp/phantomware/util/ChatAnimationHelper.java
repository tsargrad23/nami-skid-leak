package me.lordpvp.phantomware.util;

public class ChatAnimationHelper {
    private static float animationOffset = 0f;
    private static final float MAX_OFFSET = 14f;
    private static long lastUpdateTime = System.currentTimeMillis();
    private static boolean chatOpen = false;

    public static void setChatOpen(boolean open) {
        chatOpen = open;
    }

    public static void tick() {
        long now = System.currentTimeMillis();
        float delta = (now - lastUpdateTime) / 1000f;
        lastUpdateTime = now;

        float speed = 60f;

        if (chatOpen) {
            animationOffset = Math.min(animationOffset + delta * speed, MAX_OFFSET);
        } else {
            animationOffset = Math.max(animationOffset - delta * speed, 0f);
        }
    }

    public static float getAnimationOffset() {
        return animationOffset;
    }

    public static void setAnimationOffset(float offset) {
        animationOffset = offset;
    }
}
