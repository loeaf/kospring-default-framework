package com.service.frame.base.service.Impl

import com.service.frame.base.domain.FontsMp
import com.service.frame.base.entity.Fonts
import com.service.frame.base.mapper.FontsMapper
import com.service.frame.base.repository.FontsRepository
import com.service.frame.base.service.FontsService
import org.springframework.stereotype.Service

@Service
class FontsServiceImpl(private val fontsMapper: FontsMapper,
                       private val fontsRepository: FontsRepository
) : FontsService {
    override fun findAll(): List<FontsMp> {
        val result = fontsMapper.selectAll()
        println(result)
        return result
    }

    override fun findAll2(): List<Fonts> {
        return fontsRepository.findAll()
    }
}