package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.TinNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TinNhanRepository extends JpaRepository<TinNhan, Integer> {

    @Query("""
        SELECT t
        FROM TinNhan t
        JOIN ThanhVienPhong tv
             ON tv.id.maPhongChat = t.maPhongChat
        WHERE t.maPhongChat = :maPhongChat
          AND tv.id.maTaiKhoan = :maTaiKhoan
          AND (
                tv.ngayXoa IS NULL
                OR t.thoiGianGui > tv.ngayXoa
          )
        ORDER BY t.thoiGianGui ASC
    """)
    List<TinNhan> getMessages(
            @Param("maPhongChat") Integer maPhongChat,
            @Param("maTaiKhoan") Integer maTaiKhoan
    );
}
