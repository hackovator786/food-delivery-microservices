package com.hackovation.authservice.service;

import com.hackovation.authservice.util.OtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private static final int EXPIRATION_MINUTES = 10;

    @Autowired
    private OtpUtil otpUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String generateOtp(String prefix, String email, String role) {
        String otp = otpUtil.generateOtp(6);
        redisTemplate.opsForValue().set(prefix + ":" + email + ":" + role, otpUtil.hashOtp(otp), EXPIRATION_MINUTES, TimeUnit.MINUTES);
        return otp;
    }

    public Boolean validateOtp(String prefix, String email, String otp, String role) {
        String storedOtp = redisTemplate.opsForValue().get(prefix + ":" + email + ":" + role);
        return storedOtp != null && otpUtil.verifyOtp(otp, storedOtp);
    }

    public void deleteOtp(String prefix, String email, String role) {
        redisTemplate.delete(prefix + ":" +  email + ":" + role);
    }
}
