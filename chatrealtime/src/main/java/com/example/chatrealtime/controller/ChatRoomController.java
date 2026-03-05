package com.example.chatrealtime.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.entity.PhongChat;
import com.example.chatrealtime.entity.ThanhVienPhong;
import com.example.chatrealtime.entity.ThanhVienPhongId;
import com.example.chatrealtime.repository.PhongChatRepository;
import com.example.chatrealtime.repository.TaiKhoanRepository;
import com.example.chatrealtime.repository.ThanhVienPhongRepository;
import com.example.chatrealtime.service.FriendService;
import com.example.chatrealtime.service.PrivateChatService;
import com.example.chatrealtime.service.RoomQueryService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/chat")
public class ChatRoomController {

        private static final Logger log = LoggerFactory.getLogger(ChatRoomController.class);

        private final ThanhVienPhongRepository thanhVienPhongRepo;
        private final PhongChatRepository phongChatRepo;
        private final TaiKhoanRepository taiKhoanRepo;
        private final PrivateChatService privateChatService;
        private final RoomQueryService roomQueryService;
        private final FriendService friendService;

    public ChatRoomController(
            ThanhVienPhongRepository thanhVienPhongRepo,
            PhongChatRepository phongChatRepo,
            TaiKhoanRepository taiKhoanRepo,
                        PrivateChatService privateChatService,
                        RoomQueryService roomQueryService,
                        FriendService friendService
    ) {
        this.thanhVienPhongRepo = thanhVienPhongRepo;
        this.phongChatRepo = phongChatRepo;
        this.taiKhoanRepo = taiKhoanRepo;
        this.privateChatService = privateChatService;
                this.roomQueryService = roomQueryService;
                this.friendService = friendService;
    }

        @GetMapping("/search")
        public ResponseEntity<?> searchChats(
                        @RequestParam Integer myId,
                        @RequestParam String keyword
        ) {
                if (myId == null || myId <= 0) {
                        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Thiếu mã tài khoản"));
                }

                String kw = keyword != null ? keyword.trim() : "";

                var rooms = roomQueryService.searchRooms(myId, kw);
                var friends = friendService.searchAcceptedFriends(myId, kw);

                List<Map<String, Object>> results = new ArrayList<>();

                rooms.forEach(r -> {
                        Map<String, Object> item = new java.util.HashMap<>(r);
                        item.put("type", "room");
                        results.add(item);
                });

                friends.forEach(f -> {
                        Map<String, Object> item = new java.util.HashMap<>(f);
                        item.put("type", "friend");
                        results.add(item);
                });

                return ResponseEntity.ok(Map.of(
                                "status", "success",
                                "results", results
                ));
        }

