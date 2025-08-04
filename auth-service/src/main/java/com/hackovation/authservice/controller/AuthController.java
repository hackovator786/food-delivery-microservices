package com.hackovation.authservice.controller;


import com.hackovation.authservice.dto.request.LoginRequest;
import com.hackovation.authservice.dto.request.OtpRequest;
import com.hackovation.authservice.dto.request.SignUpRequest;
import com.hackovation.authservice.dto.response.AuthTokenResponse;
import com.hackovation.authservice.dto.response.MessageResponse;
import com.hackovation.authservice.enums.RequestType;
import com.hackovation.authservice.exception.AuthException;
import com.hackovation.authservice.security.OtpAuthenticationToken;
import com.hackovation.authservice.service.CustomUserDetails;
import com.hackovation.authservice.service.OtpService;
import com.hackovation.authservice.service.UserService;
import com.hackovation.authservice.util.SecureJwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecureJwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OtpService otpService;

    @PostMapping("/login/send-otp")
    public ResponseEntity<?> requestLoginOtp(@Valid @RequestBody OtpRequest otpRequest) throws Exception {
        userService.generateOtpAndSend(RequestType.LOGIN, otpRequest.getEmail());
        return ResponseEntity.ok(new MessageResponse("OTP sent successfully!"));
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
        try {
            Authentication authRequest = new OtpAuthenticationToken(loginRequest.getEmail(), loginRequest.getOtp());
            Authentication authentication = authenticationManager.authenticate(authRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            userService.resetFailedAttempts(loginRequest.getEmail());

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String authToken = jwtUtils.generateTokenFromUserId(userDetails);

            AuthTokenResponse response = new AuthTokenResponse(authToken);

            otpService.deleteOtp(RequestType.LOGIN.name().toUpperCase(), loginRequest.getEmail());

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException ex){
            try {
                userService.updateFailedAttempts(loginRequest.getEmail());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            throw new AuthException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DisabledException ex){
            throw new AuthException("Invalid credentials or the user is disabled", HttpStatus.UNAUTHORIZED);
        } catch (LockedException ex) {
            throw new AuthException("Invalid credentials or the account is locked", HttpStatus.UNAUTHORIZED);
        } catch (CredentialsExpiredException ex) {
            throw new AuthException("Your password has expired. Please reset it.", HttpStatus.UNAUTHORIZED);
        } catch (AccountExpiredException ex) {
            throw new AuthException("Your account has expired. Contact support.", HttpStatus.UNAUTHORIZED);
        } catch (AuthenticationException ex) {
            throw new AuthException("Authentication failed", HttpStatus.UNAUTHORIZED);
        } catch (Exception ex){
            System.out.println(ex.getMessage());
            throw new Exception("Unknown error has occurred");
        }
    }

    @PostMapping("/login/resend-otp")
    public ResponseEntity<?> resendLoginOtp(@Valid @RequestBody OtpRequest otpRequest) throws Exception {
        userService.generateOtpAndSend(RequestType.LOGIN, otpRequest.getEmail());
        return ResponseEntity.ok(new MessageResponse("OTP resent successfully!"));
    }

    @PostMapping("/signup/send-otp")
    public ResponseEntity<?> requestSignUpOtp(@Valid @RequestBody OtpRequest otpRequest) throws Exception {
        userService.generateOtpAndSend(RequestType.SIGNUP, otpRequest.getEmail());
        return ResponseEntity.ok(new MessageResponse("OTP sent successfully!"));
    }

    @PostMapping("/signup/verify-otp")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) throws Exception {
        return ResponseEntity.ok(userService.createUserAndGenerateAuthToken(signUpRequest));
    }

    @PostMapping("/signup/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody OtpRequest otpRequest) throws Exception {
        userService.generateOtpAndSend(RequestType.SIGNUP, otpRequest.getEmail());
        return ResponseEntity.ok(new MessageResponse("OTP resent successfully!"));
    }

}
