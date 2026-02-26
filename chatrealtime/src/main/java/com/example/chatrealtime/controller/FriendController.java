package com.example.chatrealtime.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.repository.BanBeRepository;
import com.example.chatrealtime.service.FriendService;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    private final BanBeRepository banBeRepo;
    private final FriendService friendService;

    public FriendController(BanBeRepository banBeRepo, FriendService friendService) {

        this.banBeRepo = banBeRepo;
        this.friendService = friendService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchFriends(
            @RequestParam Integer maTaiKhoan,
            @RequestParam String keyword
    ) {
        if (maTaiKhoan == null || maTaiKhoan <= 0) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Thiếu mã tài khoản"));
        }
        var results = friendService.searchAcceptedFriends(maTaiKhoan, keyword);
        return ResponseEntity.ok(Map.of("status", "success", "data", results));
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
        if (!banBeRepo.existsRelation(myId, friendId)) {
            return ResponseEntity.ok(Map.of("status", "not_friend"));
        }

        var statuses = banBeRepo.findStatuses(myId, friendId);

        if (statuses.contains("dongy"))
            return ResponseEntity.ok(Map.of("status", "friend"));

        if (statuses.contains("cho"))
            return ResponseEntity.ok(Map.of("status", "pending"));

        return ResponseEntity.ok(Map.of("status", "not_friend"));
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
        try {
            friendService.sendRequest(myId, friendId);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()
                    ));
        }
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

    @PostMapping("/respond")
    public ResponseEntity<?> respond(
            @RequestParam Integer maTaiKhoan1,
            @RequestParam Integer maTaiKhoan2,
            @RequestParam String action
    ) {
        try {
            if ("chap_nhan".equals(action)) {
                friendService.acceptRequest(maTaiKhoan1, maTaiKhoan2);
                return ResponseEntity.ok(
                        Map.of(
                                "status", "success",
                                "message", "Đã chấp nhận lời mời kết bạn"
                        )
                );
            }

            if ("tu_choi".equals(action)) {
                friendService.rejectRequest(maTaiKhoan1, maTaiKhoan2);
                return ResponseEntity.ok(
                        Map.of(
                                "status", "success",
                                "message", "Đã từ chối lời mời kết bạn"
                        )
                );
            }

            return ResponseEntity.badRequest().body(
                    Map.of("status", "error", "message", "Hành động không hợp lệ")
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", "error", "message", e.getMessage())
            );
        }
    }

    @GetMapping("/suggest")
    public ResponseEntity<?> suggest(@RequestParam Integer maTaiKhoan) {
        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "suggestions", banBeRepo.suggestFriends(maTaiKhoan)
                )
        );
    }


}
