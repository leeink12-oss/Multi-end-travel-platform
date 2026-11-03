package com.example.travelplatform.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRoomRepository extends JpaRepository<HotelRoom, Long> {

    List<HotelRoom> findByHotel(Hotel hotel);
}

