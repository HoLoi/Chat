package com.example.chatrealtime.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    private final Map<String, String> otpStore = new HashMap<>();

    public String generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStore.put(email, otp);
        return otp;
    }

    public boolean verify(String email, String otp) {
        return otp.equals(otpStore.get(email));
    }

    public void clear(String email) {
        otpStore.remove(email);
    }
}