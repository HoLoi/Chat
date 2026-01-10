package com.example.chatrealtime.controller;

import com.example.chatrealtime.repository.BanBeRepository;
import com.example.chatrealtime.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    private final BanBeRepository banBeRepo;
    private final FriendService friendService;

    public FriendController(BanBeRepository banBeRepo, FriendService friendService) {

        this.banBeRepo = banBeRepo;
        this.friendService = friendService;
    }

    @GetMapping
    public ResponseEntity<?> getFriends(@RequestParam Integer maTaiKhoan) {

        if (maTaiKhoan == null || maTaiKhoan <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Thiếu mã tài khoản"));
        }

        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "friends", banBeRepo.getFriends(maTaiKhoan)
                )
        );
    }

    @GetMapping("/status")
    public ResponseEntity<?> checkStatus(
            @RequestParam Integer myId,
            @RequestParam Integer friendId
    ) {
        String status = banBeRepo.findStatus(myId, friendId)
                .map(s -> {
                    if ("cho".equals(s)) return "pending";
                    if ("dongy".equals(s)) return "friend";
                    return "not_friend";
                })
                .orElse("not_friend");

        return ResponseEntity.ok(Map.of("status", status));
    }

    @GetMapping("/requests")
    public List<Map<String, Object>> getFriendRequests(
            @RequestParam Integer maTaiKhoan
    ) {
        return banBeRepo.getFriendRequests(maTaiKhoan);
    }

    @PostMapping("/send-request")
    public ResponseEntity<?> sendRequest(
            @RequestParam Integer myId,
            @RequestParam Integer friendId
    ) {
        friendService.sendRequest(myId, friendId);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/cancel-request")
    public ResponseEntity<?> cancelRequest(
            @RequestParam Integer myId,
            @RequestParam Integer friendId
    ) {
        friendService.cancelRequest(myId, friendId);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/unfriend")
    public ResponseEntity<?> unfriend(
            @RequestParam Integer myId,
            @RequestParam Integer friendId
    ) {
        friendService.unfriend(myId, friendId);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

}
