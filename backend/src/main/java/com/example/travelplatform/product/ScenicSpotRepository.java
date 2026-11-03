package com.example.travelplatform.product;

import com.example.travelplatform.location.City;
import com.example.travelplatform.merchant.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScenicSpotRepository extends JpaRepository<ScenicSpot, Long> {

    List<ScenicSpot> findByCityAndStatus(City city, ResourceStatus status);

    List<ScenicSpot> findByNameContainingIgnoreCaseAndStatus(String name, ResourceStatus status);

    List<ScenicSpot> findByMerchant(Merchant merchant);

    List<ScenicSpot> findByStatus(ResourceStatus status);
}

