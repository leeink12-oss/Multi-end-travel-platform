package com.example.travelplatform.restaurant;

import com.example.travelplatform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiningReservationRepository extends JpaRepository<DiningReservation, Long> {
    List<DiningReservation> findByUserOrderByCreateTimeDesc(User user);

    List<DiningReservation> findByRestaurantOrderByCreateTimeDesc(Restaurant restaurant);
}

