package com.service.frame.rstmeet.mapper;

import com.service.frame.rstmeet.dto.params.RestaurantParam;
import com.service.frame.rstmeet.model.RestaurantDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RestaurantProcMapper {
    @Deprecated
    List<RestaurantDto> findRestaurant(RestaurantParam o);
    List<RestaurantDto> findRestaurant2(RestaurantParam o);

    List<RestaurantDto> findRestaurantByInstaInfoId(String instaInfoId);

    RestaurantDto findRestaurantById(String id);
}