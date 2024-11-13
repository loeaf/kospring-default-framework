package com.service.frame.rstmeet.mapper;

import com.service.frame.rstmeet.model.LikeList;
import com.service.frame.rstmeet.model.RestaurantDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LikeListMapper {
    LikeList findByUserIdAndRestaurantId(String userId, String restaurantId);

    void regist(String id, String userId, String restaurantId);

    List<RestaurantDto> findLikeRestaurant(String userId);
}