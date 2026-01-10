package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.ThanhVienPhong;
import com.example.chatrealtime.entity.ThanhVienPhongId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ThanhVienPhongRepository
        extends JpaRepository<ThanhVienPhong, ThanhVienPhongId> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE ThanhVienPhong tv
        SET tv.ngayXoa = CURRENT_TIMESTAMP
        WHERE tv.id.maPhongChat = :maPhongChat
          AND tv.id.maTaiKhoan = :maTaiKhoan
    """)
    int updateNgayXoa(
            @Param("maPhongChat") Integer maPhongChat,
            @Param("maTaiKhoan") Integer maTaiKhoan
    );

    @Query("""
        SELECT new map(
            tk.maTaiKhoan as maTaiKhoan,
            nd.tenNguoiDung as tenNguoiDung,
            nd.anhDaiDien as anhDaiDien
        )
        FROM ThanhVienPhong tv
        JOIN TaiKhoan tk ON tv.id.maTaiKhoan = tk.maTaiKhoan
        JOIN tk.nguoiDung nd
        WHERE tv.id.maPhongChat = :maPhongChat
          AND tv.ngayXoa IS NULL
    """)
    List<Map<String, Object>> getMembers(
            @Param("maPhongChat") Integer maPhongChat
    );

    @Query("""
        SELECT tv.id.maTaiKhoan
        FROM ThanhVienPhong tv
        WHERE tv.id.maPhongChat = :maPhongChat
          AND tv.ngayXoa IS NULL
    """)
    List<Integer> getMemberIds(
            @Param("maPhongChat") Integer maPhongChat
    );
}
