package com.example.travelplatform.product;

import com.example.travelplatform.location.City;
import com.example.travelplatform.merchant.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByCityAndStatus(City city, ResourceStatus status);

    List<Hotel> findByNameContainingIgnoreCaseAndStatus(String name, ResourceStatus status);

    List<Hotel> findByMerchant(Merchant merchant);

    List<Hotel> findByStatus(ResourceStatus status);
}

