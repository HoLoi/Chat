-- app_chat.sql
CREATE DATABASE IF NOT EXISTS app_chat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE app_chat;

-- NGUOIDUNG1
CREATE TABLE IF NOT EXISTS NGUOIDUNG (
    maNguoiDung INT AUTO_INCREMENT PRIMARY KEY,
    tenNguoiDung varchar(100) NOT NULL,
    anhDaiDien_URL VARCHAR(255) DEFAULT NULL,
    gioiTinh VARCHAR(10) DEFAULT NULL,
    ngaySinh DATE DEFAULT NULL,
    soDienThoai VARCHAR(15) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- TAIKHOAN
CREATE TABLE IF NOT EXISTS TAIKHOAN (
    maTaiKhoan INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    matKhau VARCHAR(255) NOT NULL,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    trangThai VARCHAR(50) DEFAULT 'offline', -- offline, online, banned, locked
    token VARCHAR(255) DEFAULT NULL,
    diemCanhCao INT DEFAULT 0,
    soLanWarningHomNay INT DEFAULT 0,
    ngayTinhWarning DATE DEFAULT NULL,
    thoiGianKhoa DATETIME DEFAULT NULL,
    maNguoiDung INT,
    FOREIGN KEY (maNguoiDung) REFERENCES NGUOIDUNG(maNguoiDung)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PHONGCHAT
CREATE TABLE IF NOT EXISTS PHONGCHAT (
    maPhongChat INT AUTO_INCREMENT PRIMARY KEY,
    tenPhongChat VARCHAR(100) NOT NULL,
    kieuNhom TINYINT(1) DEFAULT 0, -- 0=public, 1=private
    maTruongNhom INT DEFAULT NULL,
    loaiPhong TINYINT(1) DEFAULT 0,
    anhDaiDien_URL VARCHAR(255) DEFAULT NULL,
    ngayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    trangThaiPhong VARCHAR(20) DEFAULT NULL, -- active, archived, deleted
    maTaiKhoanTao INT,
    FOREIGN KEY (maTaiKhoanTao) REFERENCES TAIKHOAN(maTaiKhoan),
    FOREIGN KEY (maTruongNhom) REFERENCES TAIKHOAN(maTaiKhoan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- THANHVIEN_PHONG
CREATE TABLE IF NOT EXISTS THANHVIEN_PHONG (
    maPhongChat INT,
    maTaiKhoan INT,
    vaiTro VARCHAR(50) DEFAULT 'member',
    ngayThamGia TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    trangThaiThamGia VARCHAR(20) DEFAULT 'approved', -- pending, approved, rejected
    nguoiDuyet INT DEFAULT NULL,
    ngayXoa TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (maPhongChat, maTaiKhoan),
    FOREIGN KEY (maPhongChat) REFERENCES PHONGCHAT(maPhongChat) ON DELETE CASCADE,
    FOREIGN KEY (maTaiKhoan) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE CASCADE,
    FOREIGN KEY (nguoiDuyet) REFERENCES TAIKHOAN(maTaiKhoan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- TINNHAN
CREATE TABLE IF NOT EXISTS TINNHAN (
    maTinNhan INT AUTO_INCREMENT PRIMARY KEY,
    noiDung TEXT,
    duongDanFile VARCHAR(255) DEFAULT NULL,
    thoiGianGui TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    maTaiKhoanGui INT,
    maPhongChat INT,
    loaiTinNhan varchar(50) DEFAULT 'text',
    trangThaiKiemDuyet VARCHAR(20) DEFAULT 'clean', -- clean, warning, blocked, hidden, flagged
    diemKiemDuyet DECIMAL(5,2) DEFAULT 0.00,
    FOREIGN KEY (maTaiKhoanGui) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE SET NULL,
    FOREIGN KEY (maPhongChat) REFERENCES PHONGCHAT(maPhongChat) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- TRANGTHAI_TINNHAN
CREATE TABLE IF NOT EXISTS TRANGTHAI_TINNHAN (
    maTinNhan INT,
    maTaiKhoan INT,
    trangThai VARCHAR(50) DEFAULT 'sent', -- sent, read
    thoiGianCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (maTinNhan, maTaiKhoan),
    FOREIGN KEY (maTinNhan) REFERENCES TINNHAN(maTinNhan) ON DELETE CASCADE,
    FOREIGN KEY (maTaiKhoan) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Log kiểm duyệt AI
CREATE TABLE IF NOT EXISTS MODERATION_LOG (
    id INT AUTO_INCREMENT PRIMARY KEY,
    maTinNhan INT,
    maPhongChat INT,
    maTaiKhoanGui INT,
    nhanViPham VARCHAR(100) DEFAULT NULL, -- hate, sexual, violence, spam...
    mucDoViPham VARCHAR(20) DEFAULT NULL, -- mild, medium, severe
    diemScore DECIMAL(5,2) DEFAULT 0.00,
    hanhDong VARCHAR(20) DEFAULT 'warn', -- warn, block, hide, flag, ban
    noiDungGoc TEXT,
    thoiGian TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maTinNhan) REFERENCES TINNHAN(maTinNhan) ON DELETE SET NULL,
    FOREIGN KEY (maPhongChat) REFERENCES PHONGCHAT(maPhongChat) ON DELETE SET NULL,
    FOREIGN KEY (maTaiKhoanGui) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- BANBE
CREATE TABLE IF NOT EXISTS BANBE (
    maTaiKhoan1 INT,
    maTaiKhoan2 INT,
    trangThai VARCHAR(50) DEFAULT 'cho', -- cho, dongy, tuchoi
    ngayCapNhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (maTaiKhoan1, maTaiKhoan2),
    FOREIGN KEY (maTaiKhoan1) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE CASCADE,
    FOREIGN KEY (maTaiKhoan2) REFERENCES TAIKHOAN(maTaiKhoan) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- INDEXES
CREATE INDEX idx_tinNhan_maPhongChat ON TINNHAN(maPhongChat);
CREATE INDEX idx_thanhVienPhong_maTaiKhoan ON THANHVIEN_PHONG(maTaiKhoan);
CREATE INDEX idx_thanhVienPhong_trangThai ON THANHVIEN_PHONG(trangThaiThamGia);
CREATE INDEX idx_yeucau_phong ON THANHVIEN_PHONG(maPhongChat, trangThaiThamGia);
CREATE INDEX idx_moderation_tinNhan ON MODERATION_LOG(maTinNhan);