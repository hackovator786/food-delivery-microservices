package com.hackovation.cartservice.feign;


import com.hackovation.cartservice.dto.ErrResponse;
import lombok.Getter;

@Getter
public class FeignExceptionWrapper extends RuntimeException {

    private final int status;
    private final ErrResponse errorResponse;

    public FeignExceptionWrapper(int status, ErrResponse errorResponse) {
        super(errorResponse.getMessage());
        this.status = status;
        this.errorResponse = errorResponse;
    }

}
