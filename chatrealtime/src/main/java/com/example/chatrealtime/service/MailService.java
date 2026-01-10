package com.example.chatrealtime.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Xác nhận đăng ký tài khoản");
        message.setText(
                "Xin chào,\n\n" +
                        "Mã OTP của bạn là: " + otp + "\n" +
                        "Mã có hiệu lực trong 5 phút.\n\n" +
                        "Trân trọng,\nTalky Team"
        );

        mailSender.send(message);
    }
}