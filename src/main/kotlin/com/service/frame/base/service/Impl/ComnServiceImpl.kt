package com.service.frame.base.service.Impl

import com.service.frame.base.domain.CifyFloorMp
import com.service.frame.base.entity.CifyFloor
import com.service.frame.base.mapper.CifyFloorMapper
import com.service.frame.base.repository.CifyFloorRepository
import com.service.frame.base.service.CifyFloorService
import org.springframework.stereotype.Service

@Service
class CifyFloorServiceImpl(private val CifyFloorMapper: CifyFloorMapper,
                       private val CifyFloorRepository: CifyFloorRepository
) : CifyFloorService {
    override fun findAll(): List<CifyFloorMp> {
        val result = CifyFloorMapper.selectAll()
        println(result)
        return result
    }

    override fun findAll2(): List<CifyFloor> {
        return CifyFloorRepository.findAll()
    }
}