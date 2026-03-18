package com.example.chatrealtime.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.chatrealtime.entity.TaiKhoan;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {
    //cai nay se thay cho cau lenh SQL: SELECT * FROM TAIKHOAN WHERE email = ?
    Optional<TaiKhoan> findByEmail(String email);

    @Query("""
    SELECT t.maTaiKhoan, t.token, t.trangThai, COALESCE(nd.tenNguoiDung, t.email)
    FROM TaiKhoan t
    LEFT JOIN t.nguoiDung nd
    WHERE t.maTaiKhoan IN :ids
""")
    List<Object[]> getUserTokenAndStatus(List<Integer> ids);

}