package com.example.travelplatform.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrouponPackageRepository extends JpaRepository<GrouponPackage, Long> {
    List<GrouponPackage> findByRestaurantOrderByIdDesc(Restaurant restaurant);

    List<GrouponPackage> findByRestaurantAndEnabledTrueOrderByIdDesc(Restaurant restaurant);
}

