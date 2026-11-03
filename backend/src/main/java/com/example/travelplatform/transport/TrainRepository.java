package com.example.travelplatform.transport;

import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.product.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface TrainRepository extends JpaRepository<Train, Long> {

    List<Train> findByMerchant(Merchant merchant);

    List<Train> findByStatus(ResourceStatus status);

    long countByMerchantAndDepartTimeBetween(Merchant merchant, LocalDateTime start, LocalDateTime end);
}

