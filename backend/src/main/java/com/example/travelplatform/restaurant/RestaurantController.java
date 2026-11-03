package com.example.travelplatform.restaurant;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.interaction.Comment;
import com.example.travelplatform.interaction.CommentRepository;
import com.example.travelplatform.message.AppMessage;
import com.example.travelplatform.message.AppMessageRepository;
import com.example.travelplatform.interaction.FavoriteRepository;
import com.example.travelplatform.interaction.FavoriteTargetType;
import com.example.travelplatform.location.City;
import com.example.travelplatform.location.CityRepository;
import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.merchant.MerchantRepository;
import com.example.travelplatform.merchant.MerchantType;
import com.example.travelplatform.product.ResourceStatus;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin
public class RestaurantController {
    private final Random random = new Random();

    private final RestaurantRepository restaurantRepository;
    private final DiningReservationRepository diningReservationRepository;
    private final GrouponPackageRepository grouponPackageRepository;
    private final GrouponOrderRepository grouponOrderRepository;
    private final CityRepository cityRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final RecommendedDishRepository recommendedDishRepository;
    private final RecommendedDishLikeRepository recommendedDishLikeRepository;
    private final AppMessageRepository appMessageRepository;

    public RestaurantController(RestaurantRepository restaurantRepository,
                                DiningReservationRepository diningReservationRepository,
                                GrouponPackageRepository grouponPackageRepository,
                                GrouponOrderRepository grouponOrderRepository,
                                CityRepository cityRepository,
                                MerchantRepository merchantRepository,
                                UserRepository userRepository,
                                FavoriteRepository favoriteRepository,
                                CommentRepository commentRepository,
                                RecommendedDishRepository recommendedDishRepository,
                                RecommendedDishLikeRepository recommendedDishLikeRepository,
                                AppMessageRepository appMessageRepository) {
        this.restaurantRepository = restaurantRepository;
        this.diningReservationRepository = diningReservationRepository;
        this.grouponPackageRepository = grouponPackageRepository;
        this.grouponOrderRepository = grouponOrderRepository;
        this.cityRepository = cityRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
        this.recommendedDishRepository = recommendedDishRepository;
        this.recommendedDishLikeRepository = recommendedDishLikeRepository;
        this.appMessageRepository = appMessageRepository;
    }

    @Data
    public static class RestaurantRequest {
        private Long cityId;
        private String name;
        private String description;
        private String address;
        private BigDecimal avgPrice;
        private String openHours;
        private String coverImage;
    }

    @Data
    public static class ReserveRequest {
        private String reserveTime;
        private Integer peopleCount;
        private String contactName;
        private String contactPhone;
        private String note;
    }

    @Data
    public static class GrouponPackageRequest {
        private Long restaurantId;
        private String title;
        private String description;
        private BigDecimal originalPrice;
        private BigDecimal groupPrice;
        private String imageUrl;
        private Integer stock;
        private Boolean enabled;
    }

    @Data
    public static class GrouponOrderRequest {
        private Integer quantity;
    }

    @Data
    public static class RecommendedDishRequest {
        private Long restaurantId;
        private String name;
        private String description;
        private String imageUrl;
    }

    @GetMapping("/recommend")
    public ApiResponse<List<Restaurant>> recommend(@RequestParam(required = false) Long cityId,
                                                   @RequestParam(required = false, defaultValue = "8") Integer limit) {
        List<Restaurant> source;
        if (cityId != null) {
            City city = cityRepository.findById(cityId).orElseThrow();
            source = restaurantRepository.findByCityAndStatus(city, ResourceStatus.ONLINE);
        } else {
            source = restaurantRepository.findByStatus(ResourceStatus.ONLINE);
        }
        Collections.shuffle(source);
        int finalLimit = Math.max(1, Math.min(limit, 50));
        return ApiResponse.success(source.subList(0, Math.min(finalLimit, source.size())));
    }

