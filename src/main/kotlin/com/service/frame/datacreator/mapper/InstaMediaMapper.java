package com.service.frame.datacreator.mapper;

import com.service.frame.datacreator.model.InstaMedia;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InstaMediaMapper {
    int deleteByPrimaryKey(String id);

    int insert(InstaMedia record);

    int insertSelective(InstaMedia record);

    InstaMedia selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(InstaMedia record);

    int updateByPrimaryKey(InstaMedia record);
}