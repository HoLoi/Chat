package com.example.chatrealtime.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "BANBE")
public class BanBe {
    @EmbeddedId
    private BanBeId id;

    @Column(name = "trangThai")
    private String trangThai; // cho, dongy, tuchoi

    @Column(name = "ngayCapNhat")
    private LocalDateTime ngayCapNhat;

    public BanBe() {}

    public BanBe(BanBeId id, String trangThai) {
        this.id = id;
        this.trangThai = trangThai;
    }

    public BanBeId getId() {
        return id;
    }

    public void setId(BanBeId id) {
        this.id = id;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }
}
