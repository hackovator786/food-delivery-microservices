package com.hackovation.restaurantservice.repository;

import com.hackovation.restaurantservice.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query("SELECT r.name FROM Restaurant r WHERE r.restaurantId = :restaurantId AND r.ownerId = :ownerId")
    String getNameByRestaurantIdAndOwnerId(@Param("restaurantId") String restaurantId, @Param("ownerId") String ownerId);

}
