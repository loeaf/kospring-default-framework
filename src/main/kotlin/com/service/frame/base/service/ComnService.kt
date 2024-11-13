package com.service.frame.base.service

import com.service.frame.base.domain.CifyFloorMp
import com.service.frame.base.entity.CifyFloor

interface CifyFloorService {
    fun findAll(): List<CifyFloorMp>
    fun findAll2(): List<CifyFloor>
}