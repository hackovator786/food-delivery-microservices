package com.hackovation.authservice.util;

import java.security.SecureRandom;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class OtpUtil {
    private static final SecureRandom random = new SecureRandom();

    public String generateOtp(Integer digits) {
        int bound = (int) Math.pow(10, digits);
        int otp = random.nextInt(bound - (bound / 10)) + (bound / 10);
        return String.valueOf(otp);
    }

    public String generateAlphaNumericOtp(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    public String hashOtp(String otp) {
        return BCrypt.hashpw(otp, BCrypt.gensalt(12));
    }

    public Boolean verifyOtp(String rawOtp, String hashedOtp) {
        return BCrypt.checkpw(rawOtp, hashedOtp);
    }

    public Long expiryAfterMilliSeconds(Long expirationMs) {
        return System.currentTimeMillis() + expirationMs;
    }
}
