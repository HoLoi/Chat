package com.example.chatrealtime.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.chatrealtime.entity.BanBe;
import com.example.chatrealtime.entity.BanBeId;

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

    @Query("""
    SELECT COUNT(b) > 0 FROM BanBe b
    WHERE (b.id.maTaiKhoan1 = :a AND b.id.maTaiKhoan2 = :b)
       OR (b.id.maTaiKhoan1 = :b AND b.id.maTaiKhoan2 = :a)
""")
    boolean existsRelation(@Param("a") Integer a,
                           @Param("b") Integer b);


    @Modifying
    @Query("""
    DELETE FROM BanBe b
    WHERE (b.id.maTaiKhoan1 = :a AND b.id.maTaiKhoan2 = :b)
       OR (b.id.maTaiKhoan1 = :b AND b.id.maTaiKhoan2 = :a)
""")
    void deleteRelation(@Param("a") Integer a,
                        @Param("b") Integer b);

    @Query("""
    SELECT b.trangThai FROM BanBe b
    WHERE (b.id.maTaiKhoan1 = :a AND b.id.maTaiKhoan2 = :b)
       OR (b.id.maTaiKhoan1 = :b AND b.id.maTaiKhoan2 = :a)
""")
    List<String> findStatuses(@Param("a") Integer a,
                              @Param("b") Integer b);

        @Query("""
                SELECT COUNT(b) > 0 FROM BanBe b
                WHERE (
                                (b.id.maTaiKhoan1 = :a AND b.id.maTaiKhoan2 = :b)
                         OR (b.id.maTaiKhoan1 = :b AND b.id.maTaiKhoan2 = :a)
                )
                    AND b.trangThai = 'dongy'
        """)
        boolean isFriend(@Param("a") Integer a, @Param("b") Integer b);



    @Query("""
        SELECT COUNT(b) > 0 FROM BanBe b
        WHERE b.id.maTaiKhoan1 = :from
          AND b.id.maTaiKhoan2 = :to
          AND b.trangThai IN ('cho','dongy')
        """)
            boolean existsActiveRequest(
                    @Param("from") Integer from,
                    @Param("to") Integer to
    );


        @Query(value = """
                SELECT tk.maTaiKhoan AS maTaiKhoan,
                             nd.tenNguoiDung AS tenNguoiDung,
                             nd.anhDaiDien_URL AS anhDaiDien_URL,
                             tk.email AS email
                FROM TAIKHOAN tk
                JOIN NGUOIDUNG nd ON tk.maNguoiDung = nd.maNguoiDung
                WHERE tk.maTaiKhoan <> :uid
                    AND NOT EXISTS (
                                SELECT 1 FROM BANBE b
                                WHERE ((b.maTaiKhoan1 = :uid AND b.maTaiKhoan2 = tk.maTaiKhoan)
                                        OR (b.maTaiKhoan2 = :uid AND b.maTaiKhoan1 = tk.maTaiKhoan))
                                    AND b.trangThai = 'dongy'
                    )
                    AND tk.trangThai <> 'banned'
                ORDER BY tk.ngayTao DESC
                LIMIT 10
        """, nativeQuery = true)
        List<Map<String, Object>> suggestFriends(@Param("uid") Integer uid);


    @Query(value = """
        SELECT tk.maTaiKhoan AS id,
               nd.tenNguoiDung AS tenNguoiDung,
               nd.anhDaiDien_URL AS anhDaiDien_URL
        FROM BANBE b
        JOIN TAIKHOAN tk
             ON (CASE WHEN b.maTaiKhoan1 = :uid THEN b.maTaiKhoan2 ELSE b.maTaiKhoan1 END) = tk.maTaiKhoan
        JOIN NGUOIDUNG nd ON tk.maNguoiDung = nd.maNguoiDung
        WHERE (b.maTaiKhoan1 = :uid OR b.maTaiKhoan2 = :uid)
          AND b.trangThai = 'dongy'
          AND nd.tenNguoiDung LIKE CONCAT('%', :keyword, '%')
    """, nativeQuery = true)
    List<Map<String, Object>> searchFriends(
            @Param("uid") Integer uid,
            @Param("keyword") String keyword
    );

}
