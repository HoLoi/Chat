package com.example.chatrealtime.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "YEUCAU_THAMGIA_NHOM")
public class YeuCauThamGiaNhom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maPhongChat")
    private Integer maPhongChat;

    @Column(name = "maTaiKhoan")
    private Integer maTaiKhoan;

    @Column(name = "trangThai")
    private String trangThai; // pending, approved, rejected

    @Column(name = "lyDoTuChoi")
    private String lyDoTuChoi;

    @Column(name = "nguoiXuLy")
    private Integer nguoiXuLy;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao;

    @Column(name = "ngayXuLy")
    private LocalDateTime ngayXuLy;

    public YeuCauThamGiaNhom() {
        this.trangThai = "pending";
        this.ngayTao = LocalDateTime.now();
    }

    public YeuCauThamGiaNhom(Integer maPhongChat, Integer maTaiKhoan) {
        this();
        this.maPhongChat = maPhongChat;
        this.maTaiKhoan = maTaiKhoan;
    }

    public Integer getId() {
        return id;
    }

    public Integer getMaPhongChat() {
        return maPhongChat;
    }

    public void setMaPhongChat(Integer maPhongChat) {
        this.maPhongChat = maPhongChat;
    }

    public Integer getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(Integer maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getLyDoTuChoi() {
        return lyDoTuChoi;
    }

    public void setLyDoTuChoi(String lyDoTuChoi) {
        this.lyDoTuChoi = lyDoTuChoi;
    }

    public Integer getNguoiXuLy() {
        return nguoiXuLy;
    }

    public void setNguoiXuLy(Integer nguoiXuLy) {
        this.nguoiXuLy = nguoiXuLy;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayXuLy() {
        return ngayXuLy;
    }

    public void setNgayXuLy(LocalDateTime ngayXuLy) {
        this.ngayXuLy = ngayXuLy;
    }
}
