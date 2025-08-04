package com.hackovation.restaurantservice.repository;

import com.hackovation.restaurantservice.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

}
