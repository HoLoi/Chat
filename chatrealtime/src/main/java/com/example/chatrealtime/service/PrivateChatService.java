package com.example.chatrealtime.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.chatrealtime.entity.PhongChat;
import com.example.chatrealtime.entity.ThanhVienPhong;
import com.example.chatrealtime.repository.BanBeRepository;
import com.example.chatrealtime.repository.PhongChatRepository;
import com.example.chatrealtime.repository.TaiKhoanRepository;
import com.example.chatrealtime.repository.ThanhVienPhongRepository;

import jakarta.transaction.Transactional;

@Service
public class PrivateChatService {

    private static final Logger log = LoggerFactory.getLogger(PrivateChatService.class);

    private final BanBeRepository banBeRepository;
    private final PhongChatRepository phongChatRepository;
    private final ThanhVienPhongRepository thanhVienPhongRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public PrivateChatService(BanBeRepository banBeRepository,
                              PhongChatRepository phongChatRepository,
                              ThanhVienPhongRepository thanhVienPhongRepository,
                              TaiKhoanRepository taiKhoanRepository) {
        this.banBeRepository = banBeRepository;
        this.phongChatRepository = phongChatRepository;
        this.thanhVienPhongRepository = thanhVienPhongRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Transactional
    public Integer openOrCreate(Integer myId, Integer friendId) {
        if (myId == null || friendId == null || myId <= 0 || friendId <= 0) {
            throw new IllegalArgumentException("Thiếu mã tài khoản hợp lệ");
        }
        if (myId.equals(friendId)) {
            throw new IllegalArgumentException("Không thể mở chat với chính mình");
        }
        if (!taiKhoanRepository.existsById(friendId) || !taiKhoanRepository.existsById(myId)) {
            throw new IllegalArgumentException("Tài khoản không tồn tại");
        }
        if (!banBeRepository.isFriend(myId, friendId)) {
            throw new IllegalStateException("Chỉ mở chat với bạn bè đã đồng ý");
        }

        Integer existing = phongChatRepository.findOneToOne(myId, friendId);
        if (existing != null) {
            return existing;
        }

        PhongChat room = new PhongChat();
        room.setTenPhongChat("Đoạn chat riêng");
        room.setLoaiPhong(0); // 0 = 1-1
        room.setKieuNhom(1);  // private
        room.setMaTaiKhoanTao(myId);
        room.setNgayTao(LocalDateTime.now());
        PhongChat saved = phongChatRepository.save(room);

        addMember(saved.getMaPhongChat(), myId);
        addMember(saved.getMaPhongChat(), friendId);

        log.info("Created private room {} for users {} and {}", saved.getMaPhongChat(), myId, friendId);
        return saved.getMaPhongChat();
    }

    private void addMember(Integer roomId, Integer userId) {
        ThanhVienPhong member = new ThanhVienPhong(roomId, userId, "member");
        // đảm bảo trạng thái đã duyệt
        member.setTrangThaiThamGia("approved");
        member.setNgayThamGia(LocalDateTime.now());
        thanhVienPhongRepository.save(member);
    }
}
