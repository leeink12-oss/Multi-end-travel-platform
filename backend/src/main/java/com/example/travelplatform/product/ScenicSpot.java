package com.example.travelplatform.product;

import com.example.travelplatform.location.City;
import com.example.travelplatform.merchant.Merchant;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "scenic_spots")
public class ScenicSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 200)
    private String address;

    /**
     * 是否免费景点：true=免费，false/null=收费
     */
    private Boolean free;

    /**
     * 不同人群票价（单位：元），可为空
     */
    private BigDecimal childPrice;

    private BigDecimal adultPrice;

    private BigDecimal studentPrice;

    /**
     * 参考价格，用于列表展示和旧逻辑兼容：
     * - 免费景点=0
     * - 收费景点=优先取成人票价，否则取最小的非空票价
     */
    private BigDecimal price;

    @Column(length = 255)
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceStatus status = ResourceStatus.ONLINE;
}

