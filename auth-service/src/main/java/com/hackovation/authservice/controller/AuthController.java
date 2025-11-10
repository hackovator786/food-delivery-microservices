package com.hackovation.authservice.controller;


import com.hackovation.authservice.dto.request.LoginRequest;
import com.hackovation.authservice.dto.request.OtpRequest;
import com.hackovation.authservice.dto.request.SignUpRequest;
import com.hackovation.authservice.dto.response.ErrorResponse;
import com.hackovation.authservice.dto.response.MessageResponse;
import com.hackovation.authservice.enums.RequestType;
import com.hackovation.authservice.exception.AuthException;
import com.hackovation.authservice.exception.RegException;
import com.hackovation.authservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login/send-otp")
    public ResponseEntity<?> requestLoginOtp(@Valid @RequestBody OtpRequest otpRequest) throws Exception {
        userService.generateOtpAndSend(RequestType.LOGIN, otpRequest);
        return ResponseEntity.ok(new MessageResponse("OTP sent successfully!"));
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) throws Exception {
        try {
            Map<String, String> authResponse = userService.authenticateUser(loginRequest);
            String accessToken = authResponse.get("accessToken");
            String refreshToken = authResponse.get("refreshToken");

            // Create HttpOnly cookie for refresh token which is valid for 30 days
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/auth")
                    .maxAge(30 * 24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Send access token in JSON body
            return ResponseEntity.ok(Map.of("accessToken", accessToken));
        } catch (BadCredentialsException ex){
            userService.updateFailedAttempts(loginRequest.getEmail(), loginRequest.getRole());
            return new ResponseEntity<>(
                    new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Authentication Error", ex.getMessage(), null)
                    , HttpStatus.BAD_REQUEST);
        } catch (AuthException e){
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error during user authentication process");
        }
    }

    @PostMapping("/login/resend-otp")
    public ResponseEntity<?> resendLoginOtp(@Valid @RequestBody OtpRequest otpRequest) throws Exception {
        userService.generateOtpAndSend(RequestType.LOGIN, otpRequest);
        return ResponseEntity.ok(new MessageResponse("OTP resent successfully!"));
    }

    @PostMapping("/signup/send-otp")
    public ResponseEntity<?> requestSignUpOtp(@Valid @RequestBody OtpRequest otpRequest) throws Exception {
        System.out.println("Inside requestSignUpOtp");
        userService.generateOtpAndSend(RequestType.SIGNUP, otpRequest);
        return ResponseEntity.ok(new MessageResponse("OTP sent successfully!"));
    }

    @PostMapping("/signup/verify-otp")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest, HttpServletResponse response) throws Exception {
        try {
            System.out.println("Inside registerUser");
            Map<String, String> createUserAndGenerateAuthTokenResponse = userService.createUserAndGenerateAuthToken(signUpRequest);
            String accessToken = createUserAndGenerateAuthTokenResponse.get("accessToken");
            String refreshToken = createUserAndGenerateAuthTokenResponse.get("refreshToken");

            // Create HttpOnly cookie for refresh token which is valid for 30 days
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/auth")
                    .maxAge(30 * 24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Send access token in JSON body
            return ResponseEntity.ok(Map.of("accessToken", accessToken));
        } catch (RegException e){
            throw e;
        } catch (Exception e) {
            System.out.println("Exception during user registration: " + e);
            throw new Exception("Error during user registration process");
        }
    }

    @PostMapping("/signup/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody OtpRequest otpRequest) throws Exception {
        userService.generateOtpAndSend(RequestType.SIGNUP, otpRequest);
        return ResponseEntity.ok(new MessageResponse("OTP resent successfully!"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken") String refreshToken) throws Exception {
        System.out.println("Inside refreshToken\nRefresh token: " + refreshToken);
        String newAccessToken = userService.getNewAccessToken(refreshToken);
        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken
        ));
    }

    @PutMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) throws Exception {
        System.out.println("Inside logout\nRefresh token: " + refreshToken);
        userService.clearRefreshToken(refreshToken);
        // Clear the cookie on the client-side
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(new MessageResponse("Successfully logged out"));
    }

}
