package com.example.chatrealtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ThanhVienPhongId implements Serializable {

    @Column(name = "maPhongChat")
    private Integer maPhongChat;

    @Column(name = "maTaiKhoan")
    private Integer maTaiKhoan;

    public ThanhVienPhongId() {}

    public ThanhVienPhongId(Integer maPhongChat, Integer maTaiKhoan) {
        this.maPhongChat = maPhongChat;
        this.maTaiKhoan = maTaiKhoan;
    }

    //  BẮT BUỘC PHẢI CÓ
    public Integer getMaPhongChat() {
        return maPhongChat;
    }

    public Integer getMaTaiKhoan() {
        return maTaiKhoan;
    }

    // equals + hashCode BẮT BUỘC cho EmbeddedId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThanhVienPhongId)) return false;
        ThanhVienPhongId that = (ThanhVienPhongId) o;
        return Objects.equals(maPhongChat, that.maPhongChat)
                && Objects.equals(maTaiKhoan, that.maTaiKhoan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhongChat, maTaiKhoan);
    }
}