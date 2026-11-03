package com.example.travelplatform.recommendation;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.interaction.Comment;
import com.example.travelplatform.interaction.CommentRepository;
import com.example.travelplatform.interaction.Favorite;
import com.example.travelplatform.interaction.FavoriteRepository;
import com.example.travelplatform.interaction.FavoriteTargetType;
import com.example.travelplatform.order.Order;
import com.example.travelplatform.order.OrderRepository;
import com.example.travelplatform.product.Hotel;
import com.example.travelplatform.product.HotelRepository;
import com.example.travelplatform.product.ResourceStatus;
import com.example.travelplatform.product.ScenicSpot;
import com.example.travelplatform.product.ScenicSpotRepository;
import com.example.travelplatform.restaurant.Restaurant;
import com.example.travelplatform.restaurant.RestaurantRepository;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin
public class RecommendationController {

    private static final double FAVORITE_WEIGHT = 3.0;
    private static final double COMMENT_WEIGHT = 4.0;
    private static final double ORDER_WEIGHT = 5.0;
    private static final double TOP_N_SIMILAR_USERS = 5.0;

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final OrderRepository orderRepository;
    private final ScenicSpotRepository scenicSpotRepository;
    private final HotelRepository hotelRepository;
    private final RestaurantRepository restaurantRepository;

    public RecommendationController(UserRepository userRepository,
                                    FavoriteRepository favoriteRepository,
                                    CommentRepository commentRepository,
                                    OrderRepository orderRepository,
                                    ScenicSpotRepository scenicSpotRepository,
                                    HotelRepository hotelRepository,
                                    RestaurantRepository restaurantRepository) {
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
        this.orderRepository = orderRepository;
        this.scenicSpotRepository = scenicSpotRepository;
        this.hotelRepository = hotelRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping("/home")
    public ApiResponse<HomeRecommendationVO> home(Authentication authentication) {
        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        }
        HomeRecommendationVO vo = buildHomeRecommendations(currentUser);
        return ApiResponse.success(vo);
    }

    private HomeRecommendationVO buildHomeRecommendations(User currentUser) {
        Map<Long, Map<ResourceKey, Double>> userProfiles = buildUserProfiles();
        Map<ResourceKey, Double> currentProfile = currentUser == null
                ? Map.of()
                : userProfiles.getOrDefault(currentUser.getId(), Map.of());

        List<ScenicSpot> scenicRecommendations = recommendForType(
                FavoriteTargetType.SCENIC,
                scenicSpotRepository.findByStatus(ResourceStatus.ONLINE),
                currentProfile,
                userProfiles,
                currentUser);
        List<Hotel> hotelRecommendations = recommendForType(
                FavoriteTargetType.HOTEL,
                hotelRepository.findByStatus(ResourceStatus.ONLINE),
                currentProfile,
                userProfiles,
                currentUser);
        List<Restaurant> restaurantRecommendations = recommendForType(
                FavoriteTargetType.RESTAURANT,
                restaurantRepository.findByStatus(ResourceStatus.ONLINE),
                currentProfile,
                userProfiles,
                currentUser);

        HomeRecommendationVO vo = new HomeRecommendationVO();
        vo.setScenics(scenicRecommendations);
        vo.setHotels(hotelRecommendations);
        vo.setRestaurants(restaurantRecommendations);
        return vo;
    }

    private Map<Long, Map<ResourceKey, Double>> buildUserProfiles() {
        Map<Long, Map<ResourceKey, Double>> profiles = new HashMap<>();
        for (User user : userRepository.findAll()) {
            Map<ResourceKey, Double> profile = new HashMap<>();

            for (Favorite favorite : favoriteRepository.findByUserAndTargetType(user, FavoriteTargetType.SCENIC)) {
                addWeight(profile, ResourceKey.scenic(favorite.getTargetId()), FAVORITE_WEIGHT);
            }
            for (Favorite favorite : favoriteRepository.findByUserAndTargetType(user, FavoriteTargetType.HOTEL)) {
                addWeight(profile, ResourceKey.hotel(favorite.getTargetId()), FAVORITE_WEIGHT);
            }
            for (Favorite favorite : favoriteRepository.findByUserAndTargetType(user, FavoriteTargetType.RESTAURANT)) {
                addWeight(profile, ResourceKey.restaurant(favorite.getTargetId()), FAVORITE_WEIGHT);
            }

            for (Comment comment : commentRepository.findByUserOrderByCreateTimeDesc(user)) {
                addWeight(profile, ResourceKey.of(comment.getTargetType(), comment.getTargetId()), COMMENT_WEIGHT);
            }

            for (Order order : orderRepository.findByUser(user)) {
                addWeight(profile, ResourceKey.of(order.getTargetType(), order.getTargetId()), ORDER_WEIGHT);
            }

            if (!profile.isEmpty()) {
                profiles.put(user.getId(), profile);
            }
        }
        return profiles;
    }

