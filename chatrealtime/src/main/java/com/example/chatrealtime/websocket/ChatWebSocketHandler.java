package com.example.chatrealtime.websocket;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.chatrealtime.dto.SocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final WebSocketSessionManager manager;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatWebSocketHandler(WebSocketSessionManager manager) {
        this.manager = manager;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        SocketMessage data = mapper.readValue(message.getPayload(), SocketMessage.class);

        switch (data.type) {

            // ==== init ====
            case "init":
                manager.users.put(data.userId, session);
                send(session, "init", "User " + data.userId + " registered");
                break;

            // ==== join_room ====
            case "join_room":
                manager.userRooms.put(data.userId, data.roomId);
                break;

            // ==== chat_message ====
            case "chat_message":
                manager.userRooms.forEach((uid, rid) -> {
                    if (rid.equals(data.maPhongChat)) {
                        WebSocketSession s = manager.users.get(uid);
                        if (s != null && s.isOpen()) {
                            try {
                                sendChat(s, data);
                            } catch (Exception ignored) {}
                        }
                    }
                });
                break;

            // ==== friend_request ====
            case "friend_request":
                sendToUser(data.toUser, data);
                break;

            case "friend_accepted":
                sendToUser(data.toUser, data);
                sendToUser(data.fromUser, data);
                break;
        }
    }

    private void send(WebSocketSession session, String msgType, String msg) throws Exception {
        session.sendMessage(new TextMessage(
                mapper.writeValueAsString(
                        new Object() {
                            public final String type = msgType;
                            public final String message = msg;
                        }
                )
        ));
    }


    private void sendChat(WebSocketSession session, SocketMessage data) throws Exception {
        session.sendMessage(new TextMessage(
                mapper.writeValueAsString(
                        new Object() {
                            public final String type = "chat_message";
                            public final Integer maPhongChat = data.maPhongChat;
                            public final Integer maTaiKhoanGui = data.maTaiKhoanGui;
                            public final String noiDung = data.noiDung;
                            public final String loaiTinNhan = data.loaiTinNhan;
                            public final String duongDanFile = data.duongDanFile;
                            public final String thoiGianGui = LocalDateTime.now().toString();
                        }
                )
        ));
    }

    private void sendToUser(Integer userId, SocketMessage data) throws Exception {
        WebSocketSession s = manager.users.get(userId);
        if (s != null && s.isOpen()) {
            s.sendMessage(new TextMessage(mapper.writeValueAsString(data)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        manager.users.entrySet().removeIf(e -> e.getValue().equals(session));
        manager.userRooms.entrySet().removeIf(e -> e.getKey().equals(session));
    }
}
