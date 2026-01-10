package com.example.chatrealtime.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "THANHVIEN_PHONG")
public class ThanhVienPhong {

    @EmbeddedId
    private ThanhVienPhongId id;

    @Column(name = "vaiTro")
    private String vaiTro;

    @Column(name = "ngayThamGia")
    private LocalDateTime ngayThamGia;

    @Column(name = "ngayXoa")
    private LocalDateTime ngayXoa;

    public ThanhVienPhong() {}

    public ThanhVienPhong(Integer maPhongChat, Integer maTaiKhoan, String vaiTro) {
        this.id = new ThanhVienPhongId(maPhongChat, maTaiKhoan);
        this.vaiTro = vaiTro;
        this.ngayThamGia = LocalDateTime.now();
    }

    public ThanhVienPhongId getId() {
        return id;
    }

    public Integer getMaPhongChat() {
        return id.getMaPhongChat();
    }

    public Integer getMaTaiKhoan() {
        return id.getMaTaiKhoan();
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public LocalDateTime getNgayXoa() {
        return ngayXoa;
    }
}
