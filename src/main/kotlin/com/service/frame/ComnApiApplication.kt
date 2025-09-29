package com.service.frame

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
@SpringBootApplication(scanBasePackages = ["com.service.frame"])
@MapperScan("com.service.frame.**.mapper")
@EnableJpaRepositories
@EnableFeignClients
class FontwikiApiApplication

fun main(args: Array<String>) {
    runApplication<FontwikiApiApplication>(*args)
}
