package com.example.chatrealtime.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "MODERATION_LOG")
public class ModerationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maTinNhan")
    private Integer maTinNhan;

    @Column(name = "maPhongChat")
    private Integer maPhongChat;

    @Column(name = "maTaiKhoanGui")
    private Integer maTaiKhoanGui;

    @Column(name = "nhanViPham")
    private String nhanViPham;

    @Column(name = "mucDoViPham")
    private String mucDoViPham;

    @Column(name = "diemScore")
    private Double diemScore;

    @Column(name = "hanhDong")
    private String hanhDong;

    @Column(name = "noiDungGoc", columnDefinition = "TEXT")
    private String noiDungGoc;

    @Column(name = "thoiGian")
    private LocalDateTime thoiGian = LocalDateTime.now();

    public Integer getId() {
        return id;
    }

    public Integer getMaTinNhan() {
        return maTinNhan;
    }

    public void setMaTinNhan(Integer maTinNhan) {
        this.maTinNhan = maTinNhan;
    }

    public Integer getMaPhongChat() {
        return maPhongChat;
    }

    public void setMaPhongChat(Integer maPhongChat) {
        this.maPhongChat = maPhongChat;
    }

    public Integer getMaTaiKhoanGui() {
        return maTaiKhoanGui;
    }

    public void setMaTaiKhoanGui(Integer maTaiKhoanGui) {
        this.maTaiKhoanGui = maTaiKhoanGui;
    }

    public String getNhanViPham() {
        return nhanViPham;
    }

    public void setNhanViPham(String nhanViPham) {
        this.nhanViPham = nhanViPham;
    }

    public String getMucDoViPham() {
        return mucDoViPham;
    }

    public void setMucDoViPham(String mucDoViPham) {
        this.mucDoViPham = mucDoViPham;
    }

    public Double getDiemScore() {
        return diemScore;
    }

    public void setDiemScore(Double diemScore) {
        this.diemScore = diemScore;
    }

    public String getHanhDong() {
        return hanhDong;
    }

    public void setHanhDong(String hanhDong) {
        this.hanhDong = hanhDong;
    }

    public String getNoiDungGoc() {
        return noiDungGoc;
    }

    public void setNoiDungGoc(String noiDungGoc) {
        this.noiDungGoc = noiDungGoc;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }
}
