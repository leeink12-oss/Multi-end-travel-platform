package com.example.travelplatform.message;

import com.example.travelplatform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppMessageRepository extends JpaRepository<AppMessage, Long> {
    List<AppMessage> findByUserOrderByCreateTimeDesc(User user);
    long countByUserAndReadFlagFalse(User user);
}
