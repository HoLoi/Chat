package com.example.chatrealtime.controller;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.chatrealtime.entity.TinNhan;
import com.example.chatrealtime.service.MessageService;

@RestController
@RequestMapping("/api/chat")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // ===== SEND MESSAGE =====
    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessage(
            @RequestParam Integer maPhongChat,
            @RequestParam Integer maTaiKhoanGui,
            @RequestParam(required = false) String noiDung,
            @RequestParam(defaultValue = "text") String loaiTinNhan,
            @RequestParam(required = false) String duongDanFile
    ) {
        if ((noiDung == null || noiDung.isBlank()) && duongDanFile == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Thiếu nội dung"));
        }

        TinNhan t = messageService.sendMessage(
                maPhongChat, maTaiKhoanGui,
                noiDung, loaiTinNhan, duongDanFile
        );

        return ResponseEntity.ok(Map.of("status", "success", "message", t));
    }


        // ===== UPLOAD FILE (ẢNH/VIDEO) =====
        @PostMapping("/upload-file")
        public ResponseEntity<?> uploadFile(
                        @RequestParam Integer maPhongChat,
                        @RequestParam Integer maTaiKhoanGui,
                        @RequestParam("file") MultipartFile file,
                        @RequestParam(required = false, defaultValue = "") String loaiTinNhan
        ) {
                if (file == null || file.isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("status", "error", "message", "Thiếu file"));
                }

                try {
                        String projectDir = System.getProperty("user.dir");
                        File uploadDir = new File(projectDir + "/uploads/chat");
                        if (!uploadDir.exists()) uploadDir.mkdirs();

                        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
                        String ext = "";
                        int dot = originalName.lastIndexOf('.');
                        if (dot != -1) ext = originalName.substring(dot);

                        String savedName = UUID.randomUUID() + ext;
                        File dest = new File(uploadDir, savedName);
                        file.transferTo(dest);

                        String mime = file.getContentType() != null ? file.getContentType() : "";
                        String detectedType = loaiTinNhan;
                        if (detectedType == null || detectedType.isBlank()) {
                                if (mime.startsWith("image")) detectedType = "image";
                                else if (mime.startsWith("video")) detectedType = "video";
                                else detectedType = "file";
                        }

                        String fileUrl = "/uploads/chat/" + savedName;

                        // Lưu tin nhắn dạng file
                        TinNhan t = messageService.sendMessage(
                                        maPhongChat,
                                        maTaiKhoanGui,
                                        "",
                                        detectedType,
                                        fileUrl
                        );

                        return ResponseEntity.ok(
                                        Map.of(
                                                        "status", "success",
                                                        "loaiTinNhan", detectedType,
                                                        "duongDanFile", fileUrl,
                                                        "mimeType", mime,
                                                        "message", t
                                        )
                        );
                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.internalServerError()
                                        .body(Map.of("status", "error", "message", "Upload thất bại"));
                }
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
                        messageService.getMessagesWithSender(maPhongChat, maTaiKhoan)
                )
        );
    }

}