    private <T> List<T> recommendForType(FavoriteTargetType type,
                                         List<T> allItems,
                                         Map<ResourceKey, Double> currentProfile,
                                         Map<Long, Map<ResourceKey, Double>> userProfiles,
                                         User currentUser) {
        if (allItems == null || allItems.isEmpty()) {
            return List.of();
        }

        Set<Long> interactedIds = new LinkedHashSet<>();
        if (currentUser != null) {
            currentProfile.forEach((key, score) -> {
                if (key.type() == type && score != null && score > 0) {
                    interactedIds.add(key.id());
                }
            });
        }

        Map<Long, Double> scoreMap = new HashMap<>();
        for (T item : allItems) {
            ResourceKey candidateKey = keyOf(type, item);
            if (candidateKey == null) continue;
            if (interactedIds.contains(candidateKey.id())) continue;

            double score = 0.0;
            for (Long seedId : interactedIds) {
                ResourceKey seedKey = new ResourceKey(type, seedId);
                double sim = similarity(candidateKey, seedKey, userProfiles);
                if (sim > 0) {
                    score += sim * currentProfile.getOrDefault(seedKey, 1.0);
                }
            }
            if (score > 0) {
                scoreMap.put(candidateKey.id(), score);
            }
        }

        List<T> sorted = new ArrayList<>(allItems);
        Comparator<T> comparator = Comparator.comparingDouble((T item) -> {
            ResourceKey key = keyOf(type, item);
            return key == null ? 0.0 : scoreMap.getOrDefault(key.id(), popularityScore(type, key, userProfiles));
        }).reversed();

        sorted.sort(comparator.thenComparing((T item) -> {
            ResourceKey key = keyOf(type, item);
            return key == null ? 0.0 : popularityScore(type, key, userProfiles);
        }, Comparator.reverseOrder()));

        return sorted.stream().limit(8).collect(Collectors.toList());
    }

    private double similarity(ResourceKey a, ResourceKey b, Map<Long, Map<ResourceKey, Double>> userProfiles) {
        if (a == null || b == null || a.type() != b.type()) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (Map<ResourceKey, Double> profile : userProfiles.values()) {
            double va = profile.getOrDefault(a, 0.0);
            double vb = profile.getOrDefault(b, 0.0);
            dot += va * vb;
            normA += va * va;
            normB += vb * vb;
        }
        if (dot <= 0 || normA <= 0 || normB <= 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double popularityScore(FavoriteTargetType type, ResourceKey key, Map<Long, Map<ResourceKey, Double>> userProfiles) {
        double score = 0.0;
        for (Map<ResourceKey, Double> profile : userProfiles.values()) {
            score += profile.getOrDefault(key, 0.0);
        }
        return score;
    }

    private void addWeight(Map<ResourceKey, Double> profile, ResourceKey key, double weight) {
        profile.merge(key, weight, Double::sum);
    }

    private ResourceKey keyOf(FavoriteTargetType type, Object item) {
        if (type == FavoriteTargetType.SCENIC && item instanceof ScenicSpot scenicSpot) {
            return ResourceKey.scenic(scenicSpot.getId());
        }
        if (type == FavoriteTargetType.HOTEL && item instanceof Hotel hotel) {
            return ResourceKey.hotel(hotel.getId());
        }
        if (type == FavoriteTargetType.RESTAURANT && item instanceof Restaurant restaurant) {
            return ResourceKey.restaurant(restaurant.getId());
        }
        return null;
    }

    public record ResourceKey(FavoriteTargetType type, Long id) {
        public static ResourceKey scenic(Long id) { return new ResourceKey(FavoriteTargetType.SCENIC, id); }
        public static ResourceKey hotel(Long id) { return new ResourceKey(FavoriteTargetType.HOTEL, id); }
        public static ResourceKey restaurant(Long id) { return new ResourceKey(FavoriteTargetType.RESTAURANT, id); }
        public static ResourceKey of(FavoriteTargetType type, Long id) { return new ResourceKey(type, id); }
    }

    public static class HomeRecommendationVO {
        private List<ScenicSpot> scenics = List.of();
        private List<Hotel> hotels = List.of();
        private List<Restaurant> restaurants = List.of();

        public List<ScenicSpot> getScenics() { return scenics; }
        public void setScenics(List<ScenicSpot> scenics) { this.scenics = scenics; }
        public List<Hotel> getHotels() { return hotels; }
        public void setHotels(List<Hotel> hotels) { this.hotels = hotels; }
        public List<Restaurant> getRestaurants() { return restaurants; }
        public void setRestaurants(List<Restaurant> restaurants) { this.restaurants = restaurants; }
    }
}
