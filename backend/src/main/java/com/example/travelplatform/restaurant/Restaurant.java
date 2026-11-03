package com.example.travelplatform.restaurant;

import com.example.travelplatform.location.City;
import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.product.ResourceStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "restaurants")
public class Restaurant {

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

    @Column(length = 1000)
    private String description;

    @Column(length = 255)
    private String address;

    private BigDecimal avgPrice;

    @Column(length = 100)
    private String openHours;

    @Column(length = 2000)
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceStatus status = ResourceStatus.DRAFT;
}

