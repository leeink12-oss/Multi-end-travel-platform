package com.example.travelplatform.restaurant;

import com.example.travelplatform.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "recommended_dish_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"dish_id", "user_id"})
)
public class RecommendedDishLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private RecommendedDish dish;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

