package com.example.chatrealtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "TRANGTHAI_TINNHAN")
public class TrangThaiTinNhan {

    @EmbeddedId
    private TrangThaiTinNhanId id;

    @Column(name = "trangThai")
    private String trangThai; // sent, delivered, read

    @Column(name = "thoiGianCapNhat")
    private LocalDateTime thoiGianCapNhat;

    // ===== Getter / Setter =====

    public TrangThaiTinNhanId getId() {
        return id;
    }

    public void setId(TrangThaiTinNhanId id) {
        this.id = id;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getThoiGianCapNhat() {
        return thoiGianCapNhat;
    }

    public void setThoiGianCapNhat(LocalDateTime thoiGianCapNhat) {
        this.thoiGianCapNhat = thoiGianCapNhat;
    }
}
