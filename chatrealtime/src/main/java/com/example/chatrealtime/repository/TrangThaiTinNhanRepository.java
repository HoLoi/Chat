package com.example.chatrealtime.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.chatrealtime.entity.TrangThaiTinNhan;
import com.example.chatrealtime.entity.TrangThaiTinNhanId;

import jakarta.transaction.Transactional;

@Repository
public interface TrangThaiTinNhanRepository
        extends JpaRepository<TrangThaiTinNhan, TrangThaiTinNhanId> {

    @Query("""
        SELECT tt.id.maTinNhan,
               CASE
                   WHEN SUM(CASE WHEN tt.id.maTaiKhoan <> t.maTaiKhoanGui AND tt.trangThai = 'read' THEN 1 ELSE 0 END) > 0
                       THEN 'read'
                   ELSE 'sent'
               END
        FROM TrangThaiTinNhan tt
        JOIN TinNhan t ON t.maTinNhan = tt.id.maTinNhan
        WHERE t.maPhongChat = :pid
        GROUP BY tt.id.maTinNhan
    """)
    List<Object[]> getAggregateStatusByRoom(@Param("pid") Integer maPhongChat);

    @Modifying
    @Transactional
    @Query("""
        UPDATE TrangThaiTinNhan tt
        SET tt.trangThai = 'read',
            tt.thoiGianCapNhat = CURRENT_TIMESTAMP
        WHERE tt.id.maTaiKhoan = :uid
          AND tt.id.maTinNhan IN (
              SELECT t.maTinNhan
              FROM TinNhan t
              WHERE t.maPhongChat = :pid
          )
          AND tt.trangThai <> 'read'
    """)
    int markRead(
            @Param("uid") Integer maTaiKhoan,
            @Param("pid") Integer maPhongChat
    );
}
