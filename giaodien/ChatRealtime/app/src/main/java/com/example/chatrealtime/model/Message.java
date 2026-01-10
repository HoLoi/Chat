package com.example.chatrealtime.model;

public class Message {
    private String noiDung;
    private boolean isMine;
    private int maNguoiGui;

    public Message(String noiDung, boolean isMine, int maNguoiGui) {
        this.noiDung = noiDung;
        this.isMine = isMine;
        this.maNguoiGui = maNguoiGui;
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
}
