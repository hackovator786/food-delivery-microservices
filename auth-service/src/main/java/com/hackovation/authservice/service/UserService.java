package com.hackovation.authservice.service;


import com.hackovation.authservice.dto.request.LoginRequest;
import com.hackovation.authservice.dto.request.OtpRequest;
import com.hackovation.authservice.dto.request.SignUpRequest;
import com.hackovation.authservice.enums.RequestType;
import com.hackovation.authservice.model.Role;
import com.hackovation.authservice.model.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    void generateOtpAndSend(RequestType requestType, OtpRequest otpRequest) throws Exception;
    Map<String, String> createUserAndGenerateAuthToken(SignUpRequest signUpRequest) throws Exception;
    Map<String,String> authenticateUser(LoginRequest loginRequest) throws Exception;
    String getNewAccessToken(String refreshToken) throws Exception;
    void clearRefreshToken(String refreshToken) throws Exception;
    User getUserByUserId(String userId) throws Exception;
    User getUserByEmail(String email);
    List<User> getAllUsers();
    void updateUserRole(String userId, String roleName) throws Exception;
    void updateFailedAttempts(String email, String strRole);
    void resetFailedAttempts(String email, Role role);
    void unlockUsersWithExpiredLock();
}
