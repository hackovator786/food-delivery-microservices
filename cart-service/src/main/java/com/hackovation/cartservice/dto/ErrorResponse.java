package com.hackovation.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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