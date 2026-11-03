package com.example.travelplatform.message;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin
public class AppMessageController {

    private final AppMessageRepository messageRepository;
    private final UserRepository userRepository;

    public AppMessageController(AppMessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('USER','MERCHANT','ADMIN')")
    @GetMapping("/my")
    public ApiResponse<List<AppMessage>> myMessages(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return ApiResponse.success(messageRepository.findByUserOrderByCreateTimeDesc(user));
    }

    @PreAuthorize("hasAnyRole('USER','MERCHANT','ADMIN')")
    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return ApiResponse.success(messageRepository.countByUserAndReadFlagFalse(user));
    }

    @PreAuthorize("hasAnyRole('USER','MERCHANT','ADMIN')")
    @PutMapping("/{id}/read")
    public ApiResponse<AppMessage> markRead(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        AppMessage msg = messageRepository.findById(id).orElseThrow();
        if (!msg.getUser().getId().equals(user.getId())) {
            return ApiResponse.error(403, "无权操作该消息");
        }
        msg.setReadFlag(true);
        return ApiResponse.success(messageRepository.save(msg));
    }

    @PreAuthorize("hasAnyRole('USER','MERCHANT','ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        AppMessage msg = messageRepository.findById(id).orElseThrow();
        if (!msg.getUser().getId().equals(user.getId())) {
            return ApiResponse.error(403, "无权删除该消息");
        }
        messageRepository.delete(msg);
        return ApiResponse.success(null);
    }

    @PreAuthorize("hasAnyRole('USER','MERCHANT','ADMIN')")
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        messageRepository.findByUserOrderByCreateTimeDesc(user).forEach(msg -> msg.setReadFlag(true));
        messageRepository.flush();
        return ApiResponse.success(null);
    }

    @PreAuthorize("hasAnyRole('USER','MERCHANT','ADMIN')")
    @PostMapping
    public ApiResponse<AppMessage> create(@RequestBody AppMessage message, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        message.setUser(user);
        if (message.getCreateTime() == null) {
            message.setCreateTime(LocalDateTime.now());
        }
        return ApiResponse.success(messageRepository.save(message));
    }
}
