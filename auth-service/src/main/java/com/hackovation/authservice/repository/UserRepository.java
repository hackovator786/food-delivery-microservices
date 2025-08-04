package com.hackovation.authservice.repository;

import com.hackovation.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByEmail(String emailId);
    Boolean existsByEmail(String emailId);
    List<User> findAllByLockedAtLessThan(Long time);
}