package com.example.travelplatform.coupon;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    /** 折扣率，如 0.98 表示 98 折 */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal discountRate;
}
