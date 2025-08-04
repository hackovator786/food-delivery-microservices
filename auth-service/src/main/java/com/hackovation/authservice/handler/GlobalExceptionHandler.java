package com.hackovation.authservice.handler;

import com.hackovation.authservice.dto.response.ErrorResponse;
import com.hackovation.authservice.exception.ApiException;
import com.hackovation.authservice.exception.AuthException;
import com.hackovation.authservice.exception.RegException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active}") // fallback to "dev" if not set
    private String activeProfile;

    // Handle registration/signup-related Exception
    @ExceptionHandler(RegException.class)
    public ResponseEntity<ErrorResponse> handleRegException(RegException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Registration Error", ex.getMessage(), request);
    }

    // Handle custom Authentication Exception
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex, HttpServletRequest request) {
        return buildErrorResponse(ex.getStatus(), "Authentication Error", ex.getMessage(), request);
    }

    // Handle custom APIs related Exception
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "API Error", ex.getMessage(), request);
    }

    // Handle validation errors from @Valid on request bodies
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", message, request);
    }

    // Handle constraint violations on query params, path vars, etc.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Constraint Violation", message, request);
    }

    // Handle invalid JSON payloads
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON", "Invalid request payload", request);
    }

    // Handle MySQL-level constraint violations (e.g. duplicate key, FK errors)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String rootMsg = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "Database constraint violated";

        // Optional: custom mapping for user-friendly messages
        String userMessage = "Database constraint violated";

        if (rootMsg != null && rootMsg.toLowerCase().contains("duplicate")) {
            userMessage = "Duplicate entry already exists";
        } else if (rootMsg != null && rootMsg.toLowerCase().contains("foreign key")) {
            userMessage = "Invalid reference — related entity missing";
        } else if (rootMsg != null && rootMsg.toLowerCase().contains("null")) {
            userMessage = "Missing required data";
        }

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Database Error", userMessage, request);
    }

    // Generic fallback handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
        // Log full stack only for internal debugging
        ex.printStackTrace();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", "Something went wrong", request);
    }

    // Reusable builder method
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, HttpServletRequest request) {
        String path = shouldSendPath() ? request.getRequestURI() : null;
        return new ResponseEntity<>(new ErrorResponse(status.value(), error, message, path), status);
    }

    private boolean shouldSendPath() {
        return !"prod".equalsIgnoreCase(activeProfile);
    }
}
