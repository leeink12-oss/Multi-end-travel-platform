package com.example.travelplatform.order;

import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    List<Order> findByMerchant(Merchant merchant);

    void deleteByUser(User user);
}

