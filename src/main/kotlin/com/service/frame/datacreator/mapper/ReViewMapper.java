package com.service.frame.datacreator.mapper;

import com.service.frame.datacreator.model.ReView;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReViewMapper {
    int deleteByPrimaryKey(String id);

    int insert(ReView record);

    int insertSelective(ReView record);

    ReView selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ReView record);

    int updateByPrimaryKey(ReView record);
}