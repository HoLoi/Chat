package com.example.chatrealtime.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class OtpService {

    // OTP có hiệu lực trong 5 phút
    private static final long OTP_TTL_MS = 5 * 60 * 1000L;
    // Thời gian tối thiểu giữa 2 lần gửi OTP cho cùng email/purpose
    private static final long RESEND_COOLDOWN_MS = 10 * 1000L;
    // Purpose mặc định nếu không được cung cấp
    private static final String DEFAULT_PURPOSE = "default";
    // Cấu trúc lưu trữ OTP: key = email|purpose, value = OtpEntry
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    // Tạo OTP mới hoặc trả về OTP hiện tại nếu chưa hết hạn và không trong thời gian cooldown
    public OtpSendResult generateOtpForSend(String email) {
        return generateOtpForSend(email, DEFAULT_PURPOSE);
    }

    public OtpSendResult generateOtpForSend(String email, String purpose) {
        String key = buildKey(email, purpose);
        long now = System.currentTimeMillis();
        OtpEntry existing = otpStore.get(key);

        if (existing != null && !isExpired(existing, now)) {
            if (now - existing.lastSentAt < RESEND_COOLDOWN_MS) {
                return new OtpSendResult(existing.otp, false);
            }

            existing.lastSentAt = now;
            return new OtpSendResult(existing.otp, true);
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStore.put(key, new OtpEntry(otp, now, now));
        return new OtpSendResult(otp, true);
    }

    public boolean verify(String email, String otp) {
        return verify(email, otp, DEFAULT_PURPOSE);
    }

    public boolean verify(String email, String otp, String purpose) {
        if (otp == null) {
            return false;
        }

        String key = buildKey(email, purpose);
        OtpEntry entry = otpStore.get(key);
        if (entry == null || isExpired(entry, System.currentTimeMillis())) {
            otpStore.remove(key);
            return false;
        }
        return otp.trim().equals(entry.otp);
    }

    public void clear(String email) {
        clear(email, DEFAULT_PURPOSE);
    }

    // Xóa OTP sau khi đã sử dụng hoặc khi không còn cần thiết để tránh lưu trữ dữ liệu không cần thiết và giảm rủi ro bảo mật
    public void clear(String email, String purpose) {
        otpStore.remove(buildKey(email, purpose));
    }

    // Kiểm tra OTP đã hết hạn chưa dựa trên thời điểm tạo và TTL
    private boolean isExpired(OtpEntry entry, long now) {
        return now - entry.createdAt > OTP_TTL_MS;
    }

    // Xây dựng key lưu trữ OTP dựa trên email và mục đích, đảm bảo tính nhất quán và tránh lỗi do khoảng trắng hoặc chữ hoa
    private String buildKey(String email, String purpose) {
        String safeEmail = email == null ? "" : email.trim().toLowerCase();
        String safePurpose = purpose == null || purpose.trim().isEmpty()
                ? DEFAULT_PURPOSE
                : purpose.trim().toLowerCase();
        return safeEmail + "|" + safePurpose;
    }

    public static class OtpSendResult {
        private final String otp;
        // Nếu shouldSend = false nghĩa là OTP đã tồn tại và đang trong thời gian cooldown, không nên gửi lại
        private final boolean shouldSend;

        public OtpSendResult(String otp, boolean shouldSend) {
            this.otp = otp;
            this.shouldSend = shouldSend;
        }

        public String getOtp() {
            return otp;
        }

        public boolean shouldSend() {
            return shouldSend;
        }
    }

    private static class OtpEntry {
        private final String otp;
        // Thời điểm tạo OTP, dùng để kiểm tra hết hạn
        private final long createdAt;
        // Thời điểm OTP được gửi lần cuối, dùng để kiểm tra cooldown
        private long lastSentAt;

        private OtpEntry(String otp, long createdAt, long lastSentAt) {
            this.otp = otp;
            this.createdAt = createdAt;
            this.lastSentAt = lastSentAt;
        }
    }
}