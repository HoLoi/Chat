package com.example.chatrealtime.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TrangThaiTinNhanId implements Serializable {

    private Integer maTinNhan;
    private Integer maTaiKhoan;

    public TrangThaiTinNhanId() {}

    public TrangThaiTinNhanId(Integer maTinNhan, Integer maTaiKhoan) {
        this.maTinNhan = maTinNhan;
        this.maTaiKhoan = maTaiKhoan;
    }

    public Integer getMaTinNhan() {
        return maTinNhan;
    }

    public void setMaTinNhan(Integer maTinNhan) {
        this.maTinNhan = maTinNhan;
    }

    public Integer getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(Integer maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrangThaiTinNhanId)) return false;
        TrangThaiTinNhanId that = (TrangThaiTinNhanId) o;
        return Objects.equals(maTinNhan, that.maTinNhan)
                && Objects.equals(maTaiKhoan, that.maTaiKhoan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maTinNhan, maTaiKhoan);
    }
}
