package com.example.chatrealtime.controller;

import com.example.chatrealtime.entity.TinNhan;
import com.example.chatrealtime.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // ===== SEND MESSAGE =====
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @RequestParam Integer maPhongChat,
            @RequestParam(required = false) String noiDung,
            @RequestParam(defaultValue = "text") String loaiTinNhan,
            @RequestParam(required = false) String duongDanFile,
            @RequestAttribute("maTaiKhoan") Integer maTaiKhoan
    ) {
        if ((noiDung == null || noiDung.isBlank())
                && duongDanFile == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error",
                            "message", "Thiếu nội dung"));
        }

        TinNhan t = messageService.sendMessage(
                maPhongChat, maTaiKhoan,
                noiDung, loaiTinNhan, duongDanFile
        );

        return ResponseEntity.ok(
                Map.of("status", "success", "message", t)
        );
    }

    // ===== GET MESSAGES =====
    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(
            @RequestParam Integer maPhongChat,
            @RequestParam Integer maTaiKhoan
    ) {
        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "messages",
                        messageService.getMessages(maPhongChat, maTaiKhoan)
                )
        );
    }

}

