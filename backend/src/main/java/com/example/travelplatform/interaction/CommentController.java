package com.example.travelplatform.interaction;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.message.AppMessage;
import com.example.travelplatform.message.AppMessageRepository;
import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.merchant.MerchantRepository;
import com.example.travelplatform.product.Hotel;
import com.example.travelplatform.product.HotelRepository;
import com.example.travelplatform.product.ScenicSpot;
import com.example.travelplatform.product.ScenicSpotRepository;
import com.example.travelplatform.restaurant.Restaurant;
import com.example.travelplatform.restaurant.RestaurantRepository;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin
public class CommentController {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final AppMessageRepository appMessageRepository;
    private final MerchantRepository merchantRepository;
    private final ScenicSpotRepository scenicSpotRepository;
    private final HotelRepository hotelRepository;
    private final RestaurantRepository restaurantRepository;

    public CommentController(CommentRepository commentRepository,
                             UserRepository userRepository,
                             AppMessageRepository appMessageRepository,
                             MerchantRepository merchantRepository,
                             ScenicSpotRepository scenicSpotRepository,
                             HotelRepository hotelRepository,
                             RestaurantRepository restaurantRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.appMessageRepository = appMessageRepository;
        this.merchantRepository = merchantRepository;
        this.scenicSpotRepository = scenicSpotRepository;
        this.hotelRepository = hotelRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Data
    public static class CommentRequest {
        private Long targetId;
        private FavoriteTargetType targetType;
        @NotBlank
        private String content;
        @Min(1)
        @Max(5)
        private Integer score;
    }

    @PreAuthorize("hasAnyRole('USER','MERCHANT')")
    @PostMapping
    public ApiResponse<Comment> add(@RequestBody CommentRequest request,
                                    Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setTargetId(request.getTargetId());
        comment.setTargetType(request.getTargetType());
        comment.setContent(request.getContent());
        comment.setScore(request.getScore());
        Comment saved = commentRepository.save(comment);

        Merchant merchant = resolveMerchantByTarget(saved.getTargetType(), saved.getTargetId());
        if (merchant != null) {
            AppMessage msg = new AppMessage();
            msg.setUser(merchant.getUser());
            msg.setTitle("新的评价提醒");
            msg.setContent("用户 " + user.getNickname() + " 对你的资源提交了评价，请及时查看。");
            msg.setCategory("MERCHANT_COMMENT");
            msg.setRelatedMerchantId(merchant.getId());
            appMessageRepository.save(msg);
        }

        return ApiResponse.success(saved);
    }

    @GetMapping("/by-target")
    public ApiResponse<List<Comment>> list(@RequestParam Long targetId,
                                           @RequestParam FavoriteTargetType type) {
        return ApiResponse.success(
                commentRepository.findByTargetIdAndTargetTypeAndVisibleTrue(targetId, type));
    }

    // 当前登录用户自己的所有评价（任意已登录角色均可查看）
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public ApiResponse<List<Comment>> myComments(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return ApiResponse.success(commentRepository.findByUserOrderByCreateTimeDesc(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/hide")
    public ApiResponse<Comment> hide(@PathVariable Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow();
        comment.setVisible(false);
        return ApiResponse.success(commentRepository.save(comment));
    }

    // 当前用户删除自己的评价
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/my/{id}")
    public ApiResponse<Void> deleteMy(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Comment comment = commentRepository.findById(id).orElseThrow();
        if (!comment.getUser().getId().equals(user.getId())) {
            return ApiResponse.error(403, "不能删除他人的评价");
        }
        commentRepository.delete(comment);
        return ApiResponse.success(null);
    }

    // 商家回复用户评价
    @PreAuthorize("hasRole('MERCHANT')")
    @PostMapping("/{id}/reply")
    public ApiResponse<Comment> reply(@PathVariable Long id,
                                      @RequestBody Map<String, String> body) {
        String replyContent = body.get("replyContent");
        if (replyContent == null || replyContent.isBlank()) {
            return ApiResponse.error(400, "回复内容不能为空");
        }
        Comment comment = commentRepository.findById(id).orElseThrow();
        comment.setReplyContent(replyContent);
        comment.setReplyTime(LocalDateTime.now());
        Comment saved = commentRepository.save(comment);

        Merchant merchant = resolveMerchantByTarget(comment.getTargetType(), comment.getTargetId());
        AppMessage msg = new AppMessage();
        msg.setUser(comment.getUser());
        msg.setTitle("商家回复了你的评价");
        msg.setContent(replyContent);
        msg.setCategory("USER_COMMENT_REPLY");
        msg.setRelatedMerchantId(merchant == null ? null : merchant.getId());
        appMessageRepository.save(msg);

        return ApiResponse.success(saved);
    }

    private Merchant resolveMerchantByTarget(FavoriteTargetType type, Long targetId) {
        if (type == null || targetId == null) return null;
        return switch (type) {
            case SCENIC -> scenicSpotRepository.findById(targetId).map(ScenicSpot::getMerchant).orElse(null);
            case HOTEL -> hotelRepository.findById(targetId).map(Hotel::getMerchant).orElse(null);
            case RESTAURANT -> restaurantRepository.findById(targetId).map(Restaurant::getMerchant).orElse(null);
            default -> null;
        };
    }
}

