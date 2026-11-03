package com.example.travelplatform.product;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.interaction.Comment;
import com.example.travelplatform.interaction.CommentRepository;
import com.example.travelplatform.interaction.FavoriteRepository;
import com.example.travelplatform.interaction.FavoriteTargetType;
import com.example.travelplatform.location.City;
import com.example.travelplatform.location.CityRepository;
import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.merchant.MerchantRepository;
import com.example.travelplatform.product.dto.ScenicSpotRequest;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import com.example.travelplatform.user.UserRole;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scenics")
@CrossOrigin
public class ScenicSpotController {

    private final ScenicSpotRepository scenicSpotRepository;
    private final CityRepository cityRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;

    public ScenicSpotController(ScenicSpotRepository scenicSpotRepository,
                                CityRepository cityRepository,
                                MerchantRepository merchantRepository,
                                UserRepository userRepository,
                                FavoriteRepository favoriteRepository,
                                CommentRepository commentRepository) {
        this.scenicSpotRepository = scenicSpotRepository;
        this.cityRepository = cityRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
    }

    // 用户端：按城市浏览
    @GetMapping("/by-city/{cityId}")
    public ApiResponse<List<ScenicSpot>> listByCity(@PathVariable Long cityId) {
        City city = cityRepository.findById(cityId).orElseThrow();
        return ApiResponse.success(
                scenicSpotRepository.findByCityAndStatus(city, ResourceStatus.ONLINE));
    }

    // 用户端：随机推荐景点（未选城市则全局随机，选城市则在该城市内随机）
    @GetMapping("/recommend")
    public ApiResponse<List<ScenicSpot>> recommend(@RequestParam(required = false) Long cityId) {
        List<ScenicSpot> source;
        if (cityId != null) {
            City city = cityRepository.findById(cityId).orElseThrow();
            source = scenicSpotRepository.findByCityAndStatus(city, ResourceStatus.ONLINE);
        } else {
            source = scenicSpotRepository.findByStatus(ResourceStatus.ONLINE);
        }
        Collections.shuffle(source);
        int limit = Math.min(source.size(), 8);
        return ApiResponse.success(source.subList(0, limit));
    }

    // 用户端：搜索
    @GetMapping("/search")
    public ApiResponse<List<ScenicSpot>> search(@RequestParam String keyword) {
        return ApiResponse.success(
                scenicSpotRepository.findByNameContainingIgnoreCaseAndStatus(
                        keyword, ResourceStatus.ONLINE));
    }

    // 用户端：详情（包含收藏数、评论）
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        // 景点详情一次性返回：基础信息、收藏数和评论列表，便于前端统一展示
        ScenicSpot spot = scenicSpotRepository.findById(id).orElseThrow();
        long favoriteCount = favoriteRepository.countByTargetIdAndTargetType(
                id, FavoriteTargetType.SCENIC);
        List<Comment> comments = commentRepository.findByTargetIdAndTargetTypeAndVisibleTrue(
                id, FavoriteTargetType.SCENIC);

