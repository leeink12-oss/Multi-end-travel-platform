package com.example.travelplatform.restaurant;

import com.example.travelplatform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrouponOrderRepository extends JpaRepository<GrouponOrder, Long> {
    List<GrouponOrder> findByUserOrderByCreateTimeDesc(User user);

    List<GrouponOrder> findByGrouponPackageRestaurantOrderByCreateTimeDesc(Restaurant restaurant);
}

