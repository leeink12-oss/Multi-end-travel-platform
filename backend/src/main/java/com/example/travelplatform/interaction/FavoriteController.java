package com.example.travelplatform.interaction;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.product.Hotel;
import com.example.travelplatform.product.HotelRepository;
import com.example.travelplatform.restaurant.Restaurant;
import com.example.travelplatform.restaurant.RestaurantRepository;
import com.example.travelplatform.transport.Flight;
import com.example.travelplatform.transport.FlightRepository;
import com.example.travelplatform.transport.Train;
import com.example.travelplatform.transport.TrainRepository;
import com.example.travelplatform.product.ScenicSpot;
import com.example.travelplatform.product.ScenicSpotRepository;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final ScenicSpotRepository scenicSpotRepository;
    private final RestaurantRepository restaurantRepository;
    private final FlightRepository flightRepository;
    private final TrainRepository trainRepository;

    public FavoriteController(FavoriteRepository favoriteRepository,
                              UserRepository userRepository,
                              HotelRepository hotelRepository,
                              ScenicSpotRepository scenicSpotRepository,
                              RestaurantRepository restaurantRepository,
                              FlightRepository flightRepository,
                              TrainRepository trainRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.scenicSpotRepository = scenicSpotRepository;
        this.restaurantRepository = restaurantRepository;
        this.flightRepository = flightRepository;
        this.trainRepository = trainRepository;
    }

    @PreAuthorize("hasAnyRole('USER','MERCHANT')")
    @PostMapping("/toggle")
    public ApiResponse<Boolean> toggle(@RequestParam Long targetId,
                                       @RequestParam FavoriteTargetType type,
                                       Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return favoriteRepository.findByUserAndTargetIdAndTargetType(user, targetId, type)
                .map(f -> {
                    favoriteRepository.delete(f);
                    return ApiResponse.success(false);
                })
                .orElseGet(() -> {
                    Favorite favorite = new Favorite();
                    favorite.setUser(user);
                    favorite.setTargetId(targetId);
                    favorite.setTargetType(type);
                    favoriteRepository.save(favorite);
                    return ApiResponse.success(true);
                });
    }

    @PreAuthorize("hasAnyRole('USER','MERCHANT')")
    @GetMapping("/my")
    public ApiResponse<List<Favorite>> myFavorites(@RequestParam FavoriteTargetType type,
                                                   Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        return ApiResponse.success(favoriteRepository.findByUserAndTargetType(user, type));
    }

    /** 我的收藏（带详情），用于「我的收藏」页展示 */
    @PreAuthorize("hasAnyRole('USER','MERCHANT')")
    @GetMapping("/my-with-detail")
    public ApiResponse<List<FavoriteWithDetailVO>> myFavoritesWithDetail(@RequestParam FavoriteTargetType type,
                                                                         Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<Favorite> list = favoriteRepository.findByUserAndTargetType(user, type);
        List<FavoriteWithDetailVO> result = new ArrayList<>();
        for (Favorite f : list) {
            FavoriteWithDetailVO vo = new FavoriteWithDetailVO(f.getId(), f.getTargetId(), f.getTargetType(), f.getCreateTime());
            switch (type) {
                case HOTEL -> hotelRepository.findById(f.getTargetId()).ifPresent(h -> {
                    vo.setName(h.getName());
                    vo.setDescription(h.getDescription());
                    vo.setAddress(h.getAddress());
                    vo.setCoverImage(h.getCoverImage());
                    vo.setPrice(h.getPrice());
                    vo.setCityName(h.getCity() != null ? h.getCity().getName() : null);
                });
                case SCENIC -> scenicSpotRepository.findById(f.getTargetId()).ifPresent(s -> {
                    vo.setName(s.getName());
                    vo.setDescription(s.getDescription());
                    vo.setAddress(s.getAddress());
                    vo.setCoverImage(s.getCoverImage());
                    vo.setPrice(s.getPrice());
                    vo.setCityName(s.getCity() != null ? s.getCity().getName() : null);
                });
                case RESTAURANT -> restaurantRepository.findById(f.getTargetId()).ifPresent(r -> {
                    vo.setName(r.getName());
                    vo.setRestaurantName(r.getName());
                    vo.setDescription(r.getDescription());
                    vo.setAddress(r.getAddress());
                    vo.setCoverImage(r.getCoverImage());
                    vo.setPrice(r.getAvgPrice());
                    vo.setCityName(r.getCity() != null ? r.getCity().getName() : null);
                });
                case FLIGHT -> flightRepository.findById(f.getTargetId()).ifPresent(fl -> {
                    vo.setName(fl.getFlightNo());
                    vo.setDescription(fl.getFromCity() + " → " + fl.getToCity());
                    vo.setCoverImage(fl.getCoverImage());
                    vo.setPrice(fl.getPrice());
                });
                case TRAIN -> trainRepository.findById(f.getTargetId()).ifPresent(tr -> {
                    vo.setName(tr.getTrainNo());
                    vo.setDescription(tr.getFromCity() + " → " + tr.getToCity());
                    vo.setCoverImage(tr.getCoverImage());
                    vo.setPrice(tr.getPrice());
                });
                default -> {
                }
            }
            result.add(vo);
        }
        return ApiResponse.success(result);
    }

    @PreAuthorize("hasAnyRole('USER','MERCHANT')")
    @GetMapping("/my-restaurants")
    public ApiResponse<List<FavoriteWithDetailVO>> myRestaurantFavorites(Authentication authentication) {
        return myFavoritesWithDetail(FavoriteTargetType.RESTAURANT, authentication);
    }

    @lombok.Data
    public static class FavoriteWithDetailVO {
        private Long id;
        private Long targetId;
        private FavoriteTargetType targetType;
        private LocalDateTime createTime;
        private String name;
        private String description;
        private String address;
        private String coverImage;
        private BigDecimal price;
        private String cityName;
        private String restaurantName;

        public FavoriteWithDetailVO(Long id, Long targetId, FavoriteTargetType targetType, LocalDateTime createTime) {
            this.id = id;
            this.targetId = targetId;
            this.targetType = targetType;
            this.createTime = createTime;
        }
    }
}

