package com.service.frame.base.service

import com.service.frame.base.domain.FontsMp
import com.service.frame.base.entity.Fonts

interface FontsService {
    fun findAll(): List<FontsMp>
    fun findAll2(): List<Fonts>
}