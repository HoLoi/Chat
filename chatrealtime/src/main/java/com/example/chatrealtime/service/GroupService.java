package com.example.chatrealtime.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.chatrealtime.entity.PhongChat;
import com.example.chatrealtime.entity.ThanhVienPhong;
import com.example.chatrealtime.entity.YeuCauThamGiaNhom;
import com.example.chatrealtime.repository.PhongChatRepository;
import com.example.chatrealtime.repository.ThanhVienPhongRepository;
import com.example.chatrealtime.repository.YeuCauThamGiaNhomRepository;

@Service
public class GroupService {

    private final PhongChatRepository phongChatRepo;
    private final ThanhVienPhongRepository tvRepo;
    private final YeuCauThamGiaNhomRepository yeuCauRepo;

    public GroupService(PhongChatRepository phongChatRepo,
                        ThanhVienPhongRepository tvRepo,
                        YeuCauThamGiaNhomRepository yeuCauRepo) {
        this.phongChatRepo = phongChatRepo;
        this.tvRepo = tvRepo;
        this.yeuCauRepo = yeuCauRepo;
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
    public Map<String, Object> approveRequest(Integer requestId, Integer nguoiXuLy, boolean accept, String lyDo) {
        YeuCauThamGiaNhom yc = yeuCauRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));

        PhongChat room = phongChatRepo.findById(yc.getMaPhongChat())
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

    @Transactional(readOnly = true)
    public List<YeuCauThamGiaNhom> pendingRequests(Integer maPhongChat) {
        return yeuCauRepo.findByMaPhongChatAndTrangThai(maPhongChat, "pending");
    }
}
