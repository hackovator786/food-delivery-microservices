package com.hackovation.authservice.service;


import com.hackovation.authservice.dto.request.SignUpRequest;
import com.hackovation.authservice.dto.response.AuthTokenResponse;
import com.hackovation.authservice.enums.RequestType;
import com.hackovation.authservice.exception.RegException;
import com.hackovation.authservice.model.User;

import java.util.List;

public interface UserService {
    void generateOtpAndSend(RequestType requestType, String email) throws Exception;
    AuthTokenResponse createUserAndGenerateAuthToken(SignUpRequest signUpRequest) throws Exception;
    User getUserByUserId(String userId) throws Exception;
    User getUserByEmail(String email);
    List<User> getAllUsers();
    void updateUserRole(String userId, String roleName) throws Exception;
    void updateFailedAttempts(String usernameOrEmail);
    void resetFailedAttempts(String usernameOrEmail);
    void verifyOtp(String email, String otp) throws RegException;
    void unlockUsersWithExpiredLock();
}
