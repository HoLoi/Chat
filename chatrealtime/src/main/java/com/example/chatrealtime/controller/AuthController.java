package com.example.chatrealtime.controller;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chatrealtime.entity.TaiKhoan;
import com.example.chatrealtime.repository.TaiKhoanRepository;
import com.example.chatrealtime.service.MailService;
import com.example.chatrealtime.service.OtpService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private static final String OTP_PURPOSE_REGISTER = "register";
    private static final String OTP_PURPOSE_CHANGE_PASSWORD = "change-password";
    private static final String OTP_PURPOSE_FORGOT_PASSWORD = "forgot-password";

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

        // CỰC KỲ QUAN TRỌNG
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

        OtpService.OtpSendResult otpResult = otpService.generateOtpForSend(email, OTP_PURPOSE_REGISTER);
        if (otpResult.shouldSend()) {
            mailService.sendOtp(email, otpResult.getOtp());
        }

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

        if (!otpService.verify(email, otp, OTP_PURPOSE_REGISTER)) {
            res.put("status", "error");
            res.put("message", "OTP không đúng");
            return res;
        }

        TaiKhoan tk = new TaiKhoan();
        tk.setEmail(email);
        tk.setMatKhau(new BCryptPasswordEncoder().encode(password));
        tk.setTrangThai("offline");

        repo.save(tk);
        otpService.clear(email, OTP_PURPOSE_REGISTER);

        res.put("status", "success");
        res.put("message", "Tạo tài khoản thành công");
        return res;
    }

    @PostMapping("/request-change-password")
    public Map<String, Object> requestChangePassword(
            @RequestParam String email,
            @RequestParam String oldPassword
    ) {
        Map<String, Object> res = new HashMap<>();

        Optional<TaiKhoan> opt = repo.findByEmail(email);
        if (opt.isEmpty()) {
            res.put("status", "error");
            res.put("message", "Không tìm thấy tài khoản");
            return res;
        }

        TaiKhoan tk = opt.get();
        if (!passwordEncoder.matches(oldPassword, tk.getMatKhau())) {
            res.put("status", "error");
            res.put("message", "Mật khẩu hiện tại không đúng");
            return res;
        }

        OtpService.OtpSendResult otpResult = otpService.generateOtpForSend(email, OTP_PURPOSE_CHANGE_PASSWORD);
        if (otpResult.shouldSend()) {
            mailService.sendOtp(email, otpResult.getOtp());
        }

        res.put("status", "success");
        res.put("message", "OTP đã được gửi");
        return res;
    }

    @PostMapping("/confirm-change-password")
    public Map<String, Object> confirmChangePassword(
            @RequestParam String email,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String otp
    ) {
        Map<String, Object> res = new HashMap<>();

        Optional<TaiKhoan> opt = repo.findByEmail(email);
        if (opt.isEmpty()) {
            res.put("status", "error");
            res.put("message", "Không tìm thấy tài khoản");
            return res;
        }

        if (!otpService.verify(email, otp, OTP_PURPOSE_CHANGE_PASSWORD)) {
            res.put("status", "error");
            res.put("message", "OTP không đúng");
            return res;
        }

        TaiKhoan tk = opt.get();
        if (!passwordEncoder.matches(oldPassword, tk.getMatKhau())) {
            res.put("status", "error");
            res.put("message", "Mật khẩu hiện tại không đúng");
            return res;
        }

        tk.setMatKhau(passwordEncoder.encode(newPassword));
        repo.save(tk);
        otpService.clear(email, OTP_PURPOSE_CHANGE_PASSWORD);

        res.put("status", "success");
        res.put("message", "Đổi mật khẩu thành công");
        return res;
    }

    @PostMapping("/request-forgot-password")
    public Map<String, Object> requestForgotPassword(@RequestParam String email) {
        Map<String, Object> res = new HashMap<>();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        log.info("[FORGOT][REQUEST] email={}", normalizedEmail);

        Optional<TaiKhoan> opt = repo.findByEmail(email);
        if (opt.isEmpty()) {
            log.warn("[FORGOT][REQUEST] email={} not found", normalizedEmail);
            res.put("status", "error");
            res.put("message", "Không tìm thấy tài khoản");
            return res;
        }

        OtpService.OtpSendResult otpResult = otpService.generateOtpForSend(email, OTP_PURPOSE_FORGOT_PASSWORD);
        log.info("[FORGOT][REQUEST] email={} shouldSendOtp={}", normalizedEmail, otpResult.shouldSend());
        if (otpResult.shouldSend()) {
            mailService.sendOtp(email, otpResult.getOtp());
            log.info("[FORGOT][REQUEST] email={} otp mail sent", normalizedEmail);
        }

        res.put("status", "success");
        res.put("message", "OTP đã được gửi");
        return res;
    }

    @PostMapping("/confirm-forgot-password")
    public Map<String, Object> confirmForgotPassword(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        Map<String, Object> res = new HashMap<>();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String otpForLog = otp == null ? "null" : "len=" + otp.trim().length();
        log.info("[FORGOT][CONFIRM] email={} otp({})", normalizedEmail, otpForLog);

        Optional<TaiKhoan> opt = repo.findByEmail(email);
        if (opt.isEmpty()) {
            log.warn("[FORGOT][CONFIRM] email={} not found", normalizedEmail);
            res.put("status", "error");
            res.put("message", "Không tìm thấy tài khoản");
            return res;
        }

        if (!otpService.verify(email, otp, OTP_PURPOSE_FORGOT_PASSWORD)) {
            log.warn("[FORGOT][CONFIRM] email={} otp invalid -> reject", normalizedEmail);
            res.put("status", "error");
            res.put("message", "OTP không đúng");
            return res;
        }

        String newPassword = generateRandomPassword(10);
        TaiKhoan tk = opt.get();
        tk.setMatKhau(passwordEncoder.encode(newPassword));
        repo.save(tk);
        otpService.clear(email, OTP_PURPOSE_FORGOT_PASSWORD);
        log.info("[FORGOT][CONFIRM] email={} password updated", normalizedEmail);

        mailService.sendNewPassword(email, newPassword);
        log.info("[FORGOT][CONFIRM] email={} new password mail sent", normalizedEmail);

        res.put("status", "success");
        res.put("message", "Mật khẩu mới đã được gửi về email");
        return res;
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}

    