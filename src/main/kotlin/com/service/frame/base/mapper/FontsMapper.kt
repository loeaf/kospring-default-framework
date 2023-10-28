package com.service.frame.base.mapper

import com.service.frame.base.domain.FontsMp
import org.apache.ibatis.annotations.Mapper

@Mapper
interface FontsMapper {
    fun selectAll(): List<FontsMp>
}