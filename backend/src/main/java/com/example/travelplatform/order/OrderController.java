package com.example.travelplatform.order;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.coupon.Coupon;
import com.example.travelplatform.coupon.UserCoupon;
import com.example.travelplatform.coupon.UserCouponRepository;
import com.example.travelplatform.interaction.FavoriteTargetType;
import com.example.travelplatform.message.AppMessage;
import com.example.travelplatform.message.AppMessageRepository;
import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.merchant.MerchantRepository;
import com.example.travelplatform.product.Hotel;
import com.example.travelplatform.product.HotelRepository;
import com.example.travelplatform.product.HotelRoom;
import com.example.travelplatform.product.HotelRoomPrice;
import com.example.travelplatform.product.HotelRoomPriceRepository;
import com.example.travelplatform.product.HotelRoomRepository;
import com.example.travelplatform.product.ScenicSpot;
import com.example.travelplatform.product.ScenicSpotRepository;
import com.example.travelplatform.transport.Flight;
import com.example.travelplatform.transport.FlightRepository;
import com.example.travelplatform.transport.Train;
import com.example.travelplatform.transport.TrainRepository;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final ScenicSpotRepository scenicSpotRepository;
    private final HotelRepository hotelRepository;
    private final HotelRoomRepository hotelRoomRepository;
    private final HotelRoomPriceRepository hotelRoomPriceRepository;
    private final UserCouponRepository userCouponRepository;
    private final FlightRepository flightRepository;
    private final TrainRepository trainRepository;
    private final AppMessageRepository appMessageRepository;

    public OrderController(OrderRepository orderRepository,
                           UserRepository userRepository,
                           MerchantRepository merchantRepository,
                           ScenicSpotRepository scenicSpotRepository,
                           HotelRepository hotelRepository,
                           HotelRoomRepository hotelRoomRepository,
                           HotelRoomPriceRepository hotelRoomPriceRepository,
                           UserCouponRepository userCouponRepository,
                           FlightRepository flightRepository,
                           TrainRepository trainRepository,
                           AppMessageRepository appMessageRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
        this.scenicSpotRepository = scenicSpotRepository;
        this.hotelRepository = hotelRepository;
        this.hotelRoomRepository = hotelRoomRepository;
        this.hotelRoomPriceRepository = hotelRoomPriceRepository;
        this.userCouponRepository = userCouponRepository;
        this.flightRepository = flightRepository;
        this.trainRepository = trainRepository;
        this.appMessageRepository = appMessageRepository;
    }

    @Data
    public static class CreateOrderRequest {
        private Long targetId;
        private FavoriteTargetType targetType;
        @Min(1)
        private Integer quantity;
        private String useDate; // yyyy-MM-dd（景点）
        // 景点：收费景点需要选择票种（CHILD/ADULT/STUDENT）
        private String ticketType;
        // 酒店：房型 + 入住/离店
        private Long roomId;
        private String checkIn; // yyyy-MM-dd
        private String checkOut; // yyyy-MM-dd
        @NotBlank
        private String contactName;
        @NotBlank
        private String contactPhone;
        /**
         * 可选：用户优惠券 ID（user_coupons.id）。
         * 说明：
         * - 优惠券必须属于当前用户且未使用
         * - 折扣在后端统一计算并落库，避免前端篡改价格
         */
        private Long userCouponId;

        /**
         * 付款方式：ALIPAY / WECHAT / UNIONPAY
         */
        private String paymentMethod;

        /**
         * 出行订单舱位/席别（targetType=FLIGHT/TRAIN 时使用）
         */
        private String transportSeatClass;
    }

    // 用户下单
    @PreAuthorize("hasAnyRole('USER','MERCHANT')")
    @PostMapping
    public ApiResponse<Order> create(@RequestBody CreateOrderRequest request,
                                     Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();

        Merchant merchant;
        BigDecimal totalPrice;
        String targetName = null;
        if (request.getTargetType() == FavoriteTargetType.SCENIC) {
            ScenicSpot spot = scenicSpotRepository.findById(request.getTargetId()).orElseThrow();
            merchant = spot.getMerchant();
            targetName = spot.getName();
            BigDecimal unitPrice;
            // 免费景点：总价为 0，仍需选择预约时间
            if (Boolean.TRUE.equals(spot.getFree())) {
                unitPrice = BigDecimal.ZERO;
            } else {
                // 收费景点：必须选择票种
                if (request.getTicketType() == null || request.getTicketType().isBlank()) {
                    return ApiResponse.error(400, "收费景点下单需要选择票种");
                }
                String type = request.getTicketType().toUpperCase();
                switch (type) {
                    case "CHILD" -> {
                        if (spot.getChildPrice() == null) {
                            return ApiResponse.error(400, "未设置儿童票价格");
                        }
                        unitPrice = spot.getChildPrice();
                    }
                    case "ADULT" -> {
                        if (spot.getAdultPrice() == null) {
                            return ApiResponse.error(400, "未设置成人票价格");
                        }
                        unitPrice = spot.getAdultPrice();
                    }
                    case "STUDENT" -> {
                        if (spot.getStudentPrice() == null) {
                            return ApiResponse.error(400, "未设置学生票价格");
                        }
                        unitPrice = spot.getStudentPrice();
                    }
                    default -> {
                        return ApiResponse.error(400, "无效的票种类型");
                    }
                }
            }
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));
        } else if (request.getTargetType() == FavoriteTargetType.HOTEL) {
            Hotel hotel = hotelRepository.findById(request.getTargetId()).orElseThrow();
            merchant = hotel.getMerchant();
            targetName = hotel.getName();
            // 酒店订单按“房型 + 入住/离店日期”计算总价
            if (request.getRoomId() == null || request.getCheckIn() == null || request.getCheckOut() == null) {
                return ApiResponse.error(400, "酒店下单需要选择房型并填写入住/离店日期");
            }
            LocalDate checkIn = LocalDate.parse(request.getCheckIn());
            LocalDate checkOut = LocalDate.parse(request.getCheckOut());
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (nights <= 0) {
                return ApiResponse.error(400, "离店日期必须晚于入住日期");
            }
            HotelRoom room = hotelRoomRepository.findById(request.getRoomId()).orElseThrow();
            if (!room.getHotel().getId().equals(hotel.getId())) {
                return ApiResponse.error(400, "房型不属于该酒店");
            }

            BigDecimal sum = BigDecimal.ZERO;
            LocalDate d = checkIn;
            for (int i = 0; i < nights; i++) {
                BigDecimal dayPrice = getOrCreateRoomPrice(room, d);
                sum = sum.add(dayPrice);
                d = d.plusDays(1);
            }
            totalPrice = sum.multiply(BigDecimal.valueOf(request.getQuantity()));
        } else if (request.getTargetType() == FavoriteTargetType.FLIGHT) {
            Flight flight = flightRepository.findById(request.getTargetId()).orElseThrow();
            merchant = flight.getMerchant();
            targetName = flight.getFlightNo();
            BigDecimal unitPrice = resolveFlightPrice(flight, request.getTransportSeatClass());
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));
        } else if (request.getTargetType() == FavoriteTargetType.TRAIN) {
            Train train = trainRepository.findById(request.getTargetId()).orElseThrow();
            merchant = train.getMerchant();
            targetName = train.getTrainNo();
            BigDecimal unitPrice = resolveTrainPrice(train, request.getTransportSeatClass());
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));
        } else {
            return ApiResponse.error(400, "无效的下单类型");
        }

        BigDecimal priceBeforeCoupon = totalPrice;
        BigDecimal priceAfterCoupon = totalPrice;
        String couponName = null;
        UserCoupon userCouponEntity = null;
        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            return ApiResponse.error(400, "请选择付款方式");
        }

        String paymentMethod = request.getPaymentMethod().trim().toUpperCase();
        if (!paymentMethod.equals("ALIPAY") && !paymentMethod.equals("WECHAT") && !paymentMethod.equals("UNIONPAY")) {
            return ApiResponse.error(400, "无效的付款方式");
        }

        if (request.getUserCouponId() != null) {
            UserCoupon uc = userCouponRepository.findById(request.getUserCouponId()).orElse(null);
            if (uc == null || !uc.getUser().getId().equals(user.getId())) {
                return ApiResponse.error(400, "优惠券无效或不属于当前用户");
            }
            if (Boolean.TRUE.equals(uc.getUsed())) {
                return ApiResponse.error(400, "该优惠券已使用");
            }
            Coupon coupon = uc.getCoupon();
            // 金额统一保留 2 位小数，避免浮点误差带来的“前端显示/后端落库不一致”
            priceAfterCoupon = totalPrice.multiply(coupon.getDiscountRate()).setScale(2, RoundingMode.HALF_UP);
            couponName = coupon.getName();
            userCouponEntity = uc;
        }

        Order order = new Order();
        order.setUser(user);
        order.setMerchant(merchant);
        order.setTargetId(request.getTargetId());
        order.setTargetType(request.getTargetType());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(priceAfterCoupon);
        order.setPaymentMethod(paymentMethod);
        if (couponName != null) {
            order.setCouponName(couponName);
            order.setPriceBeforeCoupon(priceBeforeCoupon);
            order.setPriceAfterCoupon(priceAfterCoupon);
        }
        if (request.getUseDate() != null) {
            order.setUseDate(LocalDate.parse(request.getUseDate()));
        }
        if (request.getTargetType() == FavoriteTargetType.SCENIC && request.getTicketType() != null) {
            order.setTicketType(request.getTicketType().toUpperCase());
        }
        if (request.getTargetType() == FavoriteTargetType.HOTEL) {
            order.setRoomId(request.getRoomId());
            order.setCheckInDate(LocalDate.parse(request.getCheckIn()));
            order.setCheckOutDate(LocalDate.parse(request.getCheckOut()));
        }
        order.setContactName(request.getContactName());
        order.setContactPhone(request.getContactPhone());
        if (request.getTargetType() == FavoriteTargetType.FLIGHT || request.getTargetType() == FavoriteTargetType.TRAIN) {
            order.setTransportSeatClass(request.getTransportSeatClass());
        }
        order.setStatus(OrderStatus.CREATED);

        order = orderRepository.save(order);

        AppMessage orderMessage = new AppMessage();
        orderMessage.setUser(merchant.getUser());
        orderMessage.setTitle("新的订单提醒");
        orderMessage.setContent(buildMerchantOrderContent(user, order, targetName));
        orderMessage.setCategory("MERCHANT_ORDER");
        orderMessage.setRelatedOrderId(order.getId());
        orderMessage.setRelatedMerchantId(merchant.getId());
        orderMessage.setRelatedTargetId(order.getTargetId());
        orderMessage.setRelatedTargetType(order.getTargetType() != null ? order.getTargetType().name() : null);
        appMessageRepository.save(orderMessage);

        if (userCouponEntity != null) {
            // 先保存订单再回写 user_coupon.order，确保外键引用有效
            userCouponEntity.setUsed(true);
            userCouponEntity.setOrder(order);
            userCouponEntity.setUsedAt(LocalDateTime.now());
            userCouponRepository.save(userCouponEntity);
        }
        return ApiResponse.success(order);
    }

    private BigDecimal resolveFlightPrice(Flight flight, String seatClass) {
        String cls = seatClass != null ? seatClass.trim().toUpperCase() : "";
        return switch (cls) {
            case "FIRST_CLASS" -> requirePrice(flight.getFirstClassPrice(), "请选择头等舱并设置价格");
            case "BUSINESS" -> requirePrice(flight.getBusinessClassPrice(), "请选择商务舱并设置价格");
            case "ECONOMY", "" -> {
                // 未传默认经济舱（更符合用户预订习惯）
                BigDecimal p = flight.getEconomyClassPrice() != null ? flight.getEconomyClassPrice() : flight.getPrice();
                yield requirePrice(p, "航班未设置经济舱价格");
            }
            default -> throw new RuntimeException("无效的舱位类型");
        };
    }

    private BigDecimal resolveTrainPrice(Train train, String seatClass) {
        String cls = seatClass != null ? seatClass.trim().toUpperCase() : "";
        if (train.getTrainType() == com.example.travelplatform.transport.TrainType.EMU) {
            return switch (cls) {
                case "EMU_FIRST_CLASS" -> requirePrice(train.getEmuFirstClassSeatPrice(), "动车未设置头等座价格");
                case "EMU_FIRST" -> requirePrice(train.getEmuFirstSeatPrice(), "动车未设置一等座价格");
                case "EMU_SECOND", "" -> {
                    BigDecimal p = train.getEmuSecondSeatPrice() != null ? train.getEmuSecondSeatPrice() : train.getPrice();
                    yield requirePrice(p, "动车未设置二等座价格");
                }
                case "EMU_NO_SEAT" -> requirePrice(train.getEmuNoSeatPrice(), "动车未设置无座价格");
                default -> throw new RuntimeException("无效的席别类型");
            };
        }
        // TRAIN
        return switch (cls) {
            case "TRAIN_HARD_SEAT", "" -> {
                BigDecimal p = train.getTrainHardSeatPrice() != null ? train.getTrainHardSeatPrice() : train.getPrice();
                yield requirePrice(p, "火车未设置硬座价格");
            }
            case "TRAIN_SOFT_SLEEPER" -> requirePrice(train.getTrainSoftSleeperPrice(), "火车未设置软卧价格");
            case "TRAIN_HARD_SLEEPER" -> requirePrice(train.getTrainHardSleeperPrice(), "火车未设置硬卧价格");
            default -> throw new RuntimeException("无效的席别类型");
        };
    }

    private String buildMerchantOrderContent(User user, Order order, String targetName) {
        StringBuilder sb = new StringBuilder();
        sb.append("店铺/项目：").append(targetName == null || targetName.isBlank() ? "-" : targetName)
          .append("；用户：").append(user.getNickname() != null ? user.getNickname() : user.getUsername())
          .append("；订单类型：").append(order.getTargetType())
          .append("；数量：").append(order.getQuantity())
          .append("；总价：¥").append(order.getTotalPrice())
          .append("；付款方式：").append(formatPaymentMethod(order.getPaymentMethod()));
        if (order.getTargetType() == FavoriteTargetType.SCENIC) {
            sb.append("；出行日期：").append(order.getUseDate() == null ? "-" : order.getUseDate());
            if (order.getTicketType() != null) sb.append("；票种：").append(order.getTicketType());
        } else if (order.getTargetType() == FavoriteTargetType.HOTEL) {
            sb.append("；入住：").append(order.getCheckInDate() == null ? "-" : order.getCheckInDate())
              .append("；离店：").append(order.getCheckOutDate() == null ? "-" : order.getCheckOutDate())
              .append("；房型ID：").append(order.getRoomId() == null ? "-" : order.getRoomId());
        } else if (order.getTargetType() == FavoriteTargetType.FLIGHT || order.getTargetType() == FavoriteTargetType.TRAIN) {
            sb.append("；舱位/席别：").append(order.getTransportSeatClass() == null ? "-" : order.getTransportSeatClass());
        }
        sb.append("；联系人：").append(order.getContactName())
          .append("；电话：").append(order.getContactPhone());
        return sb.toString();
    }

    private String resolveTargetName(Order order) {
        if (order == null || order.getTargetType() == null || order.getTargetId() == null) return null;
        return switch (order.getTargetType()) {
            case HOTEL -> hotelRepository.findById(order.getTargetId()).map(Hotel::getName).orElse(null);
            case SCENIC -> scenicSpotRepository.findById(order.getTargetId()).map(ScenicSpot::getName).orElse(null);
            case FLIGHT -> flightRepository.findById(order.getTargetId()).map(Flight::getFlightNo).orElse(null);
            case TRAIN -> trainRepository.findById(order.getTargetId()).map(Train::getTrainNo).orElse(null);
            default -> null;
        };
    }

    private String buildUserOrderContent(Order order, String targetName, boolean started) {
        StringBuilder sb = new StringBuilder();
        String merchantName = order.getMerchant() != null && order.getMerchant().getUser() != null
                ? order.getMerchant().getUser().getNickname() != null ? order.getMerchant().getUser().getNickname() : order.getMerchant().getUser().getUsername()
                : "商家";
        sb.append("店铺：").append(merchantName)
          .append("；订单名称：").append(targetName == null || targetName.isBlank() ? "-" : targetName)
          .append("；订单类型：").append(order.getTargetType())
          .append("；状态：").append(started ? "已入住/已出行" : "未入住/未出行")
          .append("；数量：").append(order.getQuantity())
          .append("；总价：¥").append(order.getTotalPrice());
        return sb.toString();
    }

    private String formatPaymentMethod(String method) {
        if (method == null) return "-";
        return switch (method) {
            case "ALIPAY" -> "支付宝";
            case "WECHAT" -> "微信";
            case "UNIONPAY" -> "云闪付";
            default -> method;
        };
    }

    private BigDecimal requirePrice(BigDecimal price, String message) {
        if (price == null) {
            throw new RuntimeException(message);
        }
        return price;
    }

    private BigDecimal getOrCreateRoomPrice(HotelRoom room, LocalDate date) {
        return hotelRoomPriceRepository.findByRoomAndDate(room, date)
                .map(HotelRoomPrice::getPrice)
                .orElseGet(() -> {
                    BigDecimal low = room.getMinPrice() != null ? room.getMinPrice() : BigDecimal.ZERO;
                    BigDecimal high = room.getMaxPrice() != null ? room.getMaxPrice() : low;
                    if (high.compareTo(low) < 0) {
                        BigDecimal tmp = low;
                        low = high;
                        high = tmp;
                    }
                    double lowD = low.doubleValue();
                    double highD = Math.max(high.doubleValue(), lowD + 0.01);
                    double random = ThreadLocalRandom.current().nextDouble(lowD, highD);
                    BigDecimal price = BigDecimal.valueOf(random).setScale(2, java.math.RoundingMode.HALF_UP);
                    HotelRoomPrice p = new HotelRoomPrice();
                    p.setRoom(room);
                    p.setDate(date);
                    p.setPrice(price);
                    hotelRoomPriceRepository.save(p);
                    return price;
                });
    }

    // 用户查看自己的订单
    @PreAuthorize("hasAnyRole('USER','MERCHANT')")
    @GetMapping("/my")
    public ApiResponse<List<Order>> myOrders(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return ApiResponse.success(orderRepository.findByUser(user));
    }

    // 商家查看自己的订单
    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/merchant")
    public ApiResponse<List<Order>> merchantOrders(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        return ApiResponse.success(orderRepository.findByMerchant(merchant));
    }

    // 用户发起取消订单申请（仅限未完成/未取消的订单）
    @PreAuthorize("hasAnyRole('USER','MERCHANT')")
    @PutMapping("/{id}/cancel-request")
    public ApiResponse<Order> requestCancel(@PathVariable Long id,
                                            Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Order order = orderRepository.findById(id).orElseThrow();
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权操作该订单");
        }
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            return ApiResponse.error(400, "订单已完成或已取消，无法再申请取消");
        }
        order.setStatus(OrderStatus.CANCEL_REQUESTED);
        order.setCancelRequested(true);
        return ApiResponse.success(orderRepository.save(order));
    }

    // 商家同意取消
    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/{id}/cancel-approve")
    public ApiResponse<Order> approveCancel(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        Order order = orderRepository.findById(id).orElseThrow();
        if (!order.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("无权操作该订单");
        }
        if (order.getStatus() != OrderStatus.CANCEL_REQUESTED) {
            return ApiResponse.error(400, "当前订单未处于取消申请状态");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelRequested(false);
        return ApiResponse.success(orderRepository.save(order));
    }

    // 商家拒绝取消
    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/{id}/cancel-reject")
    public ApiResponse<Order> rejectCancel(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        Order order = orderRepository.findById(id).orElseThrow();
        if (!order.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("无权操作该订单");
        }
        if (order.getStatus() != OrderStatus.CANCEL_REQUESTED) {
            return ApiResponse.error(400, "当前订单未处于取消申请状态");
        }
        order.setStatus(OrderStatus.CREATED);
        order.setCancelRequested(false);
        return ApiResponse.success(orderRepository.save(order));
    }

    // 商家标记行程/入住状态：景点=已出行，酒店=已入住
    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/{id}/started")
    public ApiResponse<Order> updateStarted(@PathVariable Long id,
                                            @RequestParam boolean started,
                                            Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        Order order = orderRepository.findById(id).orElseThrow();
        if (!order.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("无权操作该订单");
        }
        order.setStarted(started);
        Order saved = orderRepository.save(order);

        String targetName = resolveTargetName(order);
        AppMessage msg = new AppMessage();
        msg.setUser(saved.getUser());
        msg.setTitle("订单状态更新");
        msg.setContent(buildUserOrderContent(saved, targetName, started));
        msg.setCategory("USER_ORDER");
        msg.setRelatedOrderId(saved.getId());
        msg.setRelatedMerchantId(merchant.getId());
        msg.setRelatedTargetId(order.getTargetId());
        msg.setRelatedTargetType(order.getTargetType() != null ? order.getTargetType().name() : null);
        appMessageRepository.save(msg);

        return ApiResponse.success(saved);
    }
}

