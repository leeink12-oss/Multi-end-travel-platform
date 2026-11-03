package com.example.travelplatform.transport;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.merchant.MerchantRepository;
import com.example.travelplatform.merchant.MerchantType;
import com.example.travelplatform.product.ResourceStatus;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transport")
@CrossOrigin
public class TransportController {

    private final FlightRepository flightRepository;
    private final TrainRepository trainRepository;
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final TransportScheduleGenerator transportScheduleGenerator;

    public TransportController(FlightRepository flightRepository,
                               TrainRepository trainRepository,
                               UserRepository userRepository,
                               MerchantRepository merchantRepository,
                               TransportScheduleGenerator transportScheduleGenerator) {
        this.flightRepository = flightRepository;
        this.trainRepository = trainRepository;
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
        this.transportScheduleGenerator = transportScheduleGenerator;
    }

    // ============ public ============
    @GetMapping("/flights")
    public ApiResponse<List<Flight>> publicFlights() {
        return ApiResponse.success(flightRepository.findByStatus(ResourceStatus.ONLINE));
    }

    @GetMapping("/flights/{id}")
    public ApiResponse<Flight> flightDetail(@PathVariable Long id) {
        return ApiResponse.success(flightRepository.findById(id).orElseThrow());
    }

    @GetMapping("/trains")
    public ApiResponse<List<Train>> publicTrains() {
        return ApiResponse.success(trainRepository.findByStatus(ResourceStatus.ONLINE));
    }

