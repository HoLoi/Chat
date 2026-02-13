package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// Cái này thay cho SELECT * FROM TAIKHOAN WHERE email = ?
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByEmail(String email);

    @Query("""
    SELECT t.maTaiKhoan, t.token, t.trangThai
    FROM TaiKhoan t
    WHERE t.maTaiKhoan IN :ids
""")
    List<Object[]> getUserTokenAndStatus(List<Integer> ids);

}