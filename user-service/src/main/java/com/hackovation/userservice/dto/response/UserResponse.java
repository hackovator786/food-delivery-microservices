package com.hackovation.userservice.dto.response;

import com.hackovation.userservice.enums.UserRole;
import lombok.*;

import java.util.Set;

@Data
@Builder
public class UserResponse {
    private String userId;
    private String name;
    private String email;
    private Long phoneNumber;
    private Set<Integer> userRoles;
}
