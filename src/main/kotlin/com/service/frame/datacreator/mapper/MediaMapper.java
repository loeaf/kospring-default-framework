package com.service.frame.datacreator.mapper;


import com.service.frame.datacreator.model.Media;

import java.util.List;

public interface MediaMapper {
    int deleteByPrimaryKey(String id);

    int insert(Media record);

    int insertSelective(Media record);

    Media selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Media record);

    int updateByPrimaryKey(Media record);

    List<Media> findByRestaurantId(String id);
}