package com.example.travelplatform.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HotelRoomPriceRepository extends JpaRepository<HotelRoomPrice, Long> {

    Optional<HotelRoomPrice> findByRoomAndDate(HotelRoom room, LocalDate date);

    List<HotelRoomPrice> findByRoomInAndDate(Iterable<HotelRoom> rooms, LocalDate date);

    List<HotelRoomPrice> findByRoomInAndDateBetween(Iterable<HotelRoom> rooms, LocalDate start, LocalDate end);
}

