package com.example.travelplatform.product;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "hotel_rooms")
public class HotelRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false, length = 100)
    private String roomType; // 房型，如大床房、标准间等

    @Column(length = 50)
    private String size; // 房间大小

    @Column(length = 255)
    private String facilities; // 房间设施

    @Column(length = 50)
    private String floor; // 楼层高度

    @Column(length = 255)
    private String imageUrl; // 房型图片

    // 价格区间下限和上限，由商家在新建房型时设置
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}

