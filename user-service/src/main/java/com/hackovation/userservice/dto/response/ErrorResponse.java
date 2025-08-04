package com.hackovation.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private final Instant timestamp = Instant.now();
    private final Integer status;
    private final String error;
    private final String message;
    private final String path;

}