package com.hackovation.userservice.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum UserRole {
    ROLE_CUSTOMER(67837),
    ROLE_DELIVERY_AGENT(46390),
    ROLE_RESTAURANT_OWNER(74208),
    ROLE_ADMIN(88373);

    private final Integer roleId;

    UserRole(Integer roleId) {
        this.roleId = roleId;
    }

    public static UserRole getRoleById(Integer roleId) {
        for (UserRole role : UserRole.values()) {
            if (Objects.equals(role.getRoleId(), roleId)) {
                return role;
            }
        }
        return null;
    }

}