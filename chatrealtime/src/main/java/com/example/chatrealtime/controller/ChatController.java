package com.example.chatrealtime.controller;

import com.example.chatrealtime.repository.TrangThaiTinNhanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final TrangThaiTinNhanRepository trangThaiTinNhanRepo;

    public ChatController(TrangThaiTinNhanRepository repo) {
        this.trangThaiTinNhanRepo = repo;
    }

    @PostMapping("/mark-read")
    public ResponseEntity<?> markRead(
            @RequestParam Integer maTaiKhoan, 
            @RequestParam Integer maPhongChat
    ) {
        int updated = trangThaiTinNhanRepo.markRead(maTaiKhoan, maPhongChat);
        return ResponseEntity.ok(
                Map.of("status", "success", "updated", updated)
        );
    }
}
