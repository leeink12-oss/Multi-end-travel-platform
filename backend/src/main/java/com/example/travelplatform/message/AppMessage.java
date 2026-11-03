package com.example.travelplatform.message;

import com.example.travelplatform.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "app_messages")
public class AppMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(nullable = false)
    private Boolean readFlag = false;

    private Long relatedOrderId;

    private Long relatedMerchantId;

    private Long relatedTargetId;

    @Column(length = 20)
    private String relatedTargetType;

    private LocalDateTime createTime = LocalDateTime.now();
}
