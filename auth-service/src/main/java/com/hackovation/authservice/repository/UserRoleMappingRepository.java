package com.hackovation.authservice.repository;

import com.hackovation.authservice.model.User;
import com.hackovation.authservice.model.UserRoleId;
import com.hackovation.authservice.model.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, UserRoleId> {
    UserRoleMapping findByUserIdAndRoleId(Long userId, Long roleId);
    List<UserRoleMapping> findAllByLockedAtLessThan(Long time);
}
