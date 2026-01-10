package com.example.chatrealtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable   // 🔥 BẮT BUỘC
public class BanBeId implements Serializable {

    @Column(name = "maTaiKhoan1")
    private Integer maTaiKhoan1;

    @Column(name = "maTaiKhoan2")
    private Integer maTaiKhoan2;

    public BanBeId() {}

    public BanBeId(Integer maTaiKhoan1, Integer maTaiKhoan2) {
        this.maTaiKhoan1 = maTaiKhoan1;
        this.maTaiKhoan2 = maTaiKhoan2;
    }

    public Integer getMaTaiKhoan1() {
        return maTaiKhoan1;
    }

    public void setMaTaiKhoan1(Integer maTaiKhoan1) {
        this.maTaiKhoan1 = maTaiKhoan1;
    }

    public Integer getMaTaiKhoan2() {
        return maTaiKhoan2;
    }

    public void setMaTaiKhoan2(Integer maTaiKhoan2) {
        this.maTaiKhoan2 = maTaiKhoan2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BanBeId)) return false;
        BanBeId that = (BanBeId) o;
        return Objects.equals(maTaiKhoan1, that.maTaiKhoan1)
                && Objects.equals(maTaiKhoan2, that.maTaiKhoan2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maTaiKhoan1, maTaiKhoan2);
    }
}
