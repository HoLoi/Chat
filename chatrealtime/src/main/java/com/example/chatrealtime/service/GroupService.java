package com.example.chatrealtime.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chatrealtime.entity.PhongChat;
import com.example.chatrealtime.entity.ThanhVienPhong;
import com.example.chatrealtime.entity.ThanhVienPhongId;
import com.example.chatrealtime.repository.PhongChatRepository;
import com.example.chatrealtime.repository.ThanhVienPhongRepository;

@Service
public class GroupService {

    private final PhongChatRepository phongChatRepo;
    private final ThanhVienPhongRepository tvRepo;

    public GroupService(PhongChatRepository phongChatRepo,
                        ThanhVienPhongRepository tvRepo) {
        this.phongChatRepo = phongChatRepo;
        this.tvRepo = tvRepo;
    }

    @Transactional
    public Map<String, Object> requestJoin(Integer maPhongChat, Integer maTaiKhoan) {
        PhongChat room = phongChatRepo.findById(maPhongChat)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chat không tồn tại"));

        ThanhVienPhong existed = tvRepo.findById(new ThanhVienPhongId(maPhongChat, maTaiKhoan))
                .orElse(null);

        if (existed != null && existed.getNgayXoa() == null && "approved".equalsIgnoreCase(existed.getTrangThaiThamGia())) {
            return Map.of("status", "success", "message", "Đã là thành viên", "state", "approved");
        }

        Integer kieuNhomValue = room.getKieuNhom();
        int kieuNhom = kieuNhomValue != null ? kieuNhomValue : 0; // default public

        if (kieuNhom == 0) {
            ThanhVienPhong tv = existed != null ? existed : new ThanhVienPhong(maPhongChat, maTaiKhoan, "member");
            tv.setTrangThaiThamGia("approved");
            tv.setNguoiDuyet(room.getMaTruongNhom() != null ? room.getMaTruongNhom() : room.getMaTaiKhoanTao());
            tv.setNgayXoa(null);
            tvRepo.save(tv);
            return Map.of("status", "success", "state", "approved");
        }

        if (existed != null && existed.getNgayXoa() == null && "pending".equalsIgnoreCase(existed.getTrangThaiThamGia())) {
            return Map.of("status", "success", "state", "pending");
        }

        ThanhVienPhong pending = existed != null ? existed : new ThanhVienPhong(maPhongChat, maTaiKhoan, "member");
        pending.setTrangThaiThamGia("pending");
        pending.setNguoiDuyet(null);
        pending.setNgayXoa(null);
        tvRepo.save(pending);
        return Map.of("status", "success", "state", "pending");
    }

    @Transactional
    public Map<String, Object> approveRequest(Integer requestId,
                                               Integer maPhongChat,
                                               Integer nguoiXuLy,
                                               boolean accept,
                                               String lyDo) {
        if (maPhongChat == null) {
            throw new IllegalArgumentException("Thiếu mã phòng");
        }

        PhongChat room = phongChatRepo.findById(maPhongChat)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chat không tồn tại"));

        Integer leader = room.getMaTruongNhom() != null ? room.getMaTruongNhom() : room.getMaTaiKhoanTao();
        if (!Objects.equals(leader, nguoiXuLy)) {
            throw new IllegalArgumentException("Bạn không có quyền duyệt");
        }

        int updated;
        if (accept) {
            updated = tvRepo.approveMember(maPhongChat, requestId, nguoiXuLy);
        } else {
            updated = tvRepo.rejectMember(maPhongChat, requestId, nguoiXuLy);
        }

        if (updated <= 0) {
            throw new IllegalArgumentException("Không tìm thấy yêu cầu chờ");
        }

        return Map.of("status", "success", "state", accept ? "approved" : "rejected");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> pendingRequests(Integer maPhongChat, Integer requesterId) {
        PhongChat room = phongChatRepo.findById(maPhongChat)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chat không tồn tại"));

        Integer leader = room.getMaTruongNhom() != null ? room.getMaTruongNhom() : room.getMaTaiKhoanTao();
        if (!Objects.equals(leader, requesterId)) {
            return Map.of("status", "error", "message", "Bạn không có quyền xem yêu cầu");
        }

        List<Map<String, Object>> pending = new ArrayList<>(tvRepo.getPendingMembers(maPhongChat));

        return Map.of("status", "success", "requests", pending);
    }
}
