package com.hackovation.authservice.dto.response;

import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
public class ErrorResponse {
    private final Instant timestamp = Instant.now();
    private final Integer status;
    private final String error;
    private final String message;
    private final String path;
}