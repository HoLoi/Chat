package com.example.chatrealtime.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoomQueryService {
    @PersistenceContext
    private EntityManager em;

    public List<Map<String, Object>> getRooms(Integer maTaiKhoan) {
        String sql = """
        SELECT 
            p.maPhongChat AS id,
            p.loaiPhong,

            CASE 
                WHEN p.loaiPhong = 0 THEN (
                    SELECT nd.tenNguoiDung
                    FROM THANHVIEN_PHONG tp2
                    JOIN TAIKHOAN tk2 ON tp2.maTaiKhoan = tk2.maTaiKhoan
                    JOIN NGUOIDUNG nd ON tk2.maNguoiDung = nd.maNguoiDung
                    WHERE tp2.maPhongChat = p.maPhongChat
                      AND tk2.maTaiKhoan <> :uid
                    LIMIT 1
                )
                ELSE p.tenPhongChat
            END AS tenPhongChat,

            CASE 
                WHEN p.loaiPhong = 0 THEN (
                    SELECT nd.anhDaiDien_URL
                    FROM THANHVIEN_PHONG tp2
                    JOIN TAIKHOAN tk2 ON tp2.maTaiKhoan = tk2.maTaiKhoan
                    JOIN NGUOIDUNG nd ON tk2.maNguoiDung = nd.maNguoiDung
                    WHERE tp2.maPhongChat = p.maPhongChat
                      AND tk2.maTaiKhoan <> :uid
                    LIMIT 1
                )
                ELSE NULL
            END AS anhDaiDien_URL,

            (SELECT t.noiDung FROM TINNHAN t 
             WHERE t.maPhongChat = p.maPhongChat 
             ORDER BY t.thoiGianGui DESC LIMIT 1) AS lastMessage,

            (SELECT t.thoiGianGui FROM TINNHAN t 
             WHERE t.maPhongChat = p.maPhongChat 
             ORDER BY t.thoiGianGui DESC LIMIT 1) AS lastTime,

            (SELECT COUNT(*)
             FROM TRANGTHAI_TINNHAN tt
             JOIN TINNHAN t2 ON tt.maTinNhan = t2.maTinNhan
             WHERE t2.maPhongChat = p.maPhongChat
               AND tt.maTaiKhoan = :uid
               AND tt.trangThai <> 'read') AS unreadCount

        FROM PHONGCHAT p
        JOIN THANHVIEN_PHONG tp ON p.maPhongChat = tp.maPhongChat
        WHERE tp.maTaiKhoan = :uid

        HAVING (
            lastTime > IFNULL(
                (SELECT ngayXoa FROM THANHVIEN_PHONG
                 WHERE maPhongChat = p.maPhongChat
                   AND maTaiKhoan = :uid),
                '1970-01-01'
            )
        ) OR lastTime IS NULL

        ORDER BY p.ngayTao DESC
    """;

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("uid", maTaiKhoan)
                .getResultList();

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] r : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r[0]);
            map.put("loaiPhong", r[1]);
            map.put("tenPhongChat", r[2] != null ? r[2] : "Không xác định");
            map.put("anhDaiDien_URL", r[3] != null ? r[3] : null);
            map.put("lastMessage", r[4] != null ? r[4] : "(Chưa có tin nhắn)");
            map.put("unreadCount", ((Number) r[6]).intValue());
            result.add(map);
        }

        return result;
    }

}
