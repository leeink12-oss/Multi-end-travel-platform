package com.example.travelplatform.restaurant;

import com.example.travelplatform.location.City;
import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.product.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByStatus(ResourceStatus status);

    List<Restaurant> findByCityAndStatus(City city, ResourceStatus status);

    List<Restaurant> findByNameContainingIgnoreCaseAndStatus(String keyword, ResourceStatus status);

    List<Restaurant> findByCityAndNameContainingIgnoreCaseAndStatus(City city, String keyword, ResourceStatus status);

    List<Restaurant> findByMerchant(Merchant merchant);
}

