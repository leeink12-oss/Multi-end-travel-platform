package com.example.travelplatform.order;

import com.example.travelplatform.interaction.FavoriteTargetType;
import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private FavoriteTargetType targetType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    /**
     * 付款方式：ALIPAY / WECHAT / UNIONPAY
     */
    @Column(length = 20)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.CREATED;

    private LocalDate useDate;

    /**
     * 景点订单票种（CHILD/ADULT/STUDENT），仅 targetType=SCENIC 时使用
     */
    private String ticketType;

    // 酒店订单：选择房型 + 入住/离店日期
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private String contactName;
    private String contactPhone;

    private LocalDateTime createTime = LocalDateTime.now();

    // 用户发起取消申请标记和说明
    private Boolean cancelRequested = false;
    private String cancelReason;

    // 行程/入住状态：景点表示“是否已出行”，酒店表示“是否已入住”
    private Boolean started = false;

    // 优惠券：使用优惠券时记录
    private String couponName;
    private java.math.BigDecimal priceBeforeCoupon;
    private java.math.BigDecimal priceAfterCoupon;

    /**
     * 出行订单（飞机/火车）舱位/席别。
     * 示例：
     * - 航班：FIRST_CLASS / BUSINESS / ECONOMY
     * - 动车：EMU_FIRST_CLASS / EMU_FIRST / EMU_SECOND / EMU_NO_SEAT
     * - 火车：TRAIN_HARD_SEAT / TRAIN_SOFT_SLEEPER / TRAIN_HARD_SLEEPER
     */
    private String transportSeatClass;
}

