package com.hackovation.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class OtpRequest {
    @NotBlank(message = "Email Address cannot be empty")
    @Size(min = 4, max = 70)
    @Email(message = "Invalid email address")
    private String email;
}