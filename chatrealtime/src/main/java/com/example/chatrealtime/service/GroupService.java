package com.example.chatrealtime.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chatrealtime.entity.PhongChat;
import com.example.chatrealtime.entity.ThanhVienPhong;
import com.example.chatrealtime.entity.YeuCauThamGiaNhom;
import com.example.chatrealtime.repository.PhongChatRepository;
import com.example.chatrealtime.repository.TaiKhoanRepository;
import com.example.chatrealtime.repository.ThanhVienPhongRepository;
import com.example.chatrealtime.repository.YeuCauThamGiaNhomRepository;

@Service
public class GroupService {

    private final PhongChatRepository phongChatRepo;
    private final ThanhVienPhongRepository tvRepo;
    private final YeuCauThamGiaNhomRepository yeuCauRepo;
    private final TaiKhoanRepository taiKhoanRepo;

    public GroupService(PhongChatRepository phongChatRepo,
                        ThanhVienPhongRepository tvRepo,
                        YeuCauThamGiaNhomRepository yeuCauRepo,
                        TaiKhoanRepository taiKhoanRepo) {
        this.phongChatRepo = phongChatRepo;
        this.tvRepo = tvRepo;
        this.yeuCauRepo = yeuCauRepo;
        this.taiKhoanRepo = taiKhoanRepo;
    }

    @Transactional
    public Map<String, Object> requestJoin(Integer maPhongChat, Integer maTaiKhoan) {
        PhongChat room = phongChatRepo.findById(maPhongChat)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chat không tồn tại"));

        if (tvRepo.existsByIdMaPhongChatAndIdMaTaiKhoanAndNgayXoaIsNull(maPhongChat, maTaiKhoan)) {
            return Map.of("status", "success", "message", "Đã là thành viên", "state", "approved");
        }

        int kieuNhom = room.getKieuNhom() != null ? room.getKieuNhom() : 0; // default public

        if (kieuNhom == 0) {
            ThanhVienPhong tv = new ThanhVienPhong(maPhongChat, maTaiKhoan, "member");
            tv.setTrangThaiThamGia("approved");
            tvRepo.save(tv);
            return Map.of("status", "success", "state", "approved");
        }

        if (yeuCauRepo.existsByMaPhongChatAndMaTaiKhoanAndTrangThaiIn(
                maPhongChat, maTaiKhoan, List.of("pending", "approved"))) {
            return Map.of("status", "success", "state", "pending");
        }

        YeuCauThamGiaNhom yc = new YeuCauThamGiaNhom(maPhongChat, maTaiKhoan);
        yeuCauRepo.save(yc);
        return Map.of("status", "success", "state", "pending");
    }

    @Transactional
    public Map<String, Object> approveRequest(Integer requestId,
                                               Integer maPhongChat,
                                               Integer nguoiXuLy,
                                               boolean accept,
                                               String lyDo) {
        // Ưu tiên tìm trong bảng yêu cầu tham gia nhóm
        YeuCauThamGiaNhom yc = requestId != null ? yeuCauRepo.findById(requestId).orElse(null) : null;
        PhongChat room;

        if (yc != null) {
            room = phongChatRepo.findById(yc.getMaPhongChat())
                    .orElseThrow(() -> new IllegalArgumentException("Phòng chat không tồn tại"));

            Integer leader = room.getMaTruongNhom() != null ? room.getMaTruongNhom() : room.getMaTaiKhoanTao();
            if (!Objects.equals(leader, nguoiXuLy)) {
                throw new IllegalArgumentException("Bạn không có quyền duyệt");
            }

            if (!"pending".equalsIgnoreCase(yc.getTrangThai())) {
                return Map.of("status", "success", "state", yc.getTrangThai());
            }

            if (accept) {
                int updated = yeuCauRepo.updateTrangThai(requestId, "approved", null, nguoiXuLy);
                if (updated > 0) {
                    if (!tvRepo.existsByIdMaPhongChatAndIdMaTaiKhoanAndNgayXoaIsNull(yc.getMaPhongChat(), yc.getMaTaiKhoan())) {
                        ThanhVienPhong tv = new ThanhVienPhong(yc.getMaPhongChat(), yc.getMaTaiKhoan(), "member");
                        tv.setTrangThaiThamGia("approved");
                        tv.setNguoiDuyet(nguoiXuLy);
                        tvRepo.save(tv);
                    } else {
                        tvRepo.approveMember(yc.getMaPhongChat(), yc.getMaTaiKhoan(), nguoiXuLy);
                    }
                }
                return Map.of("status", "success", "state", "approved");
            } else {
                yeuCauRepo.updateTrangThai(requestId, "rejected", lyDo, nguoiXuLy);
                return Map.of("status", "success", "state", "rejected");
            }
        }

        // Không có trong YEUCAU_THAMGIA_NHOM -> duyệt pending của THANHVIEN_PHONG
        if (maPhongChat == null) {
            throw new IllegalArgumentException("Thiếu mã phòng");
        }

        room = phongChatRepo.findById(maPhongChat)
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

        List<Map<String, Object>> pending = new ArrayList<>();

        // Pending do trưởng nhóm chưa duyệt khi member thêm vào phòng private
        pending.addAll(tvRepo.getPendingMembers(maPhongChat));

        // Pending gửi qua endpoint request-join (bổ sung tên, avatar)
        List<YeuCauThamGiaNhom> yeuCaus = yeuCauRepo.findByMaPhongChatAndTrangThai(maPhongChat, "pending");
        for (YeuCauThamGiaNhom yc : yeuCaus) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", yc.getId());
            item.put("maTaiKhoan", yc.getMaTaiKhoan());
            item.put("trangThai", yc.getTrangThai());

            taiKhoanRepo.findById(yc.getMaTaiKhoan()).ifPresent(tk -> {
                if (tk.getNguoiDung() != null) {
                    item.put("tenNguoiDung", tk.getNguoiDung().getTenNguoiDung());
                    item.put("anhDaiDien", tk.getNguoiDung().getAnhDaiDien());
                } else {
                    item.put("tenNguoiDung", tk.getEmail());
                }
            });
            pending.add(item);
        }

        return Map.of("status", "success", "requests", pending);
    }
}
