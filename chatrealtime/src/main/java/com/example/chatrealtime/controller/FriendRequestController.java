package com.example.chatrealtime.controller;

import com.example.chatrealtime.service.FriendRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendRequestController {
    private final FriendRequestService service;

    public FriendRequestController(FriendRequestService service) {
        this.service = service;
    }

    @PostMapping("/respond")
    public ResponseEntity<?> respond(
            @RequestParam Integer maTaiKhoan1,
            @RequestParam Integer maTaiKhoan2,
            @RequestParam String action
    ) {

        if ("chap_nhan".equals(action)) {
            service.acceptRequest(maTaiKhoan1, maTaiKhoan2);
            return ResponseEntity.ok(
                    Map.of("status", "success", "message", "Đã trở thành bạn bè")
            );
        }

        if ("tu_choi".equals(action)) {
            service.rejectRequest(maTaiKhoan1, maTaiKhoan2);
            return ResponseEntity.ok(
                    Map.of("status", "success", "message", "Đã từ chối lời mời")
            );
        }

        return ResponseEntity.badRequest()
                .body(Map.of("status", "error", "message", "Hành động không hợp lệ"));
    }
}
