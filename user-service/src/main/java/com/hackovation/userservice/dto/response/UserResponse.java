package com.hackovation.userservice.dto.response;

import lombok.*;

import java.util.Set;

@Data
@Builder
public class UserResponse {
    private String userId;
    private String name;
    private String email;
    private Long phoneNumber;
}
