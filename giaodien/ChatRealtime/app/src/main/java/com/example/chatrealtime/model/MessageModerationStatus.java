package com.example.chatrealtime.model;

public enum MessageModerationStatus {
    CLEAN,
    WARNING,
    BLOCK,
    BANNED;

    public static MessageModerationStatus from(String status) {
        if (status == null) return CLEAN;
        try {
            return MessageModerationStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception ignored) {
            return CLEAN;
        }
    }
}
