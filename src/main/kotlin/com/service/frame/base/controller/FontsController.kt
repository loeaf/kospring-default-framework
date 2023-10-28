package com.service.frame.base.controller

import com.service.frame.base.domain.FontsMp
import com.service.frame.base.entity.Fonts
import com.service.frame.base.service.FontsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/fonts")
class FontsController (private val fontsService: FontsService) {
    @GetMapping("/hello")
    fun hello(): List<FontsMp> {
        return fontsService.findAll()
    }
    @GetMapping("/hello2")
    fun hello2(): List<Fonts> {
        return fontsService.findAll2()
    }
    @GetMapping("/world")
    fun world(): String {
        return "world"
    }
}