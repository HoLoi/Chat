package com.example.chatrealtime.controller;

import com.example.chatrealtime.entity.PhongChat;
import com.example.chatrealtime.entity.ThanhVienPhong;
import com.example.chatrealtime.repository.PhongChatRepository;
import com.example.chatrealtime.repository.ThanhVienPhongRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatRoomController {

    private final ThanhVienPhongRepository thanhVienPhongRepo;
    private final PhongChatRepository phongChatRepo;

    public ChatRoomController(
            ThanhVienPhongRepository thanhVienPhongRepo,
            PhongChatRepository phongChatRepo
    ) {
        this.thanhVienPhongRepo = thanhVienPhongRepo;
        this.phongChatRepo = phongChatRepo;
    }

    // ================== XÓA PHÒNG (PHÍA NGƯỜI DÙNG) ==================
    @PostMapping("/delete-room")
    public ResponseEntity<?> deleteRoom(
            @RequestParam Integer maPhongChat,
            @RequestParam Integer maTaiKhoan
    ) {
        if (maPhongChat == null || maTaiKhoan == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Thiếu dữ liệu"));
        }

        int updated = thanhVienPhongRepo.updateNgayXoa(maPhongChat, maTaiKhoan);

        if (updated > 0) {
            return ResponseEntity.ok(
                    Map.of("status", "success", "message", "Đã xóa cuộc trò chuyện phía bạn")
            );
        }

        return ResponseEntity.status(500)
                .body(Map.of("status", "error", "message", "Không thể xóa"));
    }

    // ================== TẠO PHÒNG CHAT ==================
    @PostMapping("/create-room")
    @Transactional
    public ResponseEntity<?> createRoom(
            @RequestParam(required = false) String tenPhong,
            @RequestParam List<Integer> members,
            @RequestParam Integer currentUserId
    ) {
        // 1-1: kiểm tra phòng đã tồn tại chưa
        Integer exist = phongChatRepo.findOneToOne(
                currentUserId,
                members.size() == 1 ? members.get(0) : null
        );

        if (exist != null) {
            return ResponseEntity.ok(
                    Map.of("status", "success", "maPhongChat", exist)
            );
        }

        // Tạo phòng mới
        PhongChat p = new PhongChat();
        p.setLoaiPhong((members.size() > 1 ? 1 : 0));
        p.setTenPhongChat(tenPhong != null ? tenPhong : "Chat");
        p.setMaTaiKhoanTao(currentUserId);
        phongChatRepo.save(p);

        // Người tạo = admin
        thanhVienPhongRepo.save(
                new ThanhVienPhong(p.getMaPhongChat(), currentUserId, "admin")
        );

        // Các thành viên còn lại
        for (Integer m : members) {
            if (!m.equals(currentUserId)) {
                thanhVienPhongRepo.save(
                        new ThanhVienPhong(p.getMaPhongChat(), m, "member")
                );
            }
        }

        return ResponseEntity.ok(
                Map.of("status", "success", "maPhongChat", p.getMaPhongChat())
        );
    }

    // ================== LẤY DANH SÁCH THÀNH VIÊN ==================
    @GetMapping("/room-members")
    public ResponseEntity<?> getMembers(@RequestParam Integer maPhongChat) {
        return ResponseEntity.ok(
                Map.of(
                        "status", "success",
                        "members", thanhVienPhongRepo.getMembers(maPhongChat)
                )
        );
    }
}
