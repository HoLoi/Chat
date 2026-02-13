package com.example.chatrealtime.status;

public class AppVisibility {
    private static volatile boolean isForeground = false;

    public static void setForeground(boolean value) {
        isForeground = value;
    }

    public static boolean isForeground() {
        return isForeground;
    }
}
