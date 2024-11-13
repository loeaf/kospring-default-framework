package com.service.frame

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.service.frame"])
@ComponentScan(basePackages = ["com.service.frame.datacreator.mapper"])
@EnableJpaRepositories
@EnableFeignClients
class FontwikiApiApplication

fun main(args: Array<String>) {
    runApplication<FontwikiApiApplication>(*args)
}
