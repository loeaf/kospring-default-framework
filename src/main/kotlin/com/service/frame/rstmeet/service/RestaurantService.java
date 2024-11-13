package com.service.frame.rstmeet.service;

import com.service.frame.common.misc.Service;
import com.service.frame.rstmeet.dto.params.RestaurantParam;
import com.service.frame.rstmeet.model.Restaurant;
import com.service.frame.rstmeet.model.RestaurantDto;

import java.io.FileNotFoundException;
import java.util.List;

public interface RestaurantService extends Service<Restaurant, String> {
    void registBulkByCSV() throws FileNotFoundException;

    List<RestaurantDto> findRestaurant(RestaurantParam restaurant);

    List<Restaurant> findRestaurantByRoadAddress(String roadAddress, String name);

    List<RestaurantDto> findRestaurantByInstaInfoId(String instaInfoId);

    void fetch(RestaurantParam dto);
}

