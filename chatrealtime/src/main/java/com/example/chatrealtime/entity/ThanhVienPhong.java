package com.example.chatrealtime.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "THANHVIEN_PHONG")
public class ThanhVienPhong {

    @EmbeddedId
    private ThanhVienPhongId id;

    @Column(name = "vaiTro")
    private String vaiTro;

    @Column(name = "trangThaiThamGia")
    private String trangThaiThamGia;

    @Column(name = "nguoiDuyet")
    private Integer nguoiDuyet;

    @Column(name = "ngayThamGia")
    private LocalDateTime ngayThamGia;

    @Column(name = "ngayXoa")
    private LocalDateTime ngayXoa;

    public ThanhVienPhong() {}

    public ThanhVienPhong(Integer maPhongChat, Integer maTaiKhoan, String vaiTro) {
        this.id = new ThanhVienPhongId(maPhongChat, maTaiKhoan);
        this.vaiTro = vaiTro;
        this.trangThaiThamGia = "approved";
        this.ngayThamGia = LocalDateTime.now();
    }

    public ThanhVienPhongId getId() {
        return id;
    }

    public void setId(ThanhVienPhongId id) {
        this.id = id;
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

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getTrangThaiThamGia() {
        return trangThaiThamGia;
    }

    public void setTrangThaiThamGia(String trangThaiThamGia) {
        this.trangThaiThamGia = trangThaiThamGia;
    }

    public Integer getNguoiDuyet() {
        return nguoiDuyet;
    }

    public void setNguoiDuyet(Integer nguoiDuyet) {
        this.nguoiDuyet = nguoiDuyet;
    }

    public LocalDateTime getNgayXoa() {
        return ngayXoa;
    }

    public void setNgayXoa(LocalDateTime ngayXoa) {
        this.ngayXoa = ngayXoa;
    }

    public void setNgayThamGia(LocalDateTime ngayThamGia) {
        this.ngayThamGia = ngayThamGia;
    }
}
