package com.example.chatrealtime.controller;

import com.example.chatrealtime.service.RoomQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class RoomQueryController {
    private final RoomQueryService service;

    public RoomQueryController(RoomQueryService service) {
        this.service = service;
    }

    @GetMapping("/rooms")
    public ResponseEntity<?> getRooms(@RequestParam Integer maTaiKhoan) {
        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "rooms", service.getRooms(maTaiKhoan)
                )
        );
    }
}
