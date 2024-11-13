package com.service.frame.rstmeet.service;

import com.service.frame.common.misc.Service;
import com.service.frame.rstmeet.dto.params.LikeParam;
import com.service.frame.rstmeet.model.LikeList;
import com.service.frame.rstmeet.model.RestaurantDto;

import java.util.List;

public interface LikeListService extends Service<LikeList, String> {
    boolean likeByMapper(LikeParam likeParam);

    List<RestaurantDto> findLikeRestaurant();
}