    @PostMapping("/private/{friendId}")
    public ResponseEntity<?> openPrivateChat(
            @RequestParam Integer myId,
            @PathVariable Integer friendId
    ) {
        try {
            Integer roomId = privateChatService.openOrCreate(myId, friendId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "roomId", roomId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Open private chat failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "Không thể mở phòng"));
        }
    }

    // ================== THÔNG TIN PHÒNG + THÀNH VIÊN ==================
    @GetMapping("/room-info")
    public ResponseEntity<?> roomInfo(@RequestParam Integer maPhongChat,
                                       @RequestParam Integer maTaiKhoan) {
        PhongChat room = phongChatRepo.findById(maPhongChat)
                .orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Không tìm thấy phòng"));
        }

        // Bỏ ràng buộc ngayXoa cho phòng 1-1 để vẫn lấy avatar người đối diện
        boolean isOneToOne = room.getLoaiPhong() != null && room.getLoaiPhong() == 0;
        boolean isMember = isOneToOne
                ? thanhVienPhongRepo.existsByIdMaPhongChatAndIdMaTaiKhoan(maPhongChat, maTaiKhoan)
                : thanhVienPhongRepo.existsByIdMaPhongChatAndIdMaTaiKhoanAndNgayXoaIsNull(maPhongChat, maTaiKhoan);
        if (!isMember) {
            return ResponseEntity.status(403)
                    .body(Map.of("status", "error", "message", "Bạn không ở trong phòng"));
        }

        Map<String, Object> roomMap = new java.util.HashMap<>();
        roomMap.put("maPhongChat", room.getMaPhongChat());
        roomMap.put("tenPhongChat", room.getTenPhongChat());
        roomMap.put("loaiPhong", room.getLoaiPhong());
        roomMap.put("kieuNhom", room.getKieuNhom());
        roomMap.put("maTruongNhom", room.getMaTruongNhom());
        roomMap.put("maTaiKhoanTao", room.getMaTaiKhoanTao());
        roomMap.put("anhDaiDienUrl", room.getAnhDaiDienUrl());

        Map<String, Object> res = new java.util.HashMap<>();
        res.put("status", "success");
        res.put("room", roomMap);
        res.put("members", isOneToOne
                ? thanhVienPhongRepo.getMembersIncludeDeleted(maPhongChat)
                : thanhVienPhongRepo.getMembers(maPhongChat));

        return ResponseEntity.ok(res);
    }

        // ================== XÓA PHÒNG / GIẢI TÁN NHÓM ==================
    @PostMapping("/delete-room")
        @Transactional
    public ResponseEntity<?> deleteRoom(
            @RequestParam Integer maPhongChat,
            @RequestParam Integer maTaiKhoan
    ) {
        if (maPhongChat == null || maTaiKhoan == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Thiếu dữ liệu"));
        }

                PhongChat room = phongChatRepo.findById(maPhongChat).orElse(null);
                if (room == null) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("status", "error", "message", "Không tìm thấy phòng"));
                }

                Integer loaiPhong = room.getLoaiPhong();
                boolean isGroup = loaiPhong != null && loaiPhong == 1;

                if (isGroup) {
                        Integer leader = room.getMaTruongNhom() != null ? room.getMaTruongNhom() : room.getMaTaiKhoanTao();
                        if (!Objects.equals(leader, maTaiKhoan)) {
                                return ResponseEntity.status(403)
                                                .body(Map.of("status", "error", "message", "Chỉ trưởng nhóm mới được giải tán nhóm"));
                        }

                        phongChatRepo.delete(room);
                        return ResponseEntity.ok(Map.of("status", "success", "message", "Đã giải tán nhóm"));
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

    // ================== CẬP NHẬT PHÒNG (đổi tên / kiểu nhóm) ==================
    @PostMapping("/update-room")
    @Transactional
    public ResponseEntity<?> updateRoom(
            @RequestParam Integer maPhongChat,
            @RequestParam Integer maTaiKhoan,
            @RequestParam(required = false) String tenPhong,
            @RequestParam(required = false) Integer kieuNhom,
            @RequestParam(required = false) String anhDaiDien
    ) {
        PhongChat room = phongChatRepo.findById(maPhongChat)
                .orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Không tìm thấy phòng"));
        }

        Integer leader = room.getMaTruongNhom() != null ? room.getMaTruongNhom() : room.getMaTaiKhoanTao();
        if (!Objects.equals(leader, maTaiKhoan)) {
            return ResponseEntity.status(403)
                    .body(Map.of("status", "error", "message", "Bạn không có quyền chỉnh sửa"));
        }

                if (tenPhong != null && !tenPhong.isBlank()) {
                        room.setTenPhongChat(tenPhong.trim());
                }
                if (kieuNhom != null) {
                        room.setKieuNhom(kieuNhom);
                }
                if (anhDaiDien != null && !anhDaiDien.isBlank()) {
                        room.setAnhDaiDienUrl(normalizeAvatarPath(anhDaiDien));
                }
        phongChatRepo.save(room);
        Map<String, Object> roomMap = new java.util.HashMap<>();
        roomMap.put("maPhongChat", room.getMaPhongChat());
        roomMap.put("tenPhongChat", room.getTenPhongChat());
        roomMap.put("loaiPhong", room.getLoaiPhong());
        roomMap.put("kieuNhom", room.getKieuNhom());
        roomMap.put("maTruongNhom", room.getMaTruongNhom());
        roomMap.put("maTaiKhoanTao", room.getMaTaiKhoanTao());
        roomMap.put("anhDaiDienUrl", room.getAnhDaiDienUrl());

        Map<String, Object> res = new java.util.HashMap<>();
        res.put("status", "success");
        res.put("room", roomMap);

        return ResponseEntity.ok(res);
    }

        // ================== XÓA THÀNH VIÊN (trưởng nhóm, xóa cứng) ==================
    @PostMapping("/remove-member")
    @Transactional
    public ResponseEntity<?> removeMember(
            @RequestParam Integer maPhongChat,
            @RequestParam Integer maTaiKhoan,
            @RequestParam Integer memberId
    ) {
        PhongChat room = phongChatRepo.findById(maPhongChat)
                .orElse(null);
        if (room == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Không tìm thấy phòng"));
        }

                Integer leader = room.getMaTruongNhom() != null ? room.getMaTruongNhom() : room.getMaTaiKhoanTao();

                // Thành viên tự rời nhóm
                if (Objects.equals(maTaiKhoan, memberId)) {
                        if (Objects.equals(leader, maTaiKhoan)) {
                                return ResponseEntity.badRequest()
                                                .body(Map.of("status", "error", "message", "Trưởng nhóm không thể rời nhóm trực tiếp"));
                        }

                        int left = thanhVienPhongRepo.removeMember(maPhongChat, maTaiKhoan);
                        if (left > 0) {
                                return ResponseEntity.ok(Map.of("status", "success", "message", "Đã rời nhóm"));
                        }
                        return ResponseEntity.badRequest()
                                        .body(Map.of("status", "error", "message", "Bạn không còn trong nhóm"));
                }

                // Trưởng nhóm xóa thành viên khác
                if (!Objects.equals(leader, maTaiKhoan)) {
                        return ResponseEntity.status(403)
                                        .body(Map.of("status", "error", "message", "Bạn không có quyền xóa thành viên"));
                }

                if (Objects.equals(leader, memberId)) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("status", "error", "message", "Không thể tự xóa trưởng nhóm"));
                }

        int updated = thanhVienPhongRepo.removeMember(maPhongChat, memberId);
        if (updated > 0) {
            return ResponseEntity.ok(Map.of("status", "success"));
        }
        return ResponseEntity.status(500)
                .body(Map.of("status", "error", "message", "Không thể xóa thành viên"));
    }

        // ================== THÊM THÀNH VIÊN (thành viên thường + trưởng nhóm) ==================
        @PostMapping("/add-member")
        @Transactional
        public ResponseEntity<?> addMember(
                        @RequestParam Integer maPhongChat,
                        @RequestParam Integer maTaiKhoan,
                        @RequestParam Integer memberId
        ) {
                try {
                        if (!taiKhoanRepo.existsById(memberId)) {
                                return ResponseEntity.ok(Map.of("status", "error", "message", "Tài khoản cần thêm không tồn tại"));
                        }

                        PhongChat room = phongChatRepo.findById(maPhongChat).orElse(null);
                        if (room == null) {
                                return ResponseEntity.ok(Map.of("status", "error", "message", "Không tìm thấy phòng"));
                        }

                        Integer leader = room.getMaTruongNhom() != null ? room.getMaTruongNhom() : room.getMaTaiKhoanTao();
                        boolean isLeader = Objects.equals(leader, maTaiKhoan);
                        boolean isPrivate = room.getKieuNhom() != null && room.getKieuNhom() == 1;

                        ThanhVienPhongId id = new ThanhVienPhongId(maPhongChat, memberId);
                        ThanhVienPhong existed = thanhVienPhongRepo.findById(id).orElse(null);

                        // Nếu đã là thành viên và chưa bị xóa
                        if (existed != null && existed.getNgayXoa() == null && "approved".equalsIgnoreCase(existed.getTrangThaiThamGia())) {
                                return ResponseEntity.ok(Map.of("status", "success", "message", "Đã là thành viên"));
                        }

                        // Phòng private + người thêm không phải trưởng nhóm -> tạo pending
                        if (isPrivate && !isLeader) {
                                ThanhVienPhong pending = existed != null ? existed : new ThanhVienPhong(maPhongChat, memberId, "member");
                                pending.setTrangThaiThamGia("pending");
                                pending.setNguoiDuyet(null);
                                pending.setNgayXoa(null);
                                thanhVienPhongRepo.save(pending);
                                return ResponseEntity.ok(Map.of("status", "pending", "message", "Đã gửi yêu cầu, chờ trưởng nhóm duyệt"));
                        }

                            // Public hoặc trưởng nhóm: thêm ngay
                            ThanhVienPhong tv = existed != null ? existed : new ThanhVienPhong(maPhongChat, memberId, "member");
                            tv.setTrangThaiThamGia("approved");
                            tv.setNguoiDuyet(maTaiKhoan); // người thực hiện thao tác
                        tv.setNgayXoa(null);
                        thanhVienPhongRepo.save(tv);
                        return ResponseEntity.ok(Map.of("status", "success", "message", "Thêm thành viên thành công"));
                } catch (Exception e) {
                        log.error("[add-member] Lỗi thêm thành viên", e);
                        return ResponseEntity.ok(Map.of("status", "error", "message", "Thêm thành viên thất bại"));
                }
        }

    // ================== TẠO PHÒNG CHAT ==================
    @PostMapping("/create-room")
    @Transactional
        public ResponseEntity<?> createRoom(
                        @RequestParam(required = false) String tenPhong,
                        @RequestParam String members,
                        @RequestParam Integer currentUserId,
                        @RequestParam(required = false, defaultValue = "0") Integer kieuNhom,
                            @RequestParam(required = false) String anhDaiDien
        ) {
                log.info("[create-room] currentUserId={} rawMembers={} kieuNhom={} tenPhong={} avatar={}", currentUserId, members, kieuNhom, tenPhong, anhDaiDien);
                List<Integer> rawMembers = parseMembers(members);
                List<Integer> memberList = new java.util.ArrayList<>();
                for (Integer m : rawMembers) {
                        if (taiKhoanRepo.existsById(m)) {
                                memberList.add(m);
                        } else {
                                log.warn("[create-room] Bỏ qua member không tồn tại: {}", m);
                        }
                }

                if (!taiKhoanRepo.existsById(currentUserId)) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("status", "error", "message", "Tài khoản hiện tại không hợp lệ"));
                }

                if (memberList.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Thiếu danh sách thành viên"));
        }
        // 1-1: kiểm tra phòng đã tồn tại chưa
        Integer exist = phongChatRepo.findOneToOne(
                currentUserId,
                memberList.size() == 1 ? memberList.get(0) : null
        );
        log.info("[create-room] existingOneToOne={}", exist);

        if (exist != null) {
            return ResponseEntity.ok(
                    Map.of("status", "success", "maPhongChat", exist)
            );
        }

        // Tạo phòng mới
        PhongChat p = new PhongChat();
        p.setLoaiPhong((memberList.size() > 1 ? 1 : 0));
                p.setTenPhongChat(tenPhong != null ? tenPhong : "Chat");
        p.setMaTaiKhoanTao(currentUserId);
        p.setKieuNhom(kieuNhom);
        p.setMaTruongNhom(currentUserId);
                if (anhDaiDien != null && !anhDaiDien.isBlank()) {
                        p.setAnhDaiDienUrl(normalizeAvatarPath(anhDaiDien));
                }
        phongChatRepo.save(p);

        // Người tạo = admin
        thanhVienPhongRepo.save(
                new ThanhVienPhong(p.getMaPhongChat(), currentUserId, "admin")
        );

        // Các thành viên còn lại
        for (Integer m : memberList) {
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

        private List<Integer> parseMembers(String membersRaw) {
                List<Integer> list = new java.util.ArrayList<>();
                if (membersRaw == null || membersRaw.isBlank()) return list;
                String cleaned = membersRaw.trim();
                if (cleaned.startsWith("[")) cleaned = cleaned.substring(1);
                if (cleaned.endsWith("]")) cleaned = cleaned.substring(0, cleaned.length() - 1);
                for (String part : cleaned.split(",")) {
                        String s = part.trim();
                        if (s.isEmpty()) continue;
                        try {
                                list.add(Integer.parseInt(s));
                        } catch (NumberFormatException ignored) {}
                }
                return list;
        }

        private String normalizeAvatarPath(String raw) {
                if (raw == null) return null;
                String trimmed = raw.trim();
                if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) return null;
                // Nếu là URL đầy đủ thì giữ nguyên
                if (trimmed.startsWith("http")) return trimmed;
                // Đảm bảo luôn có dấu /
                if (!trimmed.startsWith("/")) {
                        trimmed = "/" + trimmed;
                }
                return trimmed;
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
