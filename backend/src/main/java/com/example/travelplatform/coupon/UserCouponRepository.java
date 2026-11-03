package com.example.travelplatform.coupon;

import com.example.travelplatform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUserAndUsedOrderByCreateTimeDesc(User user, Boolean used);

    List<UserCoupon> findByUserOrderByCreateTimeDesc(User user);

    void deleteByUser(User user);
}
