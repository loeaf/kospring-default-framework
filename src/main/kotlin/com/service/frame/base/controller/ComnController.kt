package com.service.frame.base.controller

import com.service.frame.base.domain.CifyFloorMp
import com.service.frame.base.entity.CifyFloor
import com.service.frame.base.service.CifyFloorService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/CifyFloor")
class CifyFloorController (private val CifyFloorService: CifyFloorService) {
    @GetMapping("/hello")
    fun hello(): List<CifyFloorMp> {
        return CifyFloorService.findAll()
    }
    @GetMapping("/hello2")
    fun hello2(): List<CifyFloor> {
        return CifyFloorService.findAll2()
    }
    @GetMapping("/world")
    fun world(): String {
        return "world"
    }
}