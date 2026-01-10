package com.example.chatrealtime.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TINNHAN")
public class TinNhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maTinNhan")
    private Integer maTinNhan;

    @Column(name = "noiDung")
    private String noiDung;

    @Column(name = "duongDanFile")
    private String duongDanFile;

    @Column(name = "thoiGianGui")
    private LocalDateTime thoiGianGui;

    @Column(name = "maTaiKhoanGui")
    private Integer maTaiKhoanGui;

    @Column(name = "maPhongChat")
    private Integer maPhongChat;

    @Column(name = "loaiTinNhan")
    private String loaiTinNhan;

    // ===== Getter / Setter =====

    public Integer getMaTinNhan() {
        return maTinNhan;
    }

    public void setMaTinNhan(Integer maTinNhan) {
        this.maTinNhan = maTinNhan;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getDuongDanFile() {
        return duongDanFile;
    }

    public void setDuongDanFile(String duongDanFile) {
        this.duongDanFile = duongDanFile;
    }

    public LocalDateTime getThoiGianGui() {
        return thoiGianGui;
    }

    public void setThoiGianGui(LocalDateTime thoiGianGui) {
        this.thoiGianGui = thoiGianGui;
    }

    public Integer getMaTaiKhoanGui() {
        return maTaiKhoanGui;
    }

    public void setMaTaiKhoanGui(Integer maTaiKhoanGui) {
        this.maTaiKhoanGui = maTaiKhoanGui;
    }

    public Integer getMaPhongChat() {
        return maPhongChat;
    }

    public void setMaPhongChat(Integer maPhongChat) {
        this.maPhongChat = maPhongChat;
    }

    public String getLoaiTinNhan() {
        return loaiTinNhan;
    }

    public void setLoaiTinNhan(String loaiTinNhan) {
        this.loaiTinNhan = loaiTinNhan;
    }
}
