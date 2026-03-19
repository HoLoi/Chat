package com.example.chatrealtime.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email, String otp) throws Exception{
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
//        log.info("[MAIL][OTP] sending to={}", normalizedEmail);
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(email);
//        message.setSubject("Xác nhận OTP");
//        message.setText(
//                "Xin chào,\n\n" +
//                        "Mã OTP của bạn là: " + otp + "\n" +
//                        "Mã có hiệu lực trong 5 phút.\n\n" +
//                        "Trân trọng,\nTalky Team"
//        );
//
//        mailSender.send(message);
//        log.info("[MAIL][OTP] sent to={}", normalizedEmail);
        if (normalizedEmail.isEmpty()) {
            log.warn("[MAIL][OTP] email rong, bo qua gui OTP");
            return;
        }
        log.info("[MAIL][OTP] gui tu={}", normalizedEmail);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setTo(normalizedEmail);
        helper.setFrom("hophuocloi2004@gmail.com", "Talky");
        helper.setSubject("Xác nhận OTP");

        helper.setText(
                "Xin chào,\n\n" +
                        "Mã OTP của bạn là: " + otp + "\n" +
                        "Mã có hiệu lực trong 5 phút.\n\n" +
                        "Trân trọng,\nTalky"
        );

        mailSender.send(message);
    }

    public void sendNewPassword(String email, String newPassword) throws Exception{
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
//        log.info("[MAIL][NEW_PASSWORD] sending to={}", normalizedEmail);
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(email);
//        message.setSubject("Mật khẩu mới");
//        message.setText(
//                "Xin chào,\n\n" +
//                        "Mật khẩu mới của bạn là: " + newPassword + "\n" +
//                        "Vui lòng đăng nhập và đổi mật khẩu sau khi nhận được email này.\n\n" +
//                        "Trân trọng,\nTalky Team"
//        );
//
//        mailSender.send(message);
//        log.info("[MAIL][NEW_PASSWORD] sent to={}", normalizedEmail);


        if (normalizedEmail.isEmpty()) {
            log.warn("[MAIL][OTP] email rong, bo qua gui OTP");
            return;
        }
        log.info("[MAIL][OTP] gui tu={}", normalizedEmail);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setTo(normalizedEmail);
        helper.setFrom("hophuocloi2004@gmail.com", "Talky");
        helper.setSubject("Mật khẩu mới");

        helper.setText(
                "Xin chào,\n\n" +
                        "Mật khẩu mới của bạn là: " + newPassword + "\n" +
                        "Vui lòng đăng nhập và đổi mật khẩu sau khi nhận được email này.\n\n" +
                        "Trân trọng,\nTalky Team"
        );

        mailSender.send(message);
    }
}