package com.hackovation.userservice.dto.response;

import com.hackovation.userservice.model.UserRole;
import lombok.*;

@Data
@Builder
public class UserResponse {
    private String userId;
    private String name;
    private String email;
    private Long phoneNumber;
    private UserRole userRole;
}
