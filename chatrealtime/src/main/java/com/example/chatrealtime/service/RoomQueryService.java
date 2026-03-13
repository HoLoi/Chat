package com.example.chatrealtime.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class RoomQueryService {
    @PersistenceContext
    private EntityManager em;

    public List<Map<String, Object>> getRooms(Integer maTaiKhoan, boolean hideDeletedGroupsWithoutNewMessage) {
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
            p.anhDaiDien_URL as avatarGroup,

                        (SELECT t.noiDung FROM TINNHAN t 
                         JOIN THANHVIEN_PHONG tvu ON tvu.maPhongChat = t.maPhongChat AND tvu.maTaiKhoan = :uid
                         WHERE t.maPhongChat = p.maPhongChat 
                             AND (tvu.ngayXoa IS NULL OR t.thoiGianGui > tvu.ngayXoa)
                         ORDER BY t.thoiGianGui DESC LIMIT 1) AS lastMessage,

                        (SELECT t.thoiGianGui FROM TINNHAN t 
                         JOIN THANHVIEN_PHONG tvu ON tvu.maPhongChat = t.maPhongChat AND tvu.maTaiKhoan = :uid
                         WHERE t.maPhongChat = p.maPhongChat 
                             AND (tvu.ngayXoa IS NULL OR t.thoiGianGui > tvu.ngayXoa)
                         ORDER BY t.thoiGianGui DESC LIMIT 1) AS lastTime,

                        (SELECT COUNT(*)
                         FROM TRANGTHAI_TINNHAN tt
                         JOIN TINNHAN t2 ON tt.maTinNhan = t2.maTinNhan
                         WHERE t2.maPhongChat = p.maPhongChat
                             AND tt.maTaiKhoan = :uid
                             AND tt.trangThai <> 'read'
                             AND t2.thoiGianGui > IFNULL(
                                        (SELECT ngayXoa FROM THANHVIEN_PHONG
                                         WHERE maPhongChat = p.maPhongChat
                                             AND maTaiKhoan = :uid),
                                        '1970-01-01'
                             )
                        ) AS unreadCount

        FROM PHONGCHAT p
        JOIN THANHVIEN_PHONG tp ON p.maPhongChat = tp.maPhongChat
            WHERE tp.maTaiKhoan = :uid
              AND IFNULL(p.trangThaiPhong, 'active') <> 'deleted'

        HAVING (
            (
                lastTime > IFNULL(
                    (SELECT ngayXoa FROM THANHVIEN_PHONG
                     WHERE maPhongChat = p.maPhongChat
                       AND maTaiKhoan = :uid),
                    '1970-01-01'
                )
            )
            OR (lastTime IS NULL)
        )
        AND NOT (p.loaiPhong = 0 AND lastTime IS NULL)
    """;

        if (hideDeletedGroupsWithoutNewMessage) {
            sql += """
                AND NOT (
                    p.loaiPhong = 1
                    AND lastTime IS NULL
                    AND (
                        SELECT ngayXoa FROM THANHVIEN_PHONG
                        WHERE maPhongChat = p.maPhongChat
                          AND maTaiKhoan = :uid
                    ) IS NOT NULL
                )
            """;
        }

        sql += " ORDER BY COALESCE(lastTime, p.ngayTao) DESC";

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("uid", maTaiKhoan)
                .getResultList();

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] r : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r[0]);
            map.put("loaiPhong", r[1]);
            map.put("tenPhongChat", r[2] != null ? r[2] : "Không xác định");
            map.put("anhDaiDien_URL", r[3] != null ? r[3] : r[4]);
            map.put("lastMessage", r[5] != null ? r[5] : "(Chưa có tin nhắn)");
            map.put("unreadCount", ((Number) r[7]).intValue());
            result.add(map);
        }

        return result;
    }

    public List<Map<String, Object>> searchRooms(Integer maTaiKhoan, String keyword) {
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
                    ELSE p.anhDaiDien_URL
                END AS anhDaiDien_URL
            FROM PHONGCHAT p
                        JOIN THANHVIEN_PHONG tp ON p.maPhongChat = tp.maPhongChat
                        WHERE tp.maTaiKhoan = :uid
                            AND IFNULL(p.trangThaiPhong, 'active') <> 'deleted'
              AND (
                    CASE WHEN p.loaiPhong = 0 THEN (
                        SELECT nd.tenNguoiDung
                        FROM THANHVIEN_PHONG tp2
                        JOIN TAIKHOAN tk2 ON tp2.maTaiKhoan = tk2.maTaiKhoan
                        JOIN NGUOIDUNG nd ON tk2.maNguoiDung = nd.maNguoiDung
                        WHERE tp2.maPhongChat = p.maPhongChat
                          AND tk2.maTaiKhoan <> :uid
                        LIMIT 1
                    ) ELSE p.tenPhongChat END
                  ) LIKE CONCAT('%', :keyword, '%')
        """;

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("uid", maTaiKhoan)
                .setParameter("keyword", keyword)
                .getResultList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r[0]);
            map.put("loaiPhong", r[1]);
            map.put("tenPhongChat", r[2]);
            map.put("anhDaiDien_URL", r[3]);
            result.add(map);
        }

        return result;
    }

}
