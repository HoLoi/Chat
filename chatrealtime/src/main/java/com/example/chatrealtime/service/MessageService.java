package com.example.chatrealtime.service;

import com.example.chatrealtime.entity.TinNhan;
import com.example.chatrealtime.entity.TrangThaiTinNhan;
import com.example.chatrealtime.entity.TrangThaiTinNhanId;
import com.example.chatrealtime.repository.ThanhVienPhongRepository;
import com.example.chatrealtime.repository.TinNhanRepository;
import com.example.chatrealtime.repository.TrangThaiTinNhanRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private TinNhanRepository tinNhanRepo;

    @Autowired
    private ThanhVienPhongRepository tvPhongRepo;

    @Autowired
    private TrangThaiTinNhanRepository trangThaiRepo;

    @Transactional
    public TinNhan sendMessage(
            Integer maPhongChat,
            Integer maTaiKhoanGui,
            String noiDung,
            String loaiTinNhan,
            String duongDanFile
    ) {
        // 1. Lưu tin nhắn
        TinNhan t = new TinNhan();
        t.setMaPhongChat(maPhongChat);
        t.setMaTaiKhoanGui(maTaiKhoanGui);
        t.setNoiDung(noiDung);
        t.setLoaiTinNhan(loaiTinNhan);
        t.setDuongDanFile(duongDanFile);
        t.setThoiGianGui(LocalDateTime.now());

        TinNhan saved = tinNhanRepo.save(t);

        // 2. Ghi trạng thái tin nhắn cho tất cả thành viên
        List<Integer> members =
                tvPhongRepo.getMemberIds(maPhongChat);

        for (Integer uid : members) {
            TrangThaiTinNhan tt = new TrangThaiTinNhan();
            tt.setId(new TrangThaiTinNhanId(
                    saved.getMaTinNhan(), uid));
            tt.setTrangThai(
                    uid.equals(maTaiKhoanGui) ? "read" : "sent"
            );
            tt.setThoiGianCapNhat(LocalDateTime.now());
            trangThaiRepo.save(tt);
        }

        return saved;
    }

    public List<TinNhan> getMessages(
            Integer maPhongChat,
            Integer maTaiKhoan
    ) {
        return tinNhanRepo.getMessages(maPhongChat, maTaiKhoan);
    }


}

