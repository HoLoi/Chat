package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {

    @Query("""
        SELECT new map(
            nd.tenNguoiDung as tenNguoiDung,
            nd.anhDaiDien as anhDaiDien_URL,
            nd.gioiTinh as gioiTinh,
            nd.ngaySinh as ngaySinh,
            nd.soDienThoai as soDienThoai
        )
        FROM TaiKhoan tk
        JOIN tk.nguoiDung nd
        WHERE tk.email = :email
    """)
    Optional<Map<String, Object>> getUserInfoByEmail(
            @Param("email") String email
    );


    @Query("""
        SELECT new map(
            nd.tenNguoiDung as tenNguoiDung,
            nd.anhDaiDien as anhDaiDien_URL,
            nd.gioiTinh as gioiTinh,
            nd.ngaySinh as ngaySinh,
            nd.soDienThoai as soDienThoai
        )
        FROM TaiKhoan tk
        JOIN tk.nguoiDung nd
        WHERE tk.maTaiKhoan = :maTaiKhoan
    """)
    Optional<Map<String, Object>> getUserByTaiKhoan(
            @Param("maTaiKhoan") Integer maTaiKhoan
    );

}
