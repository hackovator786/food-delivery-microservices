package com.hackovation.authservice.dto.request;

import com.hackovation.authservice.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.Set;


@Data
@Builder
public class UserRequest {
    private String userId;
    private String name;
    private String email;
    private Long phoneNumber;
    private UserRole userRole;
}
