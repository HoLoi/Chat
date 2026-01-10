package com.example.chatrealtime.entity;

import jakarta.persistence.*;

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

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }
}
