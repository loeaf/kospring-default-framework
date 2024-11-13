package com.service.frame.rstmeet.service;

import com.service.frame.common.misc.Service;
import com.service.frame.rstmeet.model.Restaurant;
import com.service.frame.rstmeet.model.TasteRoom;

import java.util.List;

public interface TasteRoomService extends Service<TasteRoom, String> {
    List<TasteRoom> findByRestaurant(Restaurant restaurant);

    List<TasteRoom> selectTasteRoom(String restaurantId);

    List<TasteRoom> findTasteRoomByMe();
}
