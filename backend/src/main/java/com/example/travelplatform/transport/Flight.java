package com.example.travelplatform.transport;

import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.product.ResourceStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, length = 20)
    private String flightNo;

    @Column(nullable = false, length = 50)
    private String fromCity;

    @Column(nullable = false, length = 50)
    private String toCity;

    @Column(nullable = false)
    private LocalDateTime departTime;

    @Column(nullable = false)
    private LocalDateTime arriveTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // 舱位价格（商家自定义，用户下单时可选择）
    @Column(precision = 10, scale = 2)
    private BigDecimal firstClassPrice;   // 头等舱

    @Column(precision = 10, scale = 2)
    private BigDecimal businessClassPrice; // 商务舱

    @Column(precision = 10, scale = 2)
    private BigDecimal economyClassPrice;  // 经济舱

    @Column(length = 255)
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceStatus status = ResourceStatus.ONLINE;
}

