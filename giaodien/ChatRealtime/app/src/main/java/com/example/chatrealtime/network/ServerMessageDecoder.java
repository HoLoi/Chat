package com.example.chatrealtime.network;

import java.nio.charset.StandardCharsets;

public final class ServerMessageDecoder {
    private ServerMessageDecoder() {
    }

    public static String normalize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        if (!looksLikeMojibake(message)) {
            return message;
        }

        try {
            return new String(message.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return message;
        }
    }

    private static boolean looksLikeMojibake(String value) {
        return value.contains("Ã")
                || value.contains("Â")
                || value.contains("Ä")
                || value.contains("Å")
                || value.contains("Æ")
                || value.contains("áº")
                || value.contains("á»")
                || value.contains("�");
    }
}