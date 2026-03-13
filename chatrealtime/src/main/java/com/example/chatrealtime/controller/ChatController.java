package com.example.chatrealtime.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.repository.TrangThaiTinNhanRepository;
import com.example.chatrealtime.websocket.ChatWebSocketHandler;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final TrangThaiTinNhanRepository trangThaiTinNhanRepo;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatController(TrangThaiTinNhanRepository repo, ChatWebSocketHandler chatWebSocketHandler) {
        this.trangThaiTinNhanRepo = repo;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @PostMapping("/mark-read")
    public ResponseEntity<?> markRead(
            @RequestParam Integer maTaiKhoan, 
            @RequestParam Integer maPhongChat
    ) {
        int updated = trangThaiTinNhanRepo.markRead(maTaiKhoan, maPhongChat);
        // Chi push khi co thay doi trang thai thuc su de tranh loop mark-read <-> websocket.
        if (updated > 0) {
            chatWebSocketHandler.pushMessageStatusUpdate(maPhongChat);
        }
        return ResponseEntity.ok(
            Map.of("status", "success", "updated", updated)
        );
    }
}
