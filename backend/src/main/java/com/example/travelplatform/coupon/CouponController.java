package com.example.travelplatform.coupon;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin
public class CouponController {

    private final UserCouponRepository userCouponRepository;
    private final UserRepository userRepository;

    public CouponController(UserCouponRepository userCouponRepository, UserRepository userRepository) {
        this.userCouponRepository = userCouponRepository;
        this.userRepository = userRepository;
    }

    /** 当前用户的优惠券列表；used 不传或 false 只返回未使用，true 返回全部 */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public ApiResponse<List<CouponVO>> myCoupons(@RequestParam(required = false) Boolean used,
                                                 Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<UserCoupon> list = Boolean.TRUE.equals(used)
                ? userCouponRepository.findByUserOrderByCreateTimeDesc(user)
                : userCouponRepository.findByUserAndUsedOrderByCreateTimeDesc(user, false);
        List<CouponVO> result = list.stream()
                .map(uc -> new CouponVO(
                        uc.getId(),
                        uc.getCoupon().getName(),
                        uc.getCoupon().getDiscountRate(),
                        uc.getUsed(),
                        uc.getUsedAt(),
                        uc.getCreateTime()
                ))
                .collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    public record CouponVO(Long id, String couponName, java.math.BigDecimal discountRate,
                           Boolean used, java.time.LocalDateTime usedAt, java.time.LocalDateTime createTime) {}
}
