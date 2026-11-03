package com.example.travelplatform.interaction;

import com.example.travelplatform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTargetIdAndTargetTypeAndVisibleTrue(Long targetId, FavoriteTargetType type);

    List<Comment> findByUserOrderByCreateTimeDesc(User user);

    void deleteByUser(User user);
}

