package com.example.travelplatform.transport;

import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.product.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    List<Flight> findByMerchant(Merchant merchant);

    List<Flight> findByStatus(ResourceStatus status);

    long countByMerchantAndDepartTimeBetween(Merchant merchant, LocalDateTime start, LocalDateTime end);
}

