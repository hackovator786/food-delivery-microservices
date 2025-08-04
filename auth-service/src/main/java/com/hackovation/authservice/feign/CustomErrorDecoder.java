package com.hackovation.authservice.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hackovation.authservice.dto.response.ErrResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CustomErrorDecoder implements ErrorDecoder {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            if (response.body() != null) {
                InputStream inputStream = response.body().asInputStream();
                String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                ErrResponse errorResponse = objectMapper.readValue(body, ErrResponse.class);
                return new FeignExceptionWrapper(response.status(), errorResponse);
            } else {
                return new RuntimeException("Empty error response body");
            }
        } catch (IOException e) {
            return new RuntimeException("Error decoding error response", e);
        }
    }
}
