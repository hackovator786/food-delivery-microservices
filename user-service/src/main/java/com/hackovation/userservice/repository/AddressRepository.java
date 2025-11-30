package com.hackovation.userservice.repository;

import com.hackovation.userservice.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(String userId);
    Optional<Address> findByUserIdAndId(String userId, Long id);
    Boolean deleteByUserIdAndId(String userId, Long id);
}
