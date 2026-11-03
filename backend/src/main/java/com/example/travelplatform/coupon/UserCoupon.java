package com.example.travelplatform.coupon;

import com.example.travelplatform.order.Order;
import com.example.travelplatform.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_coupons")
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false)
    private Boolean used = false;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private LocalDateTime usedAt;

    private LocalDateTime createTime = LocalDateTime.now();
}
