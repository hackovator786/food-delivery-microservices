package com.hackovation.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends Exception{
    private static final long serialVersionUID = 1L;
    private HttpStatus status;
    public AuthException(String msg, HttpStatus status) {
        super(msg);
        this.status = status;
    }
}