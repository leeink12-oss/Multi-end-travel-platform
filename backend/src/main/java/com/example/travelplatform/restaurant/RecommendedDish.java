package com.example.travelplatform.restaurant;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "recommended_dishes")
public class RecommendedDish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 2000)
    private String imageUrl;

    @Column(nullable = false)
    private Integer likeCount = 0;
}

