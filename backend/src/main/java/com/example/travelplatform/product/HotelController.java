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
import com.example.travelplatform.product.dto.HotelRequest;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin
public class HotelController {

    private final HotelRepository hotelRepository;
    private final CityRepository cityRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;

    public HotelController(HotelRepository hotelRepository,
                           CityRepository cityRepository,
                           MerchantRepository merchantRepository,
                           UserRepository userRepository,
                           FavoriteRepository favoriteRepository,
                           CommentRepository commentRepository) {
        this.hotelRepository = hotelRepository;
        this.cityRepository = cityRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/by-city/{cityId}")
    public ApiResponse<List<Hotel>> listByCity(@PathVariable Long cityId) {
        City city = cityRepository.findById(cityId).orElseThrow();
        return ApiResponse.success(
                hotelRepository.findByCityAndStatus(city, ResourceStatus.ONLINE));
    }

    // 用户端：随机推荐酒店（未选城市则全局随机，选城市则在该城市内随机）
    @GetMapping("/recommend")
    public ApiResponse<List<Hotel>> recommend(@RequestParam(required = false) Long cityId) {
        List<Hotel> source;
        if (cityId != null) {
            City city = cityRepository.findById(cityId).orElseThrow();
            source = hotelRepository.findByCityAndStatus(city, ResourceStatus.ONLINE);
        } else {
            source = hotelRepository.findByStatus(ResourceStatus.ONLINE);
        }
        Collections.shuffle(source);
        int limit = Math.min(source.size(), 8);
        return ApiResponse.success(source.subList(0, limit));
    }

    @GetMapping("/search")
    public ApiResponse<List<Hotel>> search(@RequestParam String keyword) {
        return ApiResponse.success(
                hotelRepository.findByNameContainingIgnoreCaseAndStatus(
                        keyword, ResourceStatus.ONLINE));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        // 酒店详情一次性返回：酒店基础信息、收藏数和评论列表，方便前端统一渲染
        Hotel hotel = hotelRepository.findById(id).orElseThrow();
        long favoriteCount = favoriteRepository.countByTargetIdAndTargetType(
                id, FavoriteTargetType.HOTEL);
        List<Comment> comments = commentRepository.findByTargetIdAndTargetTypeAndVisibleTrue(
                id, FavoriteTargetType.HOTEL);

        Map<String, Object> result = new HashMap<>();
        result.put("hotel", hotel);
        result.put("favoriteCount", favoriteCount);
        result.put("comments", comments);
        return ApiResponse.success(result);
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/mine")
    public ApiResponse<List<Hotel>> myHotels(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        return ApiResponse.success(hotelRepository.findByMerchant(merchant));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PostMapping
    public ApiResponse<Hotel> create(@RequestBody HotelRequest request,
                                     Authentication authentication) {
        // 商家新增酒店时默认保存为草稿，等待管理员审核后再上线
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        City city = cityRepository.findById(request.getCityId()).orElseThrow();

        Hotel hotel = new Hotel();
        hotel.setMerchant(merchant);
        hotel.setCity(city);
        hotel.setName(request.getName());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());
        hotel.setPrice(request.getPrice());
        hotel.setCoverImage(request.getCoverImage());
        // 新增默认为草稿，需管理员审核后才能上线
        hotel.setStatus(ResourceStatus.DRAFT);
        return ApiResponse.success(hotelRepository.save(hotel));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/{id}")
    public ApiResponse<Hotel> update(@PathVariable Long id,
                                     @RequestBody HotelRequest request,
                                     Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();

        Hotel hotel = hotelRepository.findById(id).orElseThrow();
        if (!hotel.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("无权操作该酒店");
        }
        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId()).orElseThrow();
            hotel.setCity(city);
        }
        hotel.setName(request.getName());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());
        hotel.setPrice(request.getPrice());
        hotel.setCoverImage(request.getCoverImage());
        return ApiResponse.success(hotelRepository.save(hotel));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/{id}/status")
    public ApiResponse<Hotel> changeStatus(@PathVariable Long id,
                                           @RequestParam ResourceStatus status,
                                           Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();

        Hotel hotel = hotelRepository.findById(id).orElseThrow();
        if (!hotel.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("无权操作该酒店");
        }
        hotel.setStatus(status);
        return ApiResponse.success(hotelRepository.save(hotel));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        Hotel hotel = hotelRepository.findById(id).orElseThrow();
        if (!hotel.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("无权操作该酒店");
        }
        hotelRepository.delete(hotel);
        return ApiResponse.success(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ApiResponse<List<Hotel>> adminAll() {
        return ApiResponse.success(hotelRepository.findAll());
    }

    // 管理员审核/调整酒店状态
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{id}/status")
    public ApiResponse<Hotel> adminChangeStatus(@PathVariable Long id,
                                                @RequestParam ResourceStatus status) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow();
        hotel.setStatus(status);
        return ApiResponse.success(hotelRepository.save(hotel));
    }
}

