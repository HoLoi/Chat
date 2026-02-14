package com.example.chatrealtime.model;

public class Message {
    private String noiDung;
    private boolean isMine;
    private int maNguoiGui;
    private String loaiTinNhan;
    private String duongDanFile;
    private String anhDaiDien;
    private String tenNguoiGui;

    public Message(String noiDung, boolean isMine, int maNguoiGui, String loaiTinNhan, String duongDanFile) {
        this.noiDung = noiDung;
        this.isMine = isMine;
        this.maNguoiGui = maNguoiGui;
        this.loaiTinNhan = loaiTinNhan;
        this.duongDanFile = duongDanFile;
    }

    public Message(String noiDung, boolean isMine, int maNguoiGui, String loaiTinNhan, String duongDanFile, String anhDaiDien, String tenNguoiGui) {
        this.noiDung = noiDung;
        this.isMine = isMine;
        this.maNguoiGui = maNguoiGui;
        this.loaiTinNhan = loaiTinNhan;
        this.duongDanFile = duongDanFile;
        this.anhDaiDien = anhDaiDien;
        this.tenNguoiGui = tenNguoiGui;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public boolean isMine() {
        return isMine;
    }

    public int getMaNguoiGui() {
        return maNguoiGui;
    }

    public String getLoaiTinNhan() {
        return loaiTinNhan;
    }

    public String getDuongDanFile() {
        return duongDanFile;
    }

    public String getAnhDaiDien() { return anhDaiDien; }

    public String getTenNguoiGui() { return tenNguoiGui; }
}
