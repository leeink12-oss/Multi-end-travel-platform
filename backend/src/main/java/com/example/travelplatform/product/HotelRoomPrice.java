package com.example.travelplatform.product;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "hotel_room_prices",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "price_date"}))
public class HotelRoomPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private HotelRoom room;

    @Column(name = "price_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private BigDecimal price;
}

