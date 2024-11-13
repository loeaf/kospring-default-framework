package com.service.frame.datacreator.mapper;

import com.service.frame.datacreator.dto.params.InstaInfoParam;
import com.service.frame.datacreator.model.InstaInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InstaInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(InstaInfo record);

    int insertSelective(InstaInfo record);

    List<InstaInfo> selectByPrimaryKey(InstaInfoParam param);

    int updateByPrimaryKeySelective(InstaInfo record);

    int updateByPrimaryKey(InstaInfo record);
}