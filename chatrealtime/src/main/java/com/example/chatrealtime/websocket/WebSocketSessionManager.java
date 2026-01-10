package com.example.chatrealtime.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    // userId -> session
    public ConcurrentHashMap<Integer, WebSocketSession> users = new ConcurrentHashMap<>();

    // userId -> roomId
    public ConcurrentHashMap<Integer, Integer> userRooms = new ConcurrentHashMap<>();
}
