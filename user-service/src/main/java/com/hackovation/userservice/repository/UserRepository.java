package com.hackovation.userservice.repository;

import com.hackovation.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByUserId(String userId);

    Boolean existsByEmail(String email);

    Optional<User> findByUserId(String userId);
}
