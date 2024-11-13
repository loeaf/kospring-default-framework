package com.service.frame.base.mapper

import com.service.frame.base.domain.CifyFloorMp
import org.apache.ibatis.annotations.Mapper

@Mapper
public interface CifyFloorMapper {
    fun selectAll(): List<CifyFloorMp>
}