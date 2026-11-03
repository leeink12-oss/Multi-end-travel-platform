package com.example.travelplatform.merchant;

import com.example.travelplatform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByUser(User user);

    List<Merchant> findByTypeAndStatus(MerchantType type, MerchantStatus status);

    List<Merchant> findByType(MerchantType type);
}

