package com.example.chatrealtime.controller;

import com.example.chatrealtime.entity.TaiKhoan;
import com.example.chatrealtime.repository.TaiKhoanRepository;
import com.example.chatrealtime.service.MailService;
import com.example.chatrealtime.service.OtpService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final TaiKhoanRepository repo;
    private final OtpService otpService;
    private final MailService mailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(TaiKhoanRepository repo,
                          OtpService otpService,
                          MailService mailService) {
        this.repo = repo;
        this.otpService = otpService;
        this.mailService = mailService;
    }

//    @PostMapping("/login")
//    public Map<String, Object> login(
//            @RequestParam String email,
//            @RequestParam String password
//    ) {
//        Map<String, Object> res = new HashMap<>();
//
//        Optional<TaiKhoan> opt = repo.findByEmail(email);
//        if (opt.isEmpty()) {
//            res.put("status", "error");
//            res.put("message", "Email không tồn tại");
//            return res;
//        }
//
//        TaiKhoan tk = opt.get();
//
//        if (!passwordEncoder.matches(password, tk.getMatKhau())) {
//            res.put("status", "error");
//            res.put("message", "Sai mật khẩu");
//            return res;
//        }
//
//        res.put("status", "success");
//        res.put("account", tk);
//        return res;
//    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestParam String email,
            @RequestParam String password
    ) {
        Map<String, Object> res = new HashMap<>();

        Optional<TaiKhoan> opt = repo.findByEmail(email);
        if (opt.isEmpty()) {
            res.put("status", "error");
            res.put("message", "Email không tồn tại");
            return res;
        }

        TaiKhoan tk = opt.get();

        if (!passwordEncoder.matches(password, tk.getMatKhau())) {
            res.put("status", "error");
            res.put("message", "Sai mật khẩu");
            return res;
        }

        // ===== TRẢ RESPONSE CHUẨN CHO ANDROID =====
        Map<String, Object> account = new HashMap<>();
        account.put("maTaiKhoan", tk.getMaTaiKhoan());
        account.put("email", tk.getEmail());

        // 🔥 CỰC KỲ QUAN TRỌNG
        if (tk.getNguoiDung() != null) {
            account.put("maNguoiDung", tk.getNguoiDung().getMaNguoiDung());
        } else {
            account.put("maNguoiDung", null);
        }

        account.put("trangThai", tk.getTrangThai());

        res.put("status", "success");
        res.put("account", account);

        return res;
    }


    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam String email) {
        Map<String, Object> res = new HashMap<>();

        if (repo.findByEmail(email).isPresent()) {
            res.put("status", "error");
            res.put("message", "Email đã được sử dụng");
            return res;
        }

        String otp = otpService.generateOtp(email);
        mailService.sendOtp(email, otp);

        res.put("status", "success");
        res.put("message", "OTP đã được gửi");
        return res;
    }

    @PostMapping("/verify")
    public Map<String, Object> verify(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String otp
    ) {
        Map<String, Object> res = new HashMap<>();

        if (!otpService.verify(email, otp)) {
            res.put("status", "error");
            res.put("message", "OTP không đúng");
            return res;
        }

        TaiKhoan tk = new TaiKhoan();
        tk.setEmail(email);
        tk.setMatKhau(new BCryptPasswordEncoder().encode(password));
        tk.setTrangThai("offline");

        repo.save(tk);
        otpService.clear(email);

        res.put("status", "success");
        res.put("message", "Tạo tài khoản thành công");
        return res;
    }
}
