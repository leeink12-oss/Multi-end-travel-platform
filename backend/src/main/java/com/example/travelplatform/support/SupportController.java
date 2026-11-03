package com.example.travelplatform.support;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.product.Hotel;
import com.example.travelplatform.product.HotelRepository;
import com.example.travelplatform.restaurant.Restaurant;
import com.example.travelplatform.restaurant.RestaurantRepository;
import com.example.travelplatform.product.ScenicSpot;
import com.example.travelplatform.product.ScenicSpotRepository;
import com.example.travelplatform.product.ResourceStatus;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@CrossOrigin
public class SupportController {

    private final ScenicSpotRepository scenicSpotRepository;
    private final HotelRepository hotelRepository;
    private final RestaurantRepository restaurantRepository;

    @Value("${spark.api-password:}")
    private String sparkApiPassword;

    private static final String SPARK_ENDPOINT = "https://spark-api-open.xf-yun.com/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();

    public SupportController(ScenicSpotRepository scenicSpotRepository,
                             HotelRepository hotelRepository,
                             RestaurantRepository restaurantRepository) {
        this.scenicSpotRepository = scenicSpotRepository;
        this.hotelRepository = hotelRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Data
    public static class SupportRequest {
        private String question;
        private Long targetId;      // 可选：酒店、景点或餐饮 ID
        private String targetType;  // HOTEL / SCENIC / RESTAURANT，可选
    }

    @PostMapping("/ask")
    public ApiResponse<String> ask(@RequestBody SupportRequest req) {
        if (sparkApiPassword == null || sparkApiPassword.isBlank()) {
            return ApiResponse.error(500, "未配置星火大模型 API 密码，请在 application.yml 中配置 spark.api-password");
        }
        if (req.getQuestion() == null || req.getQuestion().isBlank()) {
            return ApiResponse.error(400, "问题不能为空");
        }
        StringBuilder contextBuilder = new StringBuilder();
        if (req.getTargetId() != null && req.getTargetType() != null) {
            if ("HOTEL".equalsIgnoreCase(req.getTargetType())) {
                hotelRepository.findById(req.getTargetId()).ifPresent(hotel -> {
                    contextBuilder.append("你现在是旅游预订平台的智能行程助手，用户正在查看的酒店信息如下：")
                            .append("酒店名称：").append(hotel.getName()).append("；")
                            .append("城市：").append(hotel.getCity() != null ? hotel.getCity().getName() : "未知").append("；")
                            .append("简介：").append(hotel.getDescription() != null ? hotel.getDescription() : "暂无简介").append("。");
                });
            } else if ("SCENIC".equalsIgnoreCase(req.getTargetType())) {
                scenicSpotRepository.findById(req.getTargetId()).ifPresent(spot -> {
                    contextBuilder.append("你现在是旅游预订平台的智能行程助手，用户正在查看的景点信息如下：")
                            .append("景点名称：").append(spot.getName()).append("；")
                            .append("城市：").append(spot.getCity() != null ? spot.getCity().getName() : "未知").append("；")
                            .append("简介：").append(spot.getDescription() != null ? spot.getDescription() : "暂无简介").append("。");
                });
            } else if ("RESTAURANT".equalsIgnoreCase(req.getTargetType())) {
                restaurantRepository.findById(req.getTargetId()).ifPresent(restaurant -> {
                    contextBuilder.append("你现在是旅游预订平台的智能行程助手，用户正在查看的餐饮信息如下：")
                            .append("店铺名称：").append(restaurant.getName()).append("；")
                            .append("城市：").append(restaurant.getCity() != null ? restaurant.getCity().getName() : "未知").append("；")
                            .append("地址：").append(restaurant.getAddress() != null ? restaurant.getAddress() : "暂无地址").append("；")
                            .append("简介：").append(restaurant.getDescription() != null ? restaurant.getDescription() : "暂无简介").append("。");
                });
            }
        }

        // 所有在线资源列表，限制 AI 只能从这里推荐
        List<Hotel> onlineHotels = hotelRepository.findByStatus(ResourceStatus.ONLINE);
        List<ScenicSpot> onlineScenics = scenicSpotRepository.findByStatus(ResourceStatus.ONLINE);
        List<Restaurant> onlineRestaurants = restaurantRepository.findByStatus(ResourceStatus.ONLINE);

        StringBuilder candidatesBuilder = new StringBuilder();
        candidatesBuilder.append("以下是平台当前可以推荐的酒店、景点和餐饮店铺，只能从这份列表中进行推荐：\n");
        candidatesBuilder.append("【酒店列表】\n");
        for (Hotel h : onlineHotels) {
            candidatesBuilder
                    .append("- 酒店ID:").append(h.getId())
                    .append("，名称:").append(h.getName())
                    .append("，城市:").append(h.getCity() != null ? h.getCity().getName() : "未知")
                    .append("\n");
        }
        candidatesBuilder.append("【景点列表】\n");
        for (ScenicSpot s : onlineScenics) {
            candidatesBuilder
                    .append("- 景点ID:").append(s.getId())
                    .append("，名称:").append(s.getName())
                    .append("，城市:").append(s.getCity() != null ? s.getCity().getName() : "未知")
                    .append("\n");
        }
        candidatesBuilder.append("【餐饮列表】\n");
        for (Restaurant r : onlineRestaurants) {
            candidatesBuilder
                    .append("- 餐饮ID:").append(r.getId())
                    .append("，名称:").append(r.getName())
                    .append("，城市:").append(r.getCity() != null ? r.getCity().getName() : "未知")
                    .append("\n");
        }

        String systemPrompt = "你是“GO anywhere.com”旅游服务平台的智能行程助手，"
                + "需要用简洁、友好的中文回答用户关于酒店、景点、餐饮、订单、入住和出行相关的问题。"
                + "【重要】当用户让你推荐酒店、景点或餐饮、制定旅游计划时，只能使用我提供的候选列表中的资源，"
                + "不能凭空编造或推荐列表之外的地点。回答时请写出推荐的名称和所在城市。"
                + "【链接格式】当你推荐具体酒店时，必须在该酒店名称后紧跟 [酒店:ID]，ID 为上面酒店列表中该酒店的 ID；"
                + "当你推荐具体景点时，必须在该景点名称后紧跟 [景点:ID]，ID 为上面景点列表中该景点的 ID；"
                + "当你推荐具体餐饮店铺时，必须在该店铺名称后紧跟 [餐饮:ID]，ID 为上面餐饮列表中该店铺的 ID。"
                + "例如：推荐北京某某酒店[酒店:5]、故宫[景点:3]、老北京炸酱面馆[餐饮:8]。这样用户可点击链接跳转平台下单。"
                + "如果涉及退款或价格等敏感内容，请给出建议流程，不要擅自承诺。";
        // 说明：
        // - 前端会解析 [酒店:ID]/[景点:ID]/[餐饮:ID] 并渲染为可点击链接（跳转到 /hotel/:id / /scenic/:id / /dining/:id）
        // - 通过“仅能从候选列表推荐 + 强制携带 ID 标记”的组合，减少模型幻觉并保证跳转可用

        StringBuilder userContent = new StringBuilder();
        userContent.append(candidatesBuilder).append("\n\n");
        if (contextBuilder.length() > 0) {
            userContent.append(contextBuilder).append("\n\n");
        }
        userContent.append("用户的问题是：").append(req.getQuestion());

        Map<String, Object> body = new HashMap<>();
        // 根据你在讯飞控制台开通的具体模型名称填写，例如 "4.0Ultra" / "lite" 等
        body.put("model", "4.0Ultra");

        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userContent.toString());

        body.put("messages", List.of(sysMsg, userMsg));
        body.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 星火 HTTP 接口使用 Bearer {APIPassword} 作为 Authorization
        headers.setBearerAuth(sparkApiPassword);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(SPARK_ENDPOINT, entity, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return ApiResponse.error(500, "调用星火大模型接口失败");
            }
            Object choicesObj = response.getBody().get("choices");
            if (choicesObj instanceof List<?> choices && !choices.isEmpty()) {
                Object first = choices.get(0);
                if (first instanceof Map<?, ?> choice) {
                    Object messageObj = choice.get("message");
                    if (messageObj instanceof Map<?, ?> message) {
                        Object content = message.get("content");
                        if (content != null) {
                            String answer = content.toString();
                            if (isRestaurantQuestion(req.getQuestion()) && !answer.contains("[餐饮:")) {
                                String fallback = buildRestaurantFallback(onlineRestaurants, req.getQuestion());
                                if (fallback != null) {
                                    return ApiResponse.success(fallback);
                                }
                            }
                            return ApiResponse.success(answer);
                        }
                    }
                }
            }
            if (isRestaurantQuestion(req.getQuestion())) {
                String fallback = buildRestaurantFallback(onlineRestaurants, req.getQuestion());
                if (fallback != null) {
                    return ApiResponse.success(fallback);
                }
            }
            return ApiResponse.error(500, "星火大模型返回结果解析失败");
        } catch (Exception e) {
            return ApiResponse.error(500, "调用星火大模型出错：" + e.getMessage());
        }
    }

    private boolean isRestaurantQuestion(String question) {
        if (question == null) return false;
        String q = question.toLowerCase();
        return q.contains("餐") || q.contains("吃") || q.contains("饭") || q.contains("餐饮") || q.contains("餐厅") || q.contains("饭店") || q.contains("中餐") || q.contains("火锅") || q.contains("烧烤");
    }

    private String buildRestaurantFallback(List<Restaurant> restaurants, String question) {
        if (restaurants == null || restaurants.isEmpty()) {
            return "目前平台暂未提供可推荐的餐饮店铺，请稍后再试。";
        }
        String city = extractCityName(question);
        List<Restaurant> filtered = restaurants;
        if (city != null) {
            filtered = restaurants.stream()
                    .filter(r -> r.getCity() != null && r.getCity().getName() != null && r.getCity().getName().contains(city))
                    .toList();
            if (filtered.isEmpty()) {
                filtered = restaurants;
            }
        }
        Restaurant r1 = filtered.get(0);
        Restaurant r2 = filtered.size() > 1 ? filtered.get(1) : null;
        StringBuilder sb = new StringBuilder();
        sb.append("餐饮推荐：").append(r1.getName())
          .append("[餐饮:").append(r1.getId()).append("]")
          .append("（").append(r1.getCity() != null ? r1.getCity().getName() : "未知城市").append("）");
        if (r2 != null) {
            sb.append("，").append(r2.getName())
              .append("[餐饮:").append(r2.getId()).append("]")
              .append("（").append(r2.getCity() != null ? r2.getCity().getName() : "未知城市").append("）");
        }
        sb.append("。点击店铺名称即可查看详情并预订。");
        return sb.toString();
    }

    private String extractCityName(String question) {
        if (question == null) return null;
        String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "成都", "重庆", "天津", "南京", "武汉", "西安", "长沙", "苏州", "厦门", "青岛", "大连", "昆明", "三亚", "哈尔滨", "郑州"};
        for (String city : cities) {
            if (question.contains(city)) {
                return city;
            }
        }
        return null;
    }
}

