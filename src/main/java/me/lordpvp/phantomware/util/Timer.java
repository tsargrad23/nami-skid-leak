package me.lordpvp.phantomware.util;

public class Timer {
    private long start = System.currentTimeMillis();

    public void reset() {
        start = System.currentTimeMillis();
    }

    public boolean hasElapsed(long ms) {
        return System.currentTimeMillis() - start >= ms;
    }
}
