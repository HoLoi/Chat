package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.BanBe;
import com.example.chatrealtime.entity.BanBeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BanBeRepository extends JpaRepository<BanBe, BanBeId> {
    @Query(value = """
        SELECT 
            tk2.maTaiKhoan AS maTaiKhoan,
            tk2.email AS email,
            tk2.trangThai AS trangThai,
            nd.tenNguoiDung AS tenNguoiDung,
            nd.anhDaiDien_URL AS anhDaiDien_URL
        FROM BANBE b
        JOIN TAIKHOAN tk2 ON b.maTaiKhoan2 = tk2.maTaiKhoan
        JOIN NGUOIDUNG nd ON tk2.maNguoiDung = nd.maNguoiDung
        WHERE b.maTaiKhoan1 = :maTaiKhoan
          AND b.trangThai = 'dongy'
        """, nativeQuery = true)
    List<Map<String, Object>> getFriends(@Param("maTaiKhoan") Integer maTaiKhoan);

    @Query("""
        SELECT b.trangThai FROM BanBe b
        WHERE (b.id.maTaiKhoan1 = :a AND b.id.maTaiKhoan2 = :b)
           OR (b.id.maTaiKhoan1 = :b AND b.id.maTaiKhoan2 = :a)
    """)
    Optional<String> findStatus(@Param("a") Integer a,
                                @Param("b") Integer b);


    @Query(value = """
        SELECT 
            u.maTaiKhoan AS maNguoiGui,
            nd.tenNguoiDung AS tenNguoiGui,
            u.email AS emailNguoiGui,
            COALESCE(nd.anhDaiDien_URL, 'default_avatar.png') AS anhDaiDien_URL
        FROM BANBE b
        JOIN TAIKHOAN u ON b.maTaiKhoan1 = u.maTaiKhoan
        JOIN NGUOIDUNG nd ON u.maNguoiDung = nd.maNguoiDung
        WHERE b.maTaiKhoan2 = :maTaiKhoan
          AND b.trangThai = 'cho'
        """, nativeQuery = true)
    List<Map<String, Object>> getFriendRequests(
            @Param("maTaiKhoan") Integer maTaiKhoan
    );


    @Modifying
    @Query("""
        UPDATE BanBe b
        SET b.trangThai = 'dongy'
        WHERE b.id.maTaiKhoan1 = :from
          AND b.id.maTaiKhoan2 = :to
          AND b.trangThai = 'cho'
    """)
    int acceptRequest(@Param("from") Integer from,
                      @Param("to") Integer to);

    @Modifying
    @Query(value = """
        INSERT INTO BANBE (maTaiKhoan1, maTaiKhoan2, trangThai)
        VALUES (:to, :from, 'dongy')
        ON DUPLICATE KEY UPDATE trangThai='dongy'
    """, nativeQuery = true)
    void insertReverseFriend(@Param("from") Integer from,
                             @Param("to") Integer to);

    @Modifying
    @Query("""
        UPDATE BanBe b
        SET b.trangThai = 'tuchoi'
        WHERE b.id.maTaiKhoan1 = :from
          AND b.id.maTaiKhoan2 = :to
          AND b.trangThai = 'cho'
    """)
    int rejectRequest(@Param("from") Integer from,
                      @Param("to") Integer to);
}
