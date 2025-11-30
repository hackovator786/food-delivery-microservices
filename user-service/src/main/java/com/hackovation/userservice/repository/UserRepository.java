package com.hackovation.userservice.repository;

import com.hackovation.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByUserId(String userId);

    Boolean existsByEmail(String email);

    Optional<User> findByUserId(String userId);
}
