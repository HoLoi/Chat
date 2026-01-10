package com.example.chatrealtime.controller;

import com.example.chatrealtime.entity.NguoiDung;
import com.example.chatrealtime.entity.TaiKhoan;
import com.example.chatrealtime.repository.NguoiDungRepository;
import com.example.chatrealtime.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Value("${app.base-url}")
    private String baseUrl;

    private final TaiKhoanRepository taiKhoanRepo;
    private final NguoiDungRepository nguoiDungRepo;

    public UserController(TaiKhoanRepository taiKhoanRepo,
                          NguoiDungRepository nguoiDungRepo) {
        this.taiKhoanRepo = taiKhoanRepo;
        this.nguoiDungRepo = nguoiDungRepo;
    }

    // =========================
    // TẠO THÔNG TIN NGƯỜI DÙNG
    // =========================
    @PostMapping("/create-info")
    public Map<String, Object> createUserInfo(
            @RequestParam String email,
            @RequestParam String tenNguoiDung,
            @RequestParam String gioiTinh,
            @RequestParam(required = false) String ngaySinh,
            @RequestParam String soDienThoai,
            @RequestParam(required = false) MultipartFile image
    ) throws Exception {

        Map<String, Object> res = new HashMap<>();

        Optional<TaiKhoan> opt = taiKhoanRepo.findByEmail(email);
        if (opt.isEmpty()) {
            res.put("status", "error");
            res.put("message", "Không tìm thấy tài khoản");
            return res;
        }

        TaiKhoan tk = opt.get();

        // ĐÃ CÓ THÔNG TIN → KHÔNG CHO TẠO LẠI
        if (tk.getNguoiDung() != null) {
            res.put("status", "error");
            res.put("message", "Tài khoản đã có thông tin người dùng");
            return res;
        }


        // ===== TẠO NGƯỜI DÙNG =====
        NguoiDung nd = new NguoiDung();
        nd.setTenNguoiDung(tenNguoiDung);
        nd.setGioiTinh(gioiTinh);
        if (ngaySinh != null && !ngaySinh.isEmpty()) {
            nd.setNgaySinh(LocalDate.parse(ngaySinh)); // ✅ YYYY-MM-DD
        }
        nd.setSoDienThoai(soDienThoai);

        nguoiDungRepo.save(nd);

        // ===== UPLOAD ẢNH ĐẠI DIỆN =====
        if (image != null && !image.isEmpty()) {

            String projectDir = System.getProperty("user.dir");
            String uploadDirPath =
                    projectDir + File.separator + "uploads"
                            + File.separator + "avatars";

            File uploadDir = new File(uploadDirPath);

            // 👉 TỰ TẠO THƯ MỤC NẾU CHƯA CÓ
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("📁 Tạo thư mục uploads/avatars: " + created);
            }

            System.out.println("📂 Upload dir = " + uploadDir.getAbsolutePath());

            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            File dest = new File(uploadDir, filename);

            System.out.println("📄 File sẽ lưu tại = " + dest.getAbsolutePath());

            image.transferTo(dest);

            if (dest.exists()) {
                System.out.println("✅ Upload ảnh thành công");
            } else {
                System.out.println("❌ Upload ảnh thất bại");
            }

            // 👉 LƯU URL TƯƠNG ĐỐI (CỰC KỲ QUAN TRỌNG)
            String imageUrl = "/uploads/avatars/" + filename;
            nd.setAnhDaiDien(imageUrl);
            nguoiDungRepo.save(nd);
        }


        // ===== GÁN NGƯỜI DÙNG CHO TÀI KHOẢN =====
        tk.setNguoiDung(nd);
        taiKhoanRepo.save(tk);

        res.put("status", "success");
        res.put("message", "Tạo thông tin người dùng thành công");
        res.put("nguoiDung", nd);
        return res;
    }

    // =========================
    // UPDATE TRẠNG THÁI ONLINE / OFFLINE
    // =========================
    @PostMapping("/update-status")
    public Map<String, Object> updateStatus(
            @RequestParam String email,
            @RequestParam String status
    ) {
        Map<String, Object> res = new HashMap<>();

        Optional<TaiKhoan> opt = taiKhoanRepo.findByEmail(email);
        if (opt.isEmpty()) {
            res.put("status", "error");
            res.put("message", "Không tìm thấy tài khoản");
            return res;
        }

        TaiKhoan tk = opt.get();
        tk.setTrangThai(status);
        taiKhoanRepo.save(tk);

        res.put("status", "success");
        res.put("message", "Cập nhật trạng thái thành công");
        return res;
    }


    @GetMapping("/get-information")
    public Map<String, Object> getInformation(
            @RequestParam String email
    ) {
        Map<String, Object> res = new HashMap<>();

        if (email == null || email.isBlank()) {
            res.put("status", "error");
            res.put("message", "Thiếu email");
            return res;
        }

        return nguoiDungRepo.getUserInfoByEmail(email)
                .map(data -> {
                    // ⚠️ KHÔNG GHÉP baseUrl NỮA
                    Object avatar = data.get("anhDaiDien_URL");
                    if (avatar != null) {
                        data.put("anhDaiDien_URL", avatar.toString());
                    }

                    res.put("status", "success");
                    res.put("data", data);
                    return res;
                })
                .orElseGet(() -> {
                    res.put("status", "error");
                    res.put("message", "Không tìm thấy thông tin người dùng");
                    return res;
                });
    }

    @PostMapping("/update-info")
    public Map<String, Object> updateUserInfo(
            @RequestParam String email,
            @RequestParam String tenNguoiDung,
            @RequestParam String gioiTinh,
            @RequestParam(required = false) String ngaySinh,
            @RequestParam String soDienThoai,
            @RequestParam(required = false) MultipartFile image
    ) {
        Map<String, Object> res = new HashMap<>();

        try {
            Optional<TaiKhoan> opt = taiKhoanRepo.findByEmail(email);
            if (opt.isEmpty()) {
                res.put("status", "error");
                res.put("message", "Không tìm thấy tài khoản");
                return res;
            }

            TaiKhoan tk = opt.get();
            NguoiDung nd = tk.getNguoiDung();

            if (nd == null) {
                res.put("status", "error");
                res.put("message", "Chưa có thông tin người dùng");
                return res;
            }

            // ===== UPDATE TEXT =====
            nd.setTenNguoiDung(tenNguoiDung);
            nd.setGioiTinh(gioiTinh);
            nd.setSoDienThoai(soDienThoai);

            // ✅ SAFE parse date
            if (ngaySinh != null && !ngaySinh.isBlank()) {
                try {
                    nd.setNgaySinh(LocalDate.parse(ngaySinh)); // yyyy-MM-dd
                } catch (Exception e) {
                    res.put("status", "error");
                    res.put("message", "Ngày sinh không hợp lệ");
                    return res;
                }
            }

            // ===== UPLOAD AVATAR =====
            if (image != null && !image.isEmpty()) {
                String projectDir = System.getProperty("user.dir");
                File uploadDir = new File(projectDir + "/uploads/avatars");
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
                File dest = new File(uploadDir, filename);
                image.transferTo(dest);

                nd.setAnhDaiDien("/uploads/avatars/" + filename);
            }

            nguoiDungRepo.save(nd);

            res.put("status", "success");
            res.put("message", "Cập nhật thông tin thành công");
            return res;

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 RẤT QUAN TRỌNG
            res.put("status", "error");
            res.put("message", "Lỗi server");
            return res;
        }
    }

    @GetMapping("/get-user-by-email")
    public Map<String, Object> getUserByEmail(@RequestParam String email) {
        Map<String, Object> res = new HashMap<>();

        if (email == null || email.isBlank()) {
            res.put("status", "error");
            res.put("message", "Thiếu email");
            return res;
        }

        return taiKhoanRepo.findByEmail(email)
                .map(tk -> {
                    res.put("status", "success");
                    res.put("maTaiKhoan", tk.getMaTaiKhoan());
                    return res;
                })
                .orElseGet(() -> {
                    res.put("status", "not_found");
                    return res;
                });
    }

    @GetMapping("/by-id")
    public ResponseEntity<?> getUserById(@RequestParam Integer maTaiKhoan) {

        return nguoiDungRepo.getUserByTaiKhoan(maTaiKhoan)
                .map(data -> ResponseEntity.ok(
                        Map.of("status", "success", "data", data)
                ))
                .orElse(ResponseEntity.ok(
                        Map.of("status", "error", "message", "Không tìm thấy thông tin")
                ));
    }
}
