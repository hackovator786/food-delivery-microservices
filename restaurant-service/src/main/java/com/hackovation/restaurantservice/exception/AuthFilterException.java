package com.hackovation.restaurantservice.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthFilterException extends AuthenticationException {
    private static final long serialVersionUID = 1L;
    public static final String CUSTOM_AUTH_ERROR_MESSAGE = "customAuthErrorMessage";

    public AuthFilterException(String msg) {
        super(msg);
    }
    public AuthFilterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}