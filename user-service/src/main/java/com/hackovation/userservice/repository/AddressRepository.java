package com.hackovation.userservice.repository;

import com.hackovation.userservice.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    Optional<Address> findByUserIdAndId(Long userId, Long id);
    Optional<Address> findByUserIdAndName(Long userId, String name);

    @Modifying
    @Query("DELETE FROM Address a WHERE a.user.id = :userId AND a.id = :id")
    int deleteByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);
}