    @GetMapping("/search")
    public ApiResponse<List<Restaurant>> search(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Long cityId,
                                                @RequestParam(required = false, defaultValue = "20") Integer limit) {
        List<Restaurant> source;
        boolean blankKeyword = keyword == null || keyword.isBlank();
        int finalLimit = Math.max(1, Math.min(limit, 100));
        if (cityId != null) {
            City city = cityRepository.findById(cityId).orElseThrow();
            source = blankKeyword
                    ? restaurantRepository.findByCityAndStatus(city, ResourceStatus.ONLINE)
                    : restaurantRepository.findByCityAndNameContainingIgnoreCaseAndStatus(city, keyword, ResourceStatus.ONLINE);
            return ApiResponse.success(source.subList(0, Math.min(source.size(), finalLimit)));
        }
        if (blankKeyword) {
            source = restaurantRepository.findByStatus(ResourceStatus.ONLINE);
            Collections.shuffle(source);
            return ApiResponse.success(source.subList(0, Math.min(source.size(), finalLimit)));
        }
        source = restaurantRepository.findByNameContainingIgnoreCaseAndStatus(keyword, ResourceStatus.ONLINE);
        return ApiResponse.success(source.subList(0, Math.min(source.size(), finalLimit)));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id, Authentication authentication) {
        // 餐厅详情一次性返回：餐厅信息、收藏数、评论、团购套餐和推荐菜
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow();
        long favoriteCount = favoriteRepository.countByTargetIdAndTargetType(id, FavoriteTargetType.RESTAURANT);
        List<Comment> comments = commentRepository.findByTargetIdAndTargetTypeAndVisibleTrue(id, FavoriteTargetType.RESTAURANT);
        List<GrouponPackage> packages = grouponPackageRepository.findByRestaurantAndEnabledTrueOrderByIdDesc(restaurant);
        List<RecommendedDish> dishes = recommendedDishRepository.findByRestaurantOrderByLikeCountDescIdDesc(restaurant);
        Map<String, Object> map = new HashMap<>();
        map.put("restaurant", restaurant);
        map.put("favoriteCount", favoriteCount);
        map.put("comments", comments);
        map.put("grouponPackages", packages);
        map.put("recommendedDishes", dishes);
        if (authentication != null && authentication.isAuthenticated() && dishes != null && !dishes.isEmpty()) {
            userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
                List<Long> likedDishIds = recommendedDishLikeRepository.findByUserAndDishIn(user, dishes)
                        .stream()
                        .map(like -> like.getDish().getId())
                        .toList();
                map.put("likedDishIds", likedDishIds);
            });
        }
        return ApiResponse.success(map);
    }

    @GetMapping("/{id}/recommended-dishes")
    public ApiResponse<List<RecommendedDish>> listRecommendedDishes(@PathVariable Long id) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow();
        return ApiResponse.success(recommendedDishRepository.findByRestaurantOrderByLikeCountDescIdDesc(restaurant));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/reserve")
    public ApiResponse<DiningReservation> reserve(@PathVariable Long id,
                                                  @RequestBody ReserveRequest req,
                                                  Authentication authentication) {
        // 餐厅订座成功后，给商家发送站内消息提醒其及时确认
        if (req.getPeopleCount() == null || req.getPeopleCount() <= 0) {
            return ApiResponse.error(400, "请填写正确的就餐人数");
        }
        if (req.getReserveTime() == null || req.getReserveTime().isBlank()) {
            return ApiResponse.error(400, "请填写到店时间");
        }
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow();
        if (restaurant.getStatus() != ResourceStatus.ONLINE) {
            return ApiResponse.error(400, "该店铺暂不可预订");
        }
        DiningReservation r = new DiningReservation();
        r.setRestaurant(restaurant);
        r.setUser(user);
        r.setPeopleCount(req.getPeopleCount());
        r.setReserveTime(LocalDateTime.parse(req.getReserveTime()));
        r.setContactName(req.getContactName());
        r.setContactPhone(req.getContactPhone());
        r.setNote(req.getNote());
        r.setStatus(ReservationStatus.PENDING);
        DiningReservation saved = diningReservationRepository.save(r);

        AppMessage msg = new AppMessage();
        msg.setUser(restaurant.getMerchant().getUser());
        msg.setTitle("新的订座提醒");
        msg.setContent("店铺：" + restaurant.getName() +
                "；用户：" + (user.getNickname() != null ? user.getNickname() : user.getUsername()) +
                "；预约了就餐订座，请尽快确认。到店时间：" + saved.getReserveTime() +
                "；人数：" + saved.getPeopleCount() +
                "；联系人：" + saved.getContactName() +
                "；电话：" + saved.getContactPhone() +
                (saved.getNote() != null && !saved.getNote().isBlank() ? "；备注：" + saved.getNote() : ""));
        msg.setCategory("MERCHANT_RESERVE");
        msg.setRelatedMerchantId(restaurant.getMerchant().getId());
        msg.setRelatedTargetId(restaurant.getId());
        msg.setRelatedTargetType("RESTAURANT");
        appMessageRepository.save(msg);

        return ApiResponse.success(saved);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my-reservations")
    public ApiResponse<List<DiningReservation>> myReservations(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return ApiResponse.success(diningReservationRepository.findByUserOrderByCreateTimeDesc(user));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my-groupon-orders")
    public ApiResponse<List<GrouponOrder>> myGrouponOrders(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return ApiResponse.success(grouponOrderRepository.findByUserOrderByCreateTimeDesc(user));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/mine")
    public ApiResponse<List<Restaurant>> mine(Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        return ApiResponse.success(restaurantRepository.findByMerchant(merchant));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PostMapping
    public ApiResponse<Restaurant> create(@RequestBody RestaurantRequest req, Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        if (req.getCityId() == null) return ApiResponse.error(400, "请选择城市");
        City city = cityRepository.findById(req.getCityId()).orElseThrow();
        Restaurant r = new Restaurant();
        r.setMerchant(merchant);
        r.setCity(city);
        r.setName(req.getName());
        r.setDescription(req.getDescription());
        r.setAddress(req.getAddress());
        r.setAvgPrice(req.getAvgPrice());
        r.setOpenHours(req.getOpenHours());
        r.setCoverImage(req.getCoverImage());
        r.setStatus(ResourceStatus.DRAFT);
        return ApiResponse.success(restaurantRepository.save(r));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/{id}")
    public ApiResponse<Restaurant> update(@PathVariable Long id,
                                          @RequestBody RestaurantRequest req,
                                          Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        Restaurant r = restaurantRepository.findById(id).orElseThrow();
        if (!r.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该店铺");
        }
        if (req.getCityId() != null) {
            City city = cityRepository.findById(req.getCityId()).orElseThrow();
            r.setCity(city);
        }
        r.setName(req.getName());
        r.setDescription(req.getDescription());
        r.setAddress(req.getAddress());
        r.setAvgPrice(req.getAvgPrice());
        r.setOpenHours(req.getOpenHours());
        r.setCoverImage(req.getCoverImage());
        return ApiResponse.success(restaurantRepository.save(r));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        Restaurant r = restaurantRepository.findById(id).orElseThrow();
        if (!r.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该店铺");
        }
        restaurantRepository.delete(r);
        return ApiResponse.success(null);
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/{id}/reservations")
    public ApiResponse<List<DiningReservation>> reservations(@PathVariable Long id, Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        Restaurant r = restaurantRepository.findById(id).orElseThrow();
        if (!r.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权查看该店铺预订");
        }
        return ApiResponse.success(diningReservationRepository.findByRestaurantOrderByCreateTimeDesc(r));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/recommended-dishes")
    public ApiResponse<List<RecommendedDish>> myRecommendedDishes(@RequestParam Long restaurantId,
                                                                  Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow();
        if (!restaurant.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权查看该店铺推荐菜");
        }
        return ApiResponse.success(recommendedDishRepository.findByRestaurantOrderByLikeCountDescIdDesc(restaurant));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PostMapping("/recommended-dishes")
    public ApiResponse<RecommendedDish> createRecommendedDish(@RequestBody RecommendedDishRequest req,
                                                              Authentication authentication) {
        // 推荐菜模块用于展示店铺特色菜品，并配合点赞形成用户互动数据
        if (req.getName() == null || req.getName().isBlank()) {
            return ApiResponse.error(400, "请填写推荐菜名称");
        }
        if (req.getRestaurantId() == null) {
            return ApiResponse.error(400, "缺少店铺信息");
        }
        Merchant merchant = ensureCateringMerchant(authentication);
        Restaurant restaurant = restaurantRepository.findById(req.getRestaurantId()).orElseThrow();
        if (!restaurant.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该店铺推荐菜");
        }
        RecommendedDish dish = new RecommendedDish();
        dish.setRestaurant(restaurant);
        dish.setName(req.getName());
        dish.setDescription(req.getDescription());
        dish.setImageUrl(req.getImageUrl());
        dish.setLikeCount(0);
        try {
            return ApiResponse.success(recommendedDishRepository.save(dish));
        } catch (Exception e) {
            return ApiResponse.error(500, "推荐菜保存失败，请重启后端后重试：" + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/recommended-dishes/{id}")
    public ApiResponse<RecommendedDish> updateRecommendedDish(@PathVariable Long id,
                                                              @RequestBody RecommendedDishRequest req,
                                                              Authentication authentication) {
        if (req.getName() == null || req.getName().isBlank()) {
            return ApiResponse.error(400, "请填写推荐菜名称");
        }
        Merchant merchant = ensureCateringMerchant(authentication);
        RecommendedDish dish = recommendedDishRepository.findById(id).orElseThrow();
        if (!dish.getRestaurant().getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该推荐菜");
        }
        dish.setName(req.getName());
        dish.setDescription(req.getDescription());
        dish.setImageUrl(req.getImageUrl());
        try {
            return ApiResponse.success(recommendedDishRepository.save(dish));
        } catch (Exception e) {
            return ApiResponse.error(500, "推荐菜更新失败，请重启后端后重试：" + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @DeleteMapping("/recommended-dishes/{id}")
    public ApiResponse<Void> deleteRecommendedDish(@PathVariable Long id, Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        RecommendedDish dish = recommendedDishRepository.findById(id).orElseThrow();
        if (!dish.getRestaurant().getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该推荐菜");
        }
        recommendedDishLikeRepository.deleteByDish(dish);
        recommendedDishRepository.delete(dish);
        return ApiResponse.success(null);
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/groupon-packages")
    public ApiResponse<List<GrouponPackage>> myGrouponPackages(@RequestParam Long restaurantId, Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow();
        if (!restaurant.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权查看该店铺团购");
        }
        return ApiResponse.success(grouponPackageRepository.findByRestaurantOrderByIdDesc(restaurant));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PostMapping("/groupon-packages")
    public ApiResponse<GrouponPackage> createGrouponPackage(@RequestBody GrouponPackageRequest req, Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        Restaurant restaurant = restaurantRepository.findById(req.getRestaurantId()).orElseThrow();
        if (!restaurant.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该店铺团购");
        }
        GrouponPackage pkg = new GrouponPackage();
        pkg.setRestaurant(restaurant);
        pkg.setTitle(req.getTitle());
        pkg.setDescription(req.getDescription());
        pkg.setOriginalPrice(req.getOriginalPrice());
        pkg.setGroupPrice(req.getGroupPrice());
        pkg.setImageUrl(req.getImageUrl());
        pkg.setStock(req.getStock() == null ? 0 : req.getStock());
        pkg.setEnabled(req.getEnabled() == null || req.getEnabled());
        return ApiResponse.success(grouponPackageRepository.save(pkg));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/groupon-packages/{id}")
    public ApiResponse<GrouponPackage> updateGrouponPackage(@PathVariable Long id,
                                                            @RequestBody GrouponPackageRequest req,
                                                            Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        GrouponPackage pkg = grouponPackageRepository.findById(id).orElseThrow();
        if (!pkg.getRestaurant().getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该团购套餐");
        }
        pkg.setTitle(req.getTitle());
        pkg.setDescription(req.getDescription());
        pkg.setOriginalPrice(req.getOriginalPrice());
        pkg.setGroupPrice(req.getGroupPrice());
        pkg.setImageUrl(req.getImageUrl());
        if (req.getStock() != null) pkg.setStock(req.getStock());
        if (req.getEnabled() != null) pkg.setEnabled(req.getEnabled());
        return ApiResponse.success(grouponPackageRepository.save(pkg));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @DeleteMapping("/groupon-packages/{id}")
    public ApiResponse<Void> deleteGrouponPackage(@PathVariable Long id, Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        GrouponPackage pkg = grouponPackageRepository.findById(id).orElseThrow();
        if (!pkg.getRestaurant().getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该团购套餐");
        }
        grouponPackageRepository.delete(pkg);
        return ApiResponse.success(null);
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/groupon-orders")
    public ApiResponse<List<GrouponOrder>> myGrouponOrdersByRestaurant(@RequestParam Long restaurantId,
                                                                        Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow();
        if (!restaurant.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权查看该店铺团购订单");
        }
        return ApiResponse.success(grouponOrderRepository.findByGrouponPackageRestaurantOrderByCreateTimeDesc(restaurant));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/groupon-packages/{id}/order")
    public ApiResponse<GrouponOrder> createGrouponOrder(@PathVariable Long id,
                                                        @RequestBody GrouponOrderRequest req,
                                                        Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        GrouponPackage pkg = grouponPackageRepository.findById(id).orElseThrow();
        if (!Boolean.TRUE.equals(pkg.getEnabled()) || pkg.getRestaurant().getStatus() != ResourceStatus.ONLINE) {
            return ApiResponse.error(400, "该团购套餐当前不可下单");
        }
        int qty = req.getQuantity() == null || req.getQuantity() <= 0 ? 1 : req.getQuantity();
        if (pkg.getStock() != null && pkg.getStock() < qty) {
            return ApiResponse.error(400, "库存不足");
        }
        GrouponOrder order = new GrouponOrder();
        order.setGrouponPackage(pkg);
        order.setUser(user);
        order.setQuantity(qty);
        order.setTotalPrice(pkg.getGroupPrice().multiply(BigDecimal.valueOf(qty)));
        order.setStatus(ReservationStatus.UNUSED);
        order.setVerifyCode(String.valueOf(100000 + random.nextInt(900000)));
        if (pkg.getStock() != null) {
            pkg.setStock(pkg.getStock() - qty);
            grouponPackageRepository.save(pkg);
        }
        GrouponOrder saved = grouponOrderRepository.save(order);

        AppMessage msg = new AppMessage();
        msg.setUser(pkg.getRestaurant().getMerchant().getUser());
        msg.setTitle("新的餐饮团购订单");
        msg.setContent("店铺：" + pkg.getRestaurant().getName() +
                "；套餐：" + pkg.getTitle() +
                "；用户：" + (user.getNickname() != null ? user.getNickname() : user.getUsername()) +
                " 提交了餐饮团购订单，请及时处理。数量：" + qty +
                "；总价：¥" + order.getTotalPrice() +
                "；核销码：" + order.getVerifyCode());
        msg.setCategory("MERCHANT_ORDER");
        msg.setRelatedMerchantId(pkg.getRestaurant().getMerchant().getId());
        msg.setRelatedOrderId(saved.getId());
        appMessageRepository.save(msg);

        return ApiResponse.success(saved);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/recommended-dishes/{id}/like")
    public ApiResponse<Map<String, Object>> toggleDishLike(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        RecommendedDish dish = recommendedDishRepository.findById(id).orElseThrow();
        boolean liked = recommendedDishLikeRepository.findByDishAndUser(dish, user)
                .map(existing -> {
                    recommendedDishLikeRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    RecommendedDishLike like = new RecommendedDishLike();
                    like.setDish(dish);
                    like.setUser(user);
                    recommendedDishLikeRepository.save(like);
                    return true;
                });
        int count = (int) recommendedDishLikeRepository.countByDish(dish);
        dish.setLikeCount(count);
        recommendedDishRepository.save(dish);
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", count);
        return ApiResponse.success(result);
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/groupon-orders/{id}/verify")
    public ApiResponse<GrouponOrder> verifyGrouponOrder(@PathVariable Long id,
                                                        @RequestParam String code,
                                                        Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        GrouponOrder order = grouponOrderRepository.findById(id).orElseThrow();
        if (!order.getGrouponPackage().getRestaurant().getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权核销该团购订单");
        }
        if (order.getStatus() != ReservationStatus.UNUSED) {
            return ApiResponse.error(400, "当前订单状态不可核销");
        }
        if (order.getVerifyCode() == null || !order.getVerifyCode().equals(code)) {
            return ApiResponse.error(400, "核销码错误");
        }
        order.setStatus(ReservationStatus.USED);
        GrouponOrder saved = grouponOrderRepository.save(order);

        AppMessage msg = new AppMessage();
        msg.setUser(order.getUser());
        msg.setTitle("团购套餐核销成功");
        msg.setContent("店铺：" + order.getGrouponPackage().getRestaurant().getName() +
                "；套餐：" + order.getGrouponPackage().getTitle() +
                " 已核销成功，核销码：" + order.getVerifyCode() + "。请及时查看订单状态。");
        msg.setCategory("USER_ORDER");
        msg.setRelatedMerchantId(merchant.getId());
        appMessageRepository.save(msg);

        return ApiResponse.success(saved);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/my-groupon-orders/{id}/refund")
    public ApiResponse<GrouponOrder> refundMyGrouponOrder(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        GrouponOrder order = grouponOrderRepository.findById(id).orElseThrow();
        if (!order.getUser().getId().equals(user.getId())) {
            return ApiResponse.error(403, "无权操作该订单");
        }
        if (order.getStatus() != ReservationStatus.UNUSED) {
            return ApiResponse.error(400, "仅待使用订单可退款");
        }
        order.setStatus(ReservationStatus.REFUNDED);
        GrouponPackage pkg = order.getGrouponPackage();
        if (pkg.getStock() != null) {
            pkg.setStock(pkg.getStock() + (order.getQuantity() == null ? 0 : order.getQuantity()));
            grouponPackageRepository.save(pkg);
        }
        return ApiResponse.success(grouponOrderRepository.save(order));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/reservations/{id}/status")
    public ApiResponse<DiningReservation> changeReservationStatus(@PathVariable Long id,
                                                                  @RequestParam ReservationStatus status,
                                                                  Authentication authentication) {
        Merchant merchant = ensureCateringMerchant(authentication);
        DiningReservation reservation = diningReservationRepository.findById(id).orElseThrow();
        if (!reservation.getRestaurant().getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该预订");
        }
        reservation.setStatus(status);
        DiningReservation saved = diningReservationRepository.save(reservation);
        AppMessage msg = new AppMessage();
        msg.setUser(reservation.getUser());
        msg.setTitle("你的餐饮预订已更新");
        msg.setContent("商家已将你的餐饮预订状态更新为：" + status + "。");
        msg.setCategory("USER_ORDER");
        msg.setRelatedMerchantId(merchant.getId());
        appMessageRepository.save(msg);
        return ApiResponse.success(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ApiResponse<List<Restaurant>> adminAll() {
        return ApiResponse.success(restaurantRepository.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{id}")
    public ApiResponse<Map<String, Object>> adminDetail(@PathVariable Long id) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow();
        Map<String, Object> map = new HashMap<>();
        map.put("restaurant", restaurant);
        map.put("merchant", restaurant.getMerchant());
        map.put("city", restaurant.getCity());
        return ApiResponse.success(map);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}/status")
    public ApiResponse<Restaurant> adminChangeStatus(@PathVariable Long id,
                                                     @RequestParam ResourceStatus status) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow();
        restaurant.setStatus(status);
        return ApiResponse.success(restaurantRepository.save(restaurant));
    }

    private Merchant ensureCateringMerchant(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        if (merchant.getType() != MerchantType.CATERING) {
            throw new RuntimeException("当前商家不是餐饮商家，无法管理餐饮店铺");
        }
        return merchant;
    }
}

