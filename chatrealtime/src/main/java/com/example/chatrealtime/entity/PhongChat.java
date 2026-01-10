package com.example.chatrealtime.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    @Column(name = "ngayTao")
    private LocalDateTime ngayTao = LocalDateTime.now();

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

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Integer getMaTaiKhoanTao() {
        return maTaiKhoanTao;
    }

    public void setMaTaiKhoanTao(Integer maTaiKhoanTao) {
        this.maTaiKhoanTao = maTaiKhoanTao;
    }
}
