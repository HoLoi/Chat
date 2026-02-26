package com.example.chatrealtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TAIKHOAN")
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maTaiKhoan")
    private Integer maTaiKhoan;

    @Column(name = "email")
    private String email;

    @Column(name = "matKhau")
    private String matKhau;

    @Column(name = "token")
    private String token;

    @Column(name = "trangThai")
    private String trangThai;

        @Column(name = "diemCanhCao")
        private Integer diemCanhCao;

    @Column(name = "soLanWarningHomNay")
    private Integer soLanWarningHomNay;

    @Column(name = "ngayTinhWarning")
    private java.time.LocalDate ngayTinhWarning;

    @Column(name = "thoiGianKhoa")
    private java.time.LocalDateTime thoiGianKhoa;

    @ManyToOne
    @JoinColumn(name = "maNguoiDung")
    private NguoiDung nguoiDung;

    public Integer getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(Integer maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getDiemCanhCao() {
        return diemCanhCao;
    }

    public void setDiemCanhCao(Integer diemCanhCao) {
        this.diemCanhCao = diemCanhCao;
    }

    public Integer getSoLanWarningHomNay() {
        return soLanWarningHomNay;
    }

    public void setSoLanWarningHomNay(Integer soLanWarningHomNay) {
        this.soLanWarningHomNay = soLanWarningHomNay;
    }

    public java.time.LocalDate getNgayTinhWarning() {
        return ngayTinhWarning;
    }

    public void setNgayTinhWarning(java.time.LocalDate ngayTinhWarning) {
        this.ngayTinhWarning = ngayTinhWarning;
    }

    public java.time.LocalDateTime getThoiGianKhoa() {
        return thoiGianKhoa;
    }

    public void setThoiGianKhoa(java.time.LocalDateTime thoiGianKhoa) {
        this.thoiGianKhoa = thoiGianKhoa;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }
}
