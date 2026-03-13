package com.example.chatrealtime.model;

public class Message {
    private Integer id;
    private String noiDung;
    private boolean isMine;
    private int maNguoiGui;
    private Integer maNguoiNhan;
    private String loaiTinNhan;
    private String duongDanFile;
    private String thoiGianGui;
    private String trangThaiTinNhan;
    private String anhDaiDien;
    private String tenNguoiGui;
    private MessageModerationStatus moderationStatus = MessageModerationStatus.CLEAN;

    public Message(String noiDung, boolean isMine, int maNguoiGui, String loaiTinNhan, String duongDanFile) {
        this(null, noiDung, isMine, maNguoiGui, null, loaiTinNhan, duongDanFile, null, null, null, null, MessageModerationStatus.CLEAN);
    }

    public Message(String noiDung, boolean isMine, int maNguoiGui, String loaiTinNhan, String duongDanFile, String anhDaiDien, String tenNguoiGui) {
        this(null, noiDung, isMine, maNguoiGui, null, loaiTinNhan, duongDanFile, null, null, anhDaiDien, tenNguoiGui, MessageModerationStatus.CLEAN);
    }

    public Message(String noiDung, boolean isMine, int maNguoiGui, String loaiTinNhan, String duongDanFile, String anhDaiDien, String tenNguoiGui, MessageModerationStatus moderationStatus) {
        this(null, noiDung, isMine, maNguoiGui, null, loaiTinNhan, duongDanFile, null, null, anhDaiDien, tenNguoiGui, moderationStatus);
    }

    public Message(Integer id, String noiDung, boolean isMine, int maNguoiGui, Integer maNguoiNhan,
                   String loaiTinNhan, String duongDanFile, String thoiGianGui, String trangThaiTinNhan,
                   String anhDaiDien, String tenNguoiGui, MessageModerationStatus moderationStatus) {
        this.id = id;
        this.noiDung = noiDung;
        this.isMine = isMine;
        this.maNguoiGui = maNguoiGui;
        this.maNguoiNhan = maNguoiNhan;
        this.loaiTinNhan = loaiTinNhan;
        this.duongDanFile = duongDanFile;
        this.thoiGianGui = thoiGianGui;
        this.trangThaiTinNhan = trangThaiTinNhan;
        this.anhDaiDien = anhDaiDien;
        this.tenNguoiGui = tenNguoiGui;
        if (moderationStatus != null) {
            this.moderationStatus = moderationStatus;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getMaNguoiNhan() {
        return maNguoiNhan;
    }

    public String getLoaiTinNhan() {
        return loaiTinNhan;
    }

    public String getDuongDanFile() {
        return duongDanFile;
    }

    public String getThoiGianGui() {
        return thoiGianGui;
    }

    public String getTrangThaiTinNhan() {
        return trangThaiTinNhan;
    }

    public void setTrangThaiTinNhan(String trangThaiTinNhan) {
        this.trangThaiTinNhan = trangThaiTinNhan;
    }

    public String getAnhDaiDien() { return anhDaiDien; }

    public String getTenNguoiGui() { return tenNguoiGui; }

    public MessageModerationStatus getModerationStatus() {
        return moderationStatus;
    }
}
