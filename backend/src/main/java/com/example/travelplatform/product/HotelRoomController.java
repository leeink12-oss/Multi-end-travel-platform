package com.example.travelplatform.product;

import com.example.travelplatform.common.ApiResponse;
import com.example.travelplatform.merchant.Merchant;
import com.example.travelplatform.merchant.MerchantRepository;
import com.example.travelplatform.user.User;
import com.example.travelplatform.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotel-rooms")
@CrossOrigin
public class HotelRoomController {

    private final HotelRoomRepository hotelRoomRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;

    public HotelRoomController(HotelRoomRepository hotelRoomRepository,
                               HotelRepository hotelRepository,
                               UserRepository userRepository,
                               MerchantRepository merchantRepository) {
        this.hotelRoomRepository = hotelRoomRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/by-hotel/{hotelId}")
    public ApiResponse<List<HotelRoom>> listByHotel(@PathVariable Long hotelId,
                                                    Authentication authentication) {
        Hotel hotel = ensureHotelOwnedByCurrentMerchant(hotelId, authentication);
        return ApiResponse.success(hotelRoomRepository.findByHotel(hotel));
    }

    // 用户端查看某酒店的房型信息（无需登录）
    @GetMapping("/public/by-hotel/{hotelId}")
    public ApiResponse<List<HotelRoom>> publicByHotel(@PathVariable Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        return ApiResponse.success(hotelRoomRepository.findByHotel(hotel));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PostMapping
    public ApiResponse<HotelRoom> create(@RequestBody HotelRoom room,
                                         Authentication authentication) {
        Hotel hotel = ensureHotelOwnedByCurrentMerchant(room.getHotel().getId(), authentication);
        room.setId(null);
        room.setHotel(hotel);
        return ApiResponse.success(hotelRoomRepository.save(room));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @PutMapping("/{id}")
    public ApiResponse<HotelRoom> update(@PathVariable Long id,
                                         @RequestBody HotelRoom updated,
                                         Authentication authentication) {
        HotelRoom room = hotelRoomRepository.findById(id).orElseThrow();
        ensureHotelOwnedByCurrentMerchant(room.getHotel().getId(), authentication);
        room.setRoomType(updated.getRoomType());
        room.setSize(updated.getSize());
        room.setFacilities(updated.getFacilities());
        room.setFloor(updated.getFloor());
        room.setImageUrl(updated.getImageUrl());
        room.setMinPrice(updated.getMinPrice());
        room.setMaxPrice(updated.getMaxPrice());
        return ApiResponse.success(hotelRoomRepository.save(room));
    }

    @PreAuthorize("hasRole('MERCHANT')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    Authentication authentication) {
        HotelRoom room = hotelRoomRepository.findById(id).orElseThrow();
        ensureHotelOwnedByCurrentMerchant(room.getHotel().getId(), authentication);
        hotelRoomRepository.delete(room);
        return ApiResponse.success(null);
    }

    private Hotel ensureHotelOwnedByCurrentMerchant(Long hotelId, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        Merchant merchant = merchantRepository.findByUser(user).orElseThrow();
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        if (!hotel.getMerchant().getId().equals(merchant.getId())) {
            throw new RuntimeException("无权操作该酒店房型");
        }
        return hotel;
    }
}

