package com.example.travelplatform.product;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.merchant.MerchantRepository;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/hotel-room-prices")
@CrossOrigin
public class HotelRoomPriceController {

    private final HotelRoomPriceRepository priceRepository;
    private final HotelRoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;

    public HotelRoomPriceController(HotelRoomPriceRepository priceRepository,
                                    HotelRoomRepository roomRepository,
                                    HotelRepository hotelRepository,
                                    UserRepository userRepository,
                                    MerchantRepository merchantRepository) {
        this.priceRepository = priceRepository;
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
    }

    // 说明：房型价格采用“每日随机一次”，由系统在价格区间内生成并落库

    // 用户端：获取某酒店在指定日期各房型价格以及整体价格区间
    @GetMapping("/public/by-hotel/{hotelId}")
    public ApiResponse<Map<String, Object>> pricesByHotel(@PathVariable Long hotelId,
                                                          @RequestParam(required = false) String date) {
        LocalDate d = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        List<HotelRoom> rooms = roomRepository.findByHotel(hotel);
        if (rooms.isEmpty()) {
            return ApiResponse.success(new HashMap<>());
        }
        List<HotelRoomPrice> prices = priceRepository.findByRoomInAndDate(rooms, d);

        Map<Long, BigDecimal> roomPriceMap = new HashMap<>();
        BigDecimal min = null;
        BigDecimal max = null;

        // 先放入商家主动设置的价格
        for (HotelRoomPrice p : prices) {
            roomPriceMap.put(p.getRoom().getId(), p.getPrice());
        }

        // 对于没有价格的房型，如果商家设置了价格区间，则在区间内随机生成当天价格并落库（每日随机一次）
        List<HotelRoomPrice> toSave = new ArrayList<>();
        for (HotelRoom room : rooms) {
            BigDecimal price = roomPriceMap.get(room.getId());
            if (price == null && room.getMinPrice() != null && room.getMaxPrice() != null) {
                BigDecimal low = room.getMinPrice();
                BigDecimal high = room.getMaxPrice();
                if (high.compareTo(low) < 0) {
                    BigDecimal tmp = low;
                    low = high;
                    high = tmp;
                }
                double lowDouble = low.doubleValue();
                double highDouble = high.doubleValue();
                double random = ThreadLocalRandom.current().nextDouble(lowDouble, highDouble);
                price = BigDecimal.valueOf(random).setScale(2, java.math.RoundingMode.HALF_UP);
                roomPriceMap.put(room.getId(), price);

                HotelRoomPrice p = new HotelRoomPrice();
                p.setRoom(room);
                p.setDate(d);
                p.setPrice(price);
                toSave.add(p);
            }
        }

        if (!toSave.isEmpty()) {
            priceRepository.saveAll(toSave);
        }

        // 计算价格区间
        for (BigDecimal p : roomPriceMap.values()) {
            if (p == null) continue;
            if (min == null || p.compareTo(min) < 0) {
                min = p;
            }
            if (max == null || p.compareTo(max) > 0) {
                max = p;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("roomPrices", roomPriceMap);
        result.put("minPrice", min);
        result.put("maxPrice", max);
        result.put("date", d.toString());
        return ApiResponse.success(result);
    }
}

