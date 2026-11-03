package com.example.travelplatform.interaction;

import com.example.travelplatform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndTargetIdAndTargetType(User user, Long targetId, FavoriteTargetType type);

    List<Favorite> findByUserAndTargetType(User user, FavoriteTargetType type);

    long countByTargetIdAndTargetType(Long targetId, FavoriteTargetType type);

    void deleteByUser(User user);
}

