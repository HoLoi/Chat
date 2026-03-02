package com.example.chatrealtime.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.service.GroupService;

@RestController
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/request-join")
    public ResponseEntity<?> requestJoin(@RequestParam Integer maPhongChat,
                                         @RequestParam Integer maTaiKhoan) {
        try {
            return ResponseEntity.ok(groupService.requestJoin(maPhongChat, maTaiKhoan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approve(@RequestParam Integer idYeuCau,
                                     @RequestParam(required = false) Integer maPhongChat,
                                     @RequestParam Integer nguoiXuLy,
                                     @RequestParam String action,
                                     @RequestParam(required = false) String lyDo) {
        try {
            boolean accept = "approve".equalsIgnoreCase(action) || "chap_nhan".equalsIgnoreCase(action);
            return ResponseEntity.ok(groupService.approveRequest(idYeuCau, maPhongChat, nguoiXuLy, accept, lyDo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> pending(@RequestParam Integer maPhongChat,
                                     @RequestParam Integer maTaiKhoan) {
        return ResponseEntity.ok(groupService.pendingRequests(maPhongChat, maTaiKhoan));
    }
}
