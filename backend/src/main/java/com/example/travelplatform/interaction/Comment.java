package com.example.travelplatform.interaction;

import com.example.travelplatform.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private FavoriteTargetType targetType;

    @Column(nullable = false, length = 500)
    private String content;

    private Integer score;

    @Column(nullable = false)
    private Boolean visible = true;

    private LocalDateTime createTime = LocalDateTime.now();

    // 商家回复内容与时间
    @Column(length = 500)
    private String replyContent;

    private LocalDateTime replyTime;
}

