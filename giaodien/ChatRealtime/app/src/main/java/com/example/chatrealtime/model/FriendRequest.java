package com.example.chatrealtime.model;

public class FriendRequest {

    private int maTaiKhoanGui;
    private String tenNguoiGui;
    private String emailNguoiGui;
    private String avatarUrl;

    // Constructor đầy đủ
    public FriendRequest(int maTaiKhoanGui, String tenNguoiGui, String emailNguoiGui, String avatarUrl) {
        this.maTaiKhoanGui = maTaiKhoanGui;
        this.tenNguoiGui = tenNguoiGui;
        this.emailNguoiGui = emailNguoiGui;
        this.avatarUrl = avatarUrl;
    }

    // Getter
    public int getMaTaiKhoanGui() {
        return maTaiKhoanGui;
    }

    public String getTenNguoiGui() {
        return tenNguoiGui;
    }

    public String getEmailNguoiGui() {
        return emailNguoiGui;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    // Setter (tùy chọn – nếu cần cập nhật)
    public void setMaTaiKhoanGui(int maTaiKhoanGui) {
        this.maTaiKhoanGui = maTaiKhoanGui;
    }

    public void setTenNguoiGui(String tenNguoiGui) {
        this.tenNguoiGui = tenNguoiGui;
    }

    public void setEmailNguoiGui(String emailNguoiGui) {
        this.emailNguoiGui = emailNguoiGui;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
