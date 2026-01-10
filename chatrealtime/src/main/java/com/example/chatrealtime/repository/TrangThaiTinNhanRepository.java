package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.TrangThaiTinNhan;
import com.example.chatrealtime.entity.TrangThaiTinNhanId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrangThaiTinNhanRepository
        extends JpaRepository<TrangThaiTinNhan, TrangThaiTinNhanId> {

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
