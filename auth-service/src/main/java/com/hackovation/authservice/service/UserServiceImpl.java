package com.hackovation.authservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackovation.authservice.dto.data.EmailVerificationData;
import com.hackovation.authservice.dto.request.LoginRequest;
import com.hackovation.authservice.dto.request.OtpRequest;
import com.hackovation.authservice.dto.request.SignUpRequest;
import com.hackovation.authservice.dto.request.UserRequest;
import com.hackovation.authservice.dto.response.ErrResponse;
import com.hackovation.authservice.dto.response.ErrorResponse;
import com.hackovation.authservice.dto.response.UserResponse;
import com.hackovation.authservice.enums.RequestType;
import com.hackovation.authservice.enums.UserRole;
import com.hackovation.authservice.exception.ApiException;
import com.hackovation.authservice.exception.AuthException;
import com.hackovation.authservice.exception.RegException;
import com.hackovation.authservice.feign.FeignExceptionWrapper;
import com.hackovation.authservice.feign.RestaurantInterface;
import com.hackovation.authservice.feign.UserInterface;
import com.hackovation.authservice.model.Role;
import com.hackovation.authservice.model.User;
import com.hackovation.authservice.model.UserRoleMapping;
import com.hackovation.authservice.repository.RoleRepository;
import com.hackovation.authservice.repository.UserRepository;
import com.hackovation.authservice.repository.UserRoleMappingRepository;
import com.hackovation.authservice.security.OtpAuthenticationToken;
import com.hackovation.authservice.util.RefreshTokenUtils;
import com.hackovation.authservice.util.AccessTokenUtils;
import feign.FeignException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AccessTokenUtils accessTokenUtils;

    @Autowired
    private RefreshTokenUtils refreshTokenUtils;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserInterface userInterface;

    @Autowired
    private RestaurantInterface restaurantInterface;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void generateOtpAndSend(RequestType requestType, OtpRequest otpRequest) throws Exception {
        try {
            System.out.println("Generating OTP for " + requestType.name().toUpperCase() + " request");
            String email = otpRequest.getEmail();
            String strRole = "ROLE_" + otpRequest.getRole().toUpperCase();
            UserRole userRole = UserRole.valueOf(strRole);
            Role role = roleRepository.findByRoleName(userRole).orElseThrow(() -> new RegException("Invalid role"));
            Optional<User> user = userRepository.findByEmail(email);

            if (requestType == RequestType.SIGNUP && user.isPresent() && user.get().getRoles().contains(role)) {
                throw new RegException("Email is already in use!\nPlease enter a different email");
            }
            if (requestType == RequestType.LOGIN && (user.isEmpty() || !user.get().getRoles().contains(role))) {
                throw new AuthException("Email does not exist\nPlease signup or enter a valid email", HttpStatus.BAD_REQUEST);
            }

            String otp = otpService.generateOtp(requestType.name().toUpperCase(), email, userRole.name().toUpperCase());
            String emailData = new ObjectMapper().writeValueAsString(
                    new EmailVerificationData(email, otp));
            kafkaTemplate.send("user-verification", emailData);
        } catch (IllegalArgumentException e) {
            throw new AuthException("Invalid role", HttpStatus.BAD_REQUEST);
        } catch (RegException | AuthException e) {
            throw e;
        } catch (JsonProcessingException e) {
            throw new Exception(e);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Map<String, String> createUserAndGenerateAuthToken(SignUpRequest signUpRequest) throws Exception {
        try {
            String email = signUpRequest.getEmail();
            String otp = signUpRequest.getOtp();
            String strRole = "ROLE_" + signUpRequest.getRole().toUpperCase();
            UserRole userRole = UserRole.valueOf(strRole);
            Role role = roleRepository.findByRoleName(userRole).orElseThrow(() -> new RegException("Invalid role"));
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && existingUser.get().getRoles().contains(role)) {
                throw new RegException("Email is already in use!\nPlease login using this email");
            }

            if (!otpService.validateOtp(
                    RequestType.SIGNUP.name().toUpperCase(), email, otp, userRole.name().toUpperCase())) {
                throw new RegException("Invalid OTP or the OTP has expired");
            }

            String userId = UUID.randomUUID().toString();
            // Update the user role if the user is already present
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                userId = user.getUserId();
                user.addRole(role);
                userRepository.save(user);
            } else {
                // Create a new user's account
                User user = new User(userId,
                        signUpRequest.getEmail()
                );
                user.addRole(role);
                userRepository.save(user);

                // Create user in user service
                ResponseEntity<?> response = response = userInterface.createUser(
                        UserRequest
                                .builder()
                                .userId(userId)
                                .name(signUpRequest.getName())
                                .email(signUpRequest.getEmail())
                                .phoneNumber(null)
                                .build());
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof Map<?, ?> map) {
                    UserResponse userResponse = objectMapper.convertValue(map, UserResponse.class);
                } else if (response.getBody() instanceof Map<?, ?> map) {
                    ErrorResponse error = objectMapper.convertValue(map, ErrorResponse.class);
                    System.out.println("Error: " + error.getMessage());
                } else {
                    System.out.println("Unexpected response format");
                }
            }

            CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsernameAndRole(userId, role);

            // Generate an access token
            String accessToken = accessTokenUtils.generateAccessToken(customUserDetails,
                    "roleId", role.getRoleId());

            // Generate refresh token
            String refreshToken = UUID.randomUUID().toString();
            String refreshTokenHash = refreshTokenUtils.hashRefreshToken(refreshToken);
            String refreshTokenPayload = refreshTokenUtils.generateRefreshToken(customUserDetails, refreshToken, role.getRoleId());

            // Store refresh token hash in a database
            User savedUser = userRepository.findByUserId(customUserDetails.getUserId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            UserRoleMapping userRoleMapping = savedUser.getUserRoleMapping(role);
            userRoleMapping.setRefreshToken(refreshTokenHash);
            userRepository.save(savedUser);

            if (accessToken == null || refreshToken == null) {
                throw new Exception("Error occurred while creating user");
            }

            // Delete OTP in Redis
            otpService.deleteOtp(RequestType.SIGNUP.name().toUpperCase(), signUpRequest.getEmail(), userRole.name().toUpperCase());

            return Map.of("accessToken", accessToken, "refreshToken", refreshTokenPayload);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new RegException("Invalid role");
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
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Map<String, String> authenticateUser(LoginRequest loginRequest) throws Exception {
        Role role = null;
        try {
            // Authenticate the user
            String strRole = "ROLE_" + loginRequest.getRole().toUpperCase();
            UserRole userRole = UserRole.valueOf(strRole);
            role = roleRepository.findByRoleName(userRole).orElseThrow(() -> new AuthException("Invalid role", HttpStatus.BAD_REQUEST));
            Authentication authRequest = new OtpAuthenticationToken(loginRequest.getEmail(), loginRequest.getOtp(), userRole.name().toUpperCase());
            Authentication authentication = authenticationManager.authenticate(authRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Reset failed attempts to zero
            resetFailedAttempts(loginRequest.getEmail(), role);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Get Restaurant ID if the role is RESTAURANT_OWNER
            String restaurantId = null;
            if(userRole.name().equals("ROLE_RESTAURANT_OWNER")) {
                restaurantId = getRestaurantId(userDetails);
            }

            // Generate an access token
            String accessToken = accessTokenUtils.generateAccessToken(userDetails,
                    "roleId", role.getRoleId(),
                    "restaurantId", restaurantId
            );

            // Generate refresh token
            String refreshToken = UUID.randomUUID().toString();
            String refreshTokenHash = refreshTokenUtils.hashRefreshToken(refreshToken);
            String refreshTokenPayload = refreshTokenUtils.generateRefreshToken(userDetails, refreshToken, role.getRoleId());

            // Store refresh token hash in a database
            User user = userRepository.findByUserId(userDetails.getUserId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            UserRoleMapping userRoleMapping = user.getUserRoleMapping(role);
            userRoleMapping.setRefreshToken(refreshTokenHash);
            userRepository.save(user);

            if (accessToken == null || refreshToken == null) {
                throw new Exception("Error creating access token or refresh token");
            }

            // Delete otp from redis
            otpService.deleteOtp(RequestType.LOGIN.name().toUpperCase(), loginRequest.getEmail(), userRole.name().toUpperCase());

            return Map.of("accessToken", accessToken, "refreshToken", refreshTokenPayload);
        } catch (IllegalArgumentException e){
            throw new AuthException("Invalid role", HttpStatus.BAD_REQUEST);
        } catch (BadCredentialsException ex) {
            throw ex;
        } catch (DisabledException ex) {
            throw new AuthException("Invalid credentials or the user is disabled", HttpStatus.UNAUTHORIZED);
        } catch (LockedException ex) {
            throw new AuthException("Invalid credentials or the account is locked", HttpStatus.UNAUTHORIZED);
        } catch (CredentialsExpiredException ex) {
            throw new AuthException("Your password has expired. Please reset it.", HttpStatus.UNAUTHORIZED);
        } catch (AccountExpiredException ex) {
            throw new AuthException("Your account has expired. Contact support.", HttpStatus.UNAUTHORIZED);
        } catch (AuthenticationException ex) {
            throw new AuthException("Authentication failed", HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public String getNewAccessToken(String refreshToken) throws Exception {
        if (refreshToken == null) {
            throw new AuthException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        String userId = refreshTokenUtils.getUserIdFromRefToken(refreshToken);
        Integer roleId = refreshTokenUtils.getRoleIdFromRefToken(refreshToken);
        String rawRefreshToken = refreshTokenUtils.getRawTokenFromRefToken(refreshToken);

        if (userId == null || rawRefreshToken == null || roleId == null) {
            throw new AuthException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        Role role = roleRepository.findByRoleName(UserRole.getRoleById(roleId)).orElseThrow(() -> new AuthException("Invalid refresh token", HttpStatus.UNAUTHORIZED));
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(!user.getRoles().contains(role))
            throw new AuthException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        UserRoleMapping userRoleMapping = user.getUserRoleMapping(role);
        if (userRoleMapping == null)
            throw new AuthException("Invalid refresh token", HttpStatus.UNAUTHORIZED);

        String refreshTokenHash = userRoleMapping.getRefreshToken();
        if (refreshTokenHash == null || !refreshTokenUtils.verifyRefreshToken(rawRefreshToken, refreshTokenHash)) {
            throw new AuthException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        CustomUserDetails userDetails = customUserDetailsService.loadUserByUsernameAndRole(userId, role);

        // Get Restaurant ID if the role is RESTAURANT_OWNER
        String restaurantId = null;
        if(role.getRoleName() == UserRole.ROLE_RESTAURANT_OWNER) {
            restaurantId = getRestaurantId(userDetails);
        }

        return accessTokenUtils.generateAccessToken(userDetails,
                "roleId",roleId,
                "restaurantId", restaurantId
        );
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void clearRefreshToken(String refreshToken) throws Exception {
        if (refreshToken == null) {
            throw new ApiException("Invalid refresh token");
        }
        String userId = refreshTokenUtils.getUserIdFromRefToken(refreshToken);
        Integer roleId = refreshTokenUtils.getRoleIdFromRefToken(refreshToken);
        String rawRefreshToken = refreshTokenUtils.getRawTokenFromRefToken(refreshToken);
        if (userId == null || rawRefreshToken == null || roleId == null) {
            throw new ApiException("Invalid refresh token");
        }

        Role role = roleRepository.findByRoleName(UserRole.getRoleById(roleId)).orElseThrow(() -> new ApiException("Invalid refresh token"));
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserRoleMapping userRoleMapping = user.getUserRoleMapping(role);
        if (userRoleMapping == null) return;

        String refreshTokenHash = userRoleMapping.getRefreshToken();
        if (refreshTokenHash == null) return;

        userRoleMapping.setRefreshToken(null);
        userRoleMappingRepository.save(userRoleMapping);
        System.out.println("Ref token cleared");
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
    @Transactional(rollbackFor = {Exception.class})
    public void updateUserRole(String userId, String roleName) throws Exception {
        User user = userRepository.findByUserId(userId).orElseThrow(
                () -> new UsernameNotFoundException("User not found"));
        UserRole userRole = UserRole.valueOf(roleName);
        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void updateFailedAttempts(String email, String strRole) {
        User user = getUserByEmail(email);
        Role role = roleRepository.findByRoleName(UserRole.valueOf("ROLE_" + strRole.toUpperCase())).orElseThrow(() -> new RuntimeException("Role not found: " + strRole.toUpperCase()));
        if (user == null) return;
        if(!user.getRoles().contains(role))return;
        UserRoleMapping userRoleMapping = user.getUserRoleMapping(role);
        userRoleMapping.setFailedAttempts(userRoleMapping.getFailedAttempts() + 1);
        userRoleMapping.setAccountNonLocked(userRoleMapping.getFailedAttempts() < 5);
        userRoleMapping.setLockedAt(userRoleMapping.getAccountNonLocked() ? null : Instant.now().toEpochMilli());
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void resetFailedAttempts(String email, Role role) {
        User user = getUserByEmail(email);
        if (user == null) return;
        if(!user.getRoles().contains(role))return;
        UserRoleMapping userRoleMapping = user.getUserRoleMapping(role);
        userRoleMapping.setFailedAttempts(0);
        userRoleMapping.setAccountNonLocked(true);
        userRoleMapping.setLockedAt(null);
        userRoleMappingRepository.save(userRoleMapping);
    }

    @Transactional(rollbackFor = {Exception.class})
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void unlockUsersWithExpiredLock() {
        Long now = System.currentTimeMillis();
        Long time = now - 15 * 60 * 1000L;
        List<UserRoleMapping> expiredLockedUsers = userRoleMappingRepository.findAllByLockedAtLessThan(time);

        if (!expiredLockedUsers.isEmpty()) {
            expiredLockedUsers.forEach(userRolemapping -> {
                userRolemapping.setFailedAttempts(0);
                userRolemapping.setAccountNonLocked(true);
                userRolemapping.setLockedAt(null);
            });
        }
        userRoleMappingRepository.saveAll(expiredLockedUsers);
        System.out.println(expiredLockedUsers);
    }

    private String getRestaurantId(CustomUserDetails userDetails) throws Exception {
        String restaurantId = null;
        try {
            ResponseEntity<?> response = restaurantInterface.getRestaurantId(userDetails.getUserId());
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof Map<?, ?> map) {
                RestaurantResponse restaurantResponse = objectMapper.convertValue(map, RestaurantResponse.class);
                restaurantId = restaurantResponse.getRestaurantId();
            } else if (response.getBody() instanceof Map<?, ?> map) {
                ErrorResponse error = objectMapper.convertValue(map, ErrorResponse.class);
                System.out.println("Error: " + error.getMessage());
            } else {
                System.out.println("Unexpected response format");
            }
        } catch (FeignException feignException) {
            System.out.println("Error from restaurant service: " + feignException.getMessage());
        } catch (FeignExceptionWrapper ex) {
            ErrResponse error = ex.getErrorResponse();
            System.out.println("Error from restaurant service: " + error.getMessage());
        } catch (Exception e){
            System.out.println("Unexpected error: " + e.getMessage());
        }
        System.out.println("UserServiceImpl --> Restaurant ID: " + restaurantId);
        return restaurantId;
    }

    private static class RestaurantResponse {
        @Getter
        private String restaurantId;
    }
}
