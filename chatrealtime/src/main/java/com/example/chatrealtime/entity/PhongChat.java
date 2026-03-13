package com.example.chatrealtime.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PHONGCHAT")
public class PhongChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maPhongChat")
    private Integer maPhongChat;

    @Column(name = "tenPhongChat")
    private String tenPhongChat;

    /**
     * 0 = chat 1-1
     * 1 = chat nhóm
     */
    @Column(name = "loaiPhong")
    private Integer loaiPhong;

    /**
     * 0 = public, 1 = private
     */
    @Column(name = "kieuNhom")
    private Integer kieuNhom;

    @Column(name = "maTruongNhom")
    private Integer maTruongNhom;

    @Column(name = "anhDaiDien_URL")
    private String anhDaiDienUrl;

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "trangThaiPhong")
    private String trangThaiPhong = "active";

    @Column(name = "maTaiKhoanTao")
    private Integer maTaiKhoanTao;

    // ===== Getter & Setter =====

    public Integer getMaPhongChat() {
        return maPhongChat;
    }

    public void setMaPhongChat(Integer maPhongChat) {
        this.maPhongChat = maPhongChat;
    }

    public String getTenPhongChat() {
        return tenPhongChat;
    }

    public void setTenPhongChat(String tenPhongChat) {
        this.tenPhongChat = tenPhongChat;
    }

    public Integer getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(Integer loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public Integer getKieuNhom() {
        return kieuNhom;
    }

    public void setKieuNhom(Integer kieuNhom) {
        this.kieuNhom = kieuNhom;
    }

    public Integer getMaTruongNhom() {
        return maTruongNhom;
    }

    public void setMaTruongNhom(Integer maTruongNhom) {
        this.maTruongNhom = maTruongNhom;
    }

    public String getAnhDaiDienUrl() {
        return anhDaiDienUrl;
    }

    public void setAnhDaiDienUrl(String anhDaiDienUrl) {
        this.anhDaiDienUrl = anhDaiDienUrl;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getTrangThaiPhong() {
        return trangThaiPhong;
    }

    public void setTrangThaiPhong(String trangThaiPhong) {
        this.trangThaiPhong = trangThaiPhong;
    }

    public Integer getMaTaiKhoanTao() {
        return maTaiKhoanTao;
    }

    public void setMaTaiKhoanTao(Integer maTaiKhoanTao) {
        this.maTaiKhoanTao = maTaiKhoanTao;
    }
}
