package com.hackovation.authservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.authservice.dto.data.EmailVerificationData;
import com.hackovation.authservice.dto.request.SignUpRequest;
import com.hackovation.authservice.dto.request.UserRequest;
import com.hackovation.authservice.dto.response.AuthTokenResponse;
import com.hackovation.authservice.dto.response.ErrResponse;
import com.hackovation.authservice.dto.response.ErrorResponse;
import com.hackovation.authservice.dto.response.UserResponse;
import com.hackovation.authservice.enums.RequestType;
import com.hackovation.authservice.enums.UserRole;
import com.hackovation.authservice.exception.RegException;
import com.hackovation.authservice.feign.FeignExceptionWrapper;
import com.hackovation.authservice.feign.UserInterface;
import com.hackovation.authservice.model.User;
import com.hackovation.authservice.repository.UserRepository;
import com.hackovation.authservice.util.SecureJwtUtils;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private SecureJwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserInterface userInterface;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void generateOtpAndSend(RequestType requestType, String email) throws Exception {
        try {
            if (requestType == RequestType.SIGNUP && userRepository.existsByEmail(email)) {
                throw new RegException("Email is already in use!\nPlease login using your email or enter a different email");
            }
            if (requestType == RequestType.LOGIN && !userRepository.existsByEmail(email)) {
                throw new RegException("Email does not exist\\nPlease signup or enter a valid email");
            }
            String otp = otpService.generateOtp(requestType.name().toUpperCase(), email);
            String emailData = new ObjectMapper().writeValueAsString(
                    new EmailVerificationData(email, otp));
            kafkaTemplate.send("user-verification", emailData);
        } catch (JsonProcessingException e) {
            throw new Exception(e);
        } catch (Exception e){
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public AuthTokenResponse createUserAndGenerateAuthToken(SignUpRequest signUpRequest) throws Exception {
        try {
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                throw new RegException("Email is already in use!\nPlease login using your email or enter a different email");
            }

            if(!otpService.validateOtp(
                    RequestType.SIGNUP.name().toUpperCase(), signUpRequest.getEmail(), signUpRequest.getOtp())) {
                throw new RegException("Invalid OTP or the OTP has expired");
            }

            // Create a new user's account
            String strRole = signUpRequest.getRole();
            UserRole role;

            if (strRole == null || strRole.isEmpty() || strRole.equals("customer")) {
                role = UserRole.ROLE_CUSTOMER;
            } else if (strRole.equals("restaurant_owner")) {
                role = UserRole.ROLE_RESTAURANT_OWNER;
            } else if (strRole.equals("delivery_agent")) {
                role = UserRole.ROLE_DELIVERY_AGENT;
            } else {
                role = UserRole.ROLE_CUSTOMER;
            }

            String userId =  UUID.randomUUID().toString();
            User user = new User(userId,
                    signUpRequest.getName(),
                    signUpRequest.getEmail(),
                    role);

            userRepository.save(user);

            // Create user in user service
            ResponseEntity<?> response = userInterface.createUser(
                    UserRequest
                            .builder()
                            .userId(userId)
                            .name(signUpRequest.getName())
                            .email(signUpRequest.getEmail())
                            .phoneNumber(null)
                            .userRole(role)
                            .build());
            System.out.println("Response from user service: " + response);
            System.out.println("Response body from user service: " + response.getBody());
            System.out.println("Response status code from user service: " + response.getStatusCode());


            if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof Map<?, ?> map) {
                UserResponse userResponse = objectMapper.convertValue(map, UserResponse.class);
                System.out.println("Inside if block: User ID: " + userResponse.getUserId());
                System.out.println("Inside if block: User Name: " + userResponse.getName());
                System.out.println("Inside if block: User Email: " + userResponse.getEmail());
                System.out.println("Inside if block: User Phone Number: " + userResponse.getPhoneNumber());
                System.out.println("Inside if block: User Role: " + userResponse.getUserRole());
            } else if (response.getBody() instanceof Map<?, ?> map) {
                ErrorResponse error = objectMapper.convertValue(map, ErrorResponse.class);
                System.out.println("Error: " + error.getMessage());
            } else {
                System.out.println("Unexpected response format");
            }

            // Generate Auth Token
            CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(userId);
            String token = jwtUtils.generateTokenFromUserId(customUserDetails);

            // Delete OTP in Redis
            otpService.deleteOtp(RequestType.SIGNUP.name().toUpperCase(), signUpRequest.getEmail());

            return new AuthTokenResponse(token);
        } catch (FeignException feignException) {
            System.out.println("Error from user service: " + feignException.getMessage());
            throw new RegException("Error from user service: " + feignException.getMessage());
        } catch (FeignExceptionWrapper ex) {
            ErrResponse error = ex.getErrorResponse();
            System.out.println("Error from user service: " + error.getMessage());
            throw new RegException("Error from user service: " + error.getMessage());
        } catch (RegException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unknown Error has occurred");
        }
    }

    @Override
    public User getUserByUserId(String userId) throws Exception {
        Optional<User> user = userRepository.findByUserId(userId);
        return user.orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public User getUserByEmail(String userNameOrEmail) {
        return userRepository.findByEmail(userNameOrEmail).orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void updateUserRole(String userId, String roleName) throws Exception {
        User user = userRepository.findByUserId(userId).orElseThrow(
                () -> new UsernameNotFoundException("User not found"));
        UserRole userRole = UserRole.valueOf(roleName);
        user.setRole(userRole);
        userRepository.save(user);
    }

    @Override
    public void updateFailedAttempts(String email) {
        User user = getUserByEmail(email);
        if(user == null)return;
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        user.setAccountNonLocked(user.getFailedAttempts() < 5);
        user.setLockedAt(user.getAccountNonLocked() ? null: Instant.now().toEpochMilli());
        userRepository.save(user);
    }

    @Override
    public void resetFailedAttempts(String email) {
        User user = getUserByEmail(email);
        if(user == null)return;
        user.setFailedAttempts(0);
        user.setAccountNonLocked(true);
        user.setLockedAt(null);
        userRepository.save(user);
    }

    @Override
    public void verifyOtp(String email, String otp) throws RegException {
        if(otpService.validateOtp(RequestType.SIGNUP.name().toUpperCase(), email, otp))
            throw new RegException("Invalid OTP");
    }

    @Transactional(rollbackFor = {Exception.class})
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void unlockUsersWithExpiredLock() {
        Long now = System.currentTimeMillis();
        Long time = now - 15 * 60 * 1000L;
        List<User> expiredLockedUsers = userRepository.findAllByLockedAtLessThan(time);

        if (!expiredLockedUsers.isEmpty()) {
            expiredLockedUsers.forEach(user -> {
                user.setFailedAttempts(0);
                user.setAccountNonLocked(true);
                user.setLockedAt(null);
            });
        }
        userRepository.saveAll(expiredLockedUsers);
        System.out.println(expiredLockedUsers);
    }

}
