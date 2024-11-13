package com.service.frame.rstmeet.repository;

import com.service.frame.rstmeet.model.Restaurant;
import com.service.frame.siginin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TasteRoomRepository extends JpaRepository<com.service.frame.rstmeet.model.TasteRoom, String> {
    List<com.service.frame.rstmeet.model.TasteRoom> findByRestaurantAndUserNot(Restaurant restaurant, User user);
}