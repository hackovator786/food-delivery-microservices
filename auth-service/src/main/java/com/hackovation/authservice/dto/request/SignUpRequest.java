package com.hackovation.authservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class SignUpRequest extends OtpRequest {
    @NotBlank(message = "Name cannot be empty")
    @Pattern(regexp = "^[a-zA-Z ]{1,40}$", message = "Invalid name")
    private String name;

    private String role;

    @NotBlank(message = "OTP cannot be empty")
    @Pattern(regexp = "^\\d{6}$", message = "Invalid OTP")
    private String otp;
}