    @GetMapping("/trains/{id}")
    public ApiResponse<Train> trainDetail(@PathVariable Long id) {
        return ApiResponse.success(trainRepository.findById(id).orElseThrow());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generate-next-30-days")
    public ApiResponse<String> generateNext30Days() {
        transportScheduleGenerator.generateForNext30Days();
        return ApiResponse.success("已生成未来30天航班与列车数据");
    }

    // ============ merchant (TRANSPORT only) ============
    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/merchant/flights")
    public ApiResponse<List<Flight>> myFlights(Authentication authentication) {
        Merchant merchant = ensureTransportMerchant(authentication);
        return ApiResponse.success(flightRepository.findByMerchant(merchant));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/merchant/trains")
    public ApiResponse<List<Train>> myTrains(Authentication authentication) {
        Merchant merchant = ensureTransportMerchant(authentication);
        return ApiResponse.success(trainRepository.findByMerchant(merchant));
    }

    @Data
    public static class FlightRequest {
        @NotBlank
        private String flightNo;
        @NotBlank
        private String fromCity;
        @NotBlank
        private String toCity;
        private String departTime; // ISO yyyy-MM-ddTHH:mm
        private String arriveTime; // ISO yyyy-MM-ddTHH:mm
        private BigDecimal price; // 可选：不填则由各舱位最低价自动生成
        private BigDecimal firstClassPrice;
        private BigDecimal businessClassPrice;
        private BigDecimal economyClassPrice;
        private String coverImage;
        private ResourceStatus status;
    }

    @Data
    public static class TrainRequest {
        @NotBlank
        private String trainNo;
        @NotBlank
        private String fromCity;
        @NotBlank
        private String toCity;
        private String departTime;
        private String arriveTime;
        private BigDecimal price; // 可选：不填则由席别最低价自动生成
        private TrainType trainType;
        // 动车席别
        private BigDecimal emuFirstClassSeatPrice;
        private BigDecimal emuFirstSeatPrice;
        private BigDecimal emuSecondSeatPrice;
        private BigDecimal emuNoSeatPrice;
        // 火车席别
        private BigDecimal trainHardSeatPrice;
        private BigDecimal trainSoftSleeperPrice;
        private BigDecimal trainHardSleeperPrice;
        private String coverImage;
        private ResourceStatus status;
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PostMapping("/merchant/flights")
    public ApiResponse<Flight> createFlight(@RequestBody FlightRequest req, Authentication authentication) {
        Merchant merchant = ensureTransportMerchant(authentication);
        if (isBlank(req.getDepartTime()) || isBlank(req.getArriveTime())) {
            return ApiResponse.error(400, "请先选择起飞时间和到达时间");
        }
        Flight f = new Flight();
        f.setMerchant(merchant);
        applyFlightRequest(f, req);
        return ApiResponse.success(flightRepository.save(f));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/merchant/flights/{id}")
    public ApiResponse<Flight> updateFlight(@PathVariable Long id, @RequestBody FlightRequest req, Authentication authentication) {
        Merchant merchant = ensureTransportMerchant(authentication);
        if (isBlank(req.getDepartTime()) || isBlank(req.getArriveTime())) {
            return ApiResponse.error(400, "请先选择起飞时间和到达时间");
        }
        Flight f = flightRepository.findById(id).orElseThrow();
        if (!f.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该航班");
        }
        applyFlightRequest(f, req);
        return ApiResponse.success(flightRepository.save(f));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @DeleteMapping("/merchant/flights/{id}")
    public ApiResponse<Void> deleteFlight(@PathVariable Long id, Authentication authentication) {
        Merchant merchant = ensureTransportMerchant(authentication);
        Flight f = flightRepository.findById(id).orElseThrow();
        if (!f.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该航班");
        }
        flightRepository.delete(f);
        return ApiResponse.success(null);
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PostMapping("/merchant/trains")
    public ApiResponse<Train> createTrain(@RequestBody TrainRequest req, Authentication authentication) {
        Merchant merchant = ensureTransportMerchant(authentication);
        if (isBlank(req.getDepartTime()) || isBlank(req.getArriveTime())) {
            return ApiResponse.error(400, "请先选择出发时间和到达时间");
        }
        Train t = new Train();
        t.setMerchant(merchant);
        applyTrainRequest(t, req);
        return ApiResponse.success(trainRepository.save(t));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/merchant/trains/{id}")
    public ApiResponse<Train> updateTrain(@PathVariable Long id, @RequestBody TrainRequest req, Authentication authentication) {
        Merchant merchant = ensureTransportMerchant(authentication);
        if (isBlank(req.getDepartTime()) || isBlank(req.getArriveTime())) {
            return ApiResponse.error(400, "请先选择出发时间和到达时间");
        }
        Train t = trainRepository.findById(id).orElseThrow();
        if (!t.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该动车");
        }
        applyTrainRequest(t, req);
        return ApiResponse.success(trainRepository.save(t));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @DeleteMapping("/merchant/trains/{id}")
    public ApiResponse<Void> deleteTrain(@PathVariable Long id, Authentication authentication) {
        Merchant merchant = ensureTransportMerchant(authentication);
        Train t = trainRepository.findById(id).orElseThrow();
        if (!t.getMerchant().getId().equals(merchant.getId())) {
            return ApiResponse.error(403, "无权操作该动车");
        }
        trainRepository.delete(t);
        return ApiResponse.success(null);
    }

    private Merchant ensureTransportMerchant(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        if (merchant.getType() != MerchantType.TRANSPORT) {
            throw new RuntimeException("当前商家不是出行商家，无法管理航班/动车");
        }
        return merchant;
    }

    private void applyFlightRequest(Flight f, FlightRequest req) {
        f.setFlightNo(req.getFlightNo());
        f.setFromCity(req.getFromCity());
        f.setToCity(req.getToCity());
        if (!isBlank(req.getDepartTime())) f.setDepartTime(LocalDateTime.parse(req.getDepartTime()));
        if (!isBlank(req.getArriveTime())) f.setArriveTime(LocalDateTime.parse(req.getArriveTime()));
        f.setFirstClassPrice(req.getFirstClassPrice());
        f.setBusinessClassPrice(req.getBusinessClassPrice());
        f.setEconomyClassPrice(req.getEconomyClassPrice());
        if (req.getPrice() != null) {
            f.setPrice(req.getPrice());
        } else {
            f.setPrice(minNonNull(req.getFirstClassPrice(), req.getBusinessClassPrice(), req.getEconomyClassPrice()));
        }
        f.setCoverImage(req.getCoverImage());
        if (req.getStatus() != null) f.setStatus(req.getStatus());
    }

    private void applyTrainRequest(Train t, TrainRequest req) {
        t.setTrainNo(req.getTrainNo());
        t.setFromCity(req.getFromCity());
        t.setToCity(req.getToCity());
        if (!isBlank(req.getDepartTime())) t.setDepartTime(LocalDateTime.parse(req.getDepartTime()));
        if (!isBlank(req.getArriveTime())) t.setArriveTime(LocalDateTime.parse(req.getArriveTime()));
        if (req.getTrainType() != null) t.setTrainType(req.getTrainType());
        t.setEmuFirstClassSeatPrice(req.getEmuFirstClassSeatPrice());
        t.setEmuFirstSeatPrice(req.getEmuFirstSeatPrice());
        t.setEmuSecondSeatPrice(req.getEmuSecondSeatPrice());
        t.setEmuNoSeatPrice(req.getEmuNoSeatPrice());
        t.setTrainHardSeatPrice(req.getTrainHardSeatPrice());
        t.setTrainSoftSleeperPrice(req.getTrainSoftSleeperPrice());
        t.setTrainHardSleeperPrice(req.getTrainHardSleeperPrice());
        if (req.getPrice() != null) {
            t.setPrice(req.getPrice());
        } else {
            t.setPrice(minNonNull(
                    req.getEmuFirstClassSeatPrice(), req.getEmuFirstSeatPrice(), req.getEmuSecondSeatPrice(), req.getEmuNoSeatPrice(),
                    req.getTrainHardSeatPrice(), req.getTrainSoftSleeperPrice(), req.getTrainHardSleeperPrice()
            ));
        }
        t.setCoverImage(req.getCoverImage());
        if (req.getStatus() != null) t.setStatus(req.getStatus());
    }

    private BigDecimal minNonNull(BigDecimal... values) {
        BigDecimal min = null;
        for (BigDecimal v : values) {
            if (v == null) continue;
            if (min == null || v.compareTo(min) < 0) {
                min = v;
            }
        }
        return min != null ? min : BigDecimal.ZERO;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

