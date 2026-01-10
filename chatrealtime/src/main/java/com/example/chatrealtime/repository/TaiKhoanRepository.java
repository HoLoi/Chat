package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Cái này thay cho SELECT * FROM TAIKHOAN WHERE email = ?
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByEmail(String email);
}