        Map<String, Object> result = new HashMap<>();
        result.put("spot", spot);
        result.put("favoriteCount", favoriteCount);
        result.put("comments", comments);
        return ApiResponse.success(result);
    }

    // 商家端：查看自己发布的景点列表
    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/mine")
    public ApiResponse<List<ScenicSpot>> myScenics(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        return ApiResponse.success(scenicSpotRepository.findByMerchant(merchant));
    }

    // 商家端：新增景点
    @PreAuthorize("hasRole('MERCHANT')")
    @PostMapping
    public ApiResponse<ScenicSpot> create(@RequestBody ScenicSpotRequest request,
                                          Authentication authentication) {
        // 商家新增景点默认进入草稿状态，保证内容先审核后发布
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        City city = cityRepository.findById(request.getCityId()).orElseThrow();

        ScenicSpot spot = new ScenicSpot();
        spot.setMerchant(merchant);
        spot.setCity(city);
        spot.setName(request.getName());
        spot.setDescription(request.getDescription());
        spot.setAddress(request.getAddress());
        spot.setFree(request.getFree());
        spot.setChildPrice(request.getChildPrice());
        spot.setAdultPrice(request.getAdultPrice());
        spot.setStudentPrice(request.getStudentPrice());
        // 参考价格兼容旧逻辑
        spot.setPrice(resolveReferencePrice(spot, request.getPrice()));
        spot.setCoverImage(request.getCoverImage());
        // 新增默认为草稿，需管理员审核后才能上线
        spot.setStatus(ResourceStatus.DRAFT);
        return ApiResponse.success(scenicSpotRepository.save(spot));
    }

    // 商家端：修改景点
    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/{id}")
    public ApiResponse<ScenicSpot> update(@PathVariable Long id,
                                          @RequestBody ScenicSpotRequest request,
                                          Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();

        ScenicSpot spot = scenicSpotRepository.findById(id).orElseThrow();
        if (!spot.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("无权操作该景点");
        }
        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId()).orElseThrow();
            spot.setCity(city);
        }
        spot.setName(request.getName());
        spot.setDescription(request.getDescription());
        spot.setAddress(request.getAddress());
        spot.setFree(request.getFree());
        spot.setChildPrice(request.getChildPrice());
        spot.setAdultPrice(request.getAdultPrice());
        spot.setStudentPrice(request.getStudentPrice());
        spot.setPrice(resolveReferencePrice(spot, request.getPrice()));
        spot.setCoverImage(request.getCoverImage());
        return ApiResponse.success(scenicSpotRepository.save(spot));
    }

    // 商家端：下架/上架/删除
    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/{id}/status")
    public ApiResponse<ScenicSpot> changeStatus(@PathVariable Long id,
                                                @RequestParam ResourceStatus status,
                                                Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();

        ScenicSpot spot = scenicSpotRepository.findById(id).orElseThrow();
        if (!spot.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("无权操作该景点");
        }
        spot.setStatus(status);
        return ApiResponse.success(scenicSpotRepository.save(spot));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        ScenicSpot spot = scenicSpotRepository.findById(id).orElseThrow();
        if (!spot.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("无权操作该景点");
        }
        scenicSpotRepository.delete(spot);
        return ApiResponse.success(null);
    }

    // 管理员：查看所有景点（用于违规处理）
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ApiResponse<List<ScenicSpot>> adminAll() {
        return ApiResponse.success(scenicSpotRepository.findAll());
    }

    // 管理员审核/调整景点状态
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}/status")
    public ApiResponse<ScenicSpot> adminChangeStatus(@PathVariable Long id,
                                                     @RequestParam ResourceStatus status) {
        ScenicSpot spot = scenicSpotRepository.findById(id).orElseThrow();
        spot.setStatus(status);
        return ApiResponse.success(scenicSpotRepository.save(spot));
    }

    /**
     * 计算景点的参考价格：
     * - 如果免费，返回 0
     * - 否则优先使用成人票价，其次三类票价中的最小值，最后退回请求中传入的 price
     */
    private java.math.BigDecimal resolveReferencePrice(ScenicSpot spot, java.math.BigDecimal requestPrice) {
        if (Boolean.TRUE.equals(spot.getFree())) {
            return java.math.BigDecimal.ZERO;
        }
        java.math.BigDecimal ref = spot.getAdultPrice();
        if (ref != null) {
            return ref;
        }
        java.math.BigDecimal min = null;
        for (java.math.BigDecimal p : new java.math.BigDecimal[]{spot.getChildPrice(), spot.getStudentPrice(), requestPrice}) {
            if (p == null) continue;
            if (min == null || p.compareTo(min) < 0) {
                min = p;
            }
        }
        return min != null ? min : java.math.BigDecimal.ZERO;
    }
}

