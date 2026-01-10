package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.PhongChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhongChatRepository extends JpaRepository<PhongChat, Integer> {

    @Query("""
        SELECT pc.maPhongChat
        FROM PhongChat pc
        JOIN ThanhVienPhong tv1 ON pc.maPhongChat = tv1.id.maPhongChat
        JOIN ThanhVienPhong tv2 ON pc.maPhongChat = tv2.id.maPhongChat
        WHERE pc.loaiPhong = 0
          AND tv1.id.maTaiKhoan = :u1
          AND tv2.id.maTaiKhoan = :u2
          AND tv1.ngayXoa IS NULL
          AND tv2.ngayXoa IS NULL
    """)
    Integer findOneToOne(
            @Param("u1") Integer user1,
            @Param("u2") Integer user2
    );
}
