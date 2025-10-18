package com.hackovation.userservice.repository;

import com.hackovation.userservice.enums.UserRole;
import com.hackovation.userservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(UserRole roleName);
}
