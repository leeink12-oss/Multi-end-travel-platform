package com.example.travelplatform.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendedDishRepository extends JpaRepository<RecommendedDish, Long> {
    List<RecommendedDish> findByRestaurantOrderByLikeCountDescIdDesc(Restaurant restaurant);
}

