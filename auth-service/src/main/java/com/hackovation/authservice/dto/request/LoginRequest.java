package com.hackovation.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
public class LoginRequest extends OtpRequest{
    @NotBlank(message = "OTP cannot be empty")
    @Pattern(regexp = "^\\d{6}$", message = "Invalid OTP")
    private String otp;
}
