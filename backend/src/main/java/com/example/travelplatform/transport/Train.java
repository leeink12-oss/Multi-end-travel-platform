package com.example.travelplatform.transport;

import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.product.ResourceStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trains")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, length = 20)
    private String trainNo;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TrainType trainType = TrainType.EMU;

    // 动车/高铁席别价格
    @Column(precision = 10, scale = 2)
    private BigDecimal emuFirstClassSeatPrice; // 头等座
    @Column(precision = 10, scale = 2)
    private BigDecimal emuFirstSeatPrice;      // 一等座
    @Column(precision = 10, scale = 2)
    private BigDecimal emuSecondSeatPrice;     // 二等座
    @Column(precision = 10, scale = 2)
    private BigDecimal emuNoSeatPrice;         // 无座

    // 普通火车席别价格
    @Column(precision = 10, scale = 2)
    private BigDecimal trainHardSeatPrice;     // 硬座
    @Column(precision = 10, scale = 2)
    private BigDecimal trainSoftSleeperPrice;  // 软卧（按你的描述“软铺”）
    @Column(precision = 10, scale = 2)
    private BigDecimal trainHardSleeperPrice;  // 硬卧

    @Column(length = 255)
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceStatus status = ResourceStatus.ONLINE;
}

