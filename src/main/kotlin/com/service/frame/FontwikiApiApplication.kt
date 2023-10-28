package com.service.frame

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FontwikiApiApplication

fun main(args: Array<String>) {
    runApplication<FontwikiApiApplication>(*args)
}
