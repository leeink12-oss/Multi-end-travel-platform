package com.example.travelplatform.user;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.coupon.UserCouponRepository;
import com.example.travelplatform.interaction.CommentRepository;
import com.example.travelplatform.interaction.FavoriteRepository;
import com.example.travelplatform.order.OrderRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final UserCouponRepository userCouponRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          OrderRepository orderRepository,
                          FavoriteRepository favoriteRepository,
                          CommentRepository commentRepository,
                          UserCouponRepository userCouponRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
        this.userCouponRepository = userCouponRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 修改密码
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestParam String oldPassword,
                                            @RequestParam String newPassword,
                                            Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ApiResponse.error(400, "原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ApiResponse.success("修改密码成功", null);
    }

    // 更新头像
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/avatar")
    public ApiResponse<String> updateAvatar(@RequestParam String avatarUrl,
                                            Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        return ApiResponse.success(avatarUrl);
    }

    // 注销当前用户（删除账户 + 订单 + 收藏 + 评价）
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMe(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        userCouponRepository.deleteByUser(user);
        orderRepository.deleteByUser(user);
        favoriteRepository.deleteByUser(user);
        commentRepository.deleteByUser(user);
        userRepository.delete(user);
        return ApiResponse.success("账户已注销", null);
    }
}

