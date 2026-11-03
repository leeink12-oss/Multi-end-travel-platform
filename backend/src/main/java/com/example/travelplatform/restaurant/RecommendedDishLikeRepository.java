package com.example.travelplatform.restaurant;

import com.example.travelplatform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecommendedDishLikeRepository extends JpaRepository<RecommendedDishLike, Long> {
    Optional<RecommendedDishLike> findByDishAndUser(RecommendedDish dish, User user);

    long countByDish(RecommendedDish dish);

    void deleteByDish(RecommendedDish dish);

    List<RecommendedDishLike> findByUserAndDishIn(User user, List<RecommendedDish> dishes);
}

