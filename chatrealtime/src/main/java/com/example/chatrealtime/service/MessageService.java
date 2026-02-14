package com.example.chatrealtime.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chatrealtime.entity.TaiKhoan;
import com.example.chatrealtime.entity.TinNhan;
import com.example.chatrealtime.entity.TrangThaiTinNhan;
import com.example.chatrealtime.entity.TrangThaiTinNhanId;
import com.example.chatrealtime.repository.PhongChatRepository;
import com.example.chatrealtime.repository.TaiKhoanRepository;
import com.example.chatrealtime.repository.ThanhVienPhongRepository;
import com.example.chatrealtime.repository.TinNhanRepository;
import com.example.chatrealtime.repository.TrangThaiTinNhanRepository;

import jakarta.transaction.Transactional;

@Service
public class MessageService {

    @Autowired
    private TinNhanRepository tinNhanRepo;

    @Autowired
    private ThanhVienPhongRepository tvPhongRepo;

    @Autowired
    private TrangThaiTinNhanRepository trangThaiRepo;

    @Autowired
    private TaiKhoanRepository taiKhoanRepo;

        @Autowired
        private PhongChatRepository phongChatRepo;

    @Autowired
    private FcmService fcmService;


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

        // Gửi push FCM cho các thành viên khác
        notifyMembers(members, maTaiKhoanGui, maPhongChat, noiDung, loaiTinNhan, duongDanFile);

        return saved;
    }

    public List<TinNhan> getMessages(
            Integer maPhongChat,
            Integer maTaiKhoan
    ) {
        return tinNhanRepo.getMessages(maPhongChat, maTaiKhoan);
    }

    public List<Map<String, Object>> getMessagesWithSender(
            Integer maPhongChat,
            Integer maTaiKhoan
    ) {
        List<TinNhan> raw = tinNhanRepo.getMessages(maPhongChat, maTaiKhoan);

        Set<Integer> senderIds = raw.stream()
                .map(TinNhan::getMaTaiKhoanGui)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, TaiKhoan> accountMap = taiKhoanRepo.findAllById(senderIds).stream()
                .collect(Collectors.toMap(
                        TaiKhoan::getMaTaiKhoan,
                        tk -> tk
                ));

        return raw.stream()
                .map(t -> {
                    TaiKhoan tk = accountMap.get(t.getMaTaiKhoanGui());
                    String tenNguoiGui = tk != null && tk.getNguoiDung() != null && tk.getNguoiDung().getTenNguoiDung() != null
                            ? tk.getNguoiDung().getTenNguoiDung()
                            : tk != null ? tk.getEmail() : "";
                    String avatar = tk != null && tk.getNguoiDung() != null
                            ? tk.getNguoiDung().getAnhDaiDien()
                            : "";

                    Map<String, Object> m = new HashMap<>();
                    m.put("maTinNhan", t.getMaTinNhan());
                    m.put("maTaiKhoanGui", t.getMaTaiKhoanGui());
                    m.put("maPhongChat", t.getMaPhongChat());
                    m.put("noiDung", t.getNoiDung());
                    m.put("loaiTinNhan", t.getLoaiTinNhan());
                    m.put("duongDanFile", t.getDuongDanFile());
                    m.put("tenNguoiGui", tenNguoiGui);
                    m.put("anhDaiDienNguoiGui", avatar != null ? avatar : "");
                    m.put("thoiGianGui", t.getThoiGianGui());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private void notifyMembers(List<Integer> members,
                               Integer senderId,
                               Integer roomId,
                               String content,
                               String type,
                               String duongDanFile) {

        var senderInfo = taiKhoanRepo.findById(senderId)
                .map(tk -> new Object() {
                    final String ten = tk.getNguoiDung() != null && tk.getNguoiDung().getTenNguoiDung() != null
                            ? tk.getNguoiDung().getTenNguoiDung()
                            : tk.getEmail();
                    final String avatar = tk.getNguoiDung() != null ? tk.getNguoiDung().getAnhDaiDien() : null;
                })
                .orElse(null);

        String senderName = senderInfo != null ? senderInfo.ten : "Tin nhan moi";
        String senderAvatar = senderInfo != null ? senderInfo.avatar : null;

        String roomName = phongChatRepo.findById(roomId)
                .map(pc -> pc.getTenPhongChat())
                .orElse("Doan chat");

        List<Integer> receivers = members.stream()
                .filter(id -> !Objects.equals(id, senderId))
                .toList();

        if (receivers.isEmpty()) return;

        List<Object[]> tokenData = taiKhoanRepo.getUserTokenAndStatus(receivers);

        List<String> tokens = tokenData.stream()
                .map(arr -> (String) arr[1])
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (tokens.isEmpty()) return;

                String resolvedType = type != null ? type : "text";
                String resolvedContent = content != null ? content : "";

                // Nếu là image/video thì body để gợi ý loại file
                String body;
                if (resolvedType.startsWith("image")) {
                        body = "Đã gửi hình ảnh";
                } else if (resolvedType.startsWith("video")) {
                        body = "Đã gửi video";
                } else {
                        body = resolvedContent;
                }

                String filePath = duongDanFile != null ? duongDanFile : "";

                Map<String, String> data = Map.of(
                        "type", "chat_message",
                        "maPhongChat", String.valueOf(roomId),
                        "maTaiKhoanGui", String.valueOf(senderId),
                        "tenNguoiGui", senderName,
                        "anhDaiDienNguoiGui", senderAvatar != null ? senderAvatar : "",
                        "roomName", roomName,
                        "noiDung", resolvedContent,
                        "loaiTinNhan", resolvedType,
                        "duongDanFile", filePath
                );

                // Tiêu đề dùng tên người gửi để notification hệ thống (nếu Android tự hiển thị) có đúng tên
                String title = senderName;

        fcmService.sendMulticast(tokens, title, body, data);
    }


}

