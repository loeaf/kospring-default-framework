package com.service.frame.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AdProperties::class)
class AdConfig

@ConfigurationProperties(prefix = "ad")
data class AdProperties(
    var count: Int = 3,
    var maxRetryCount: Int = 3,
    var defaultStatus: String = "PENDING"
) {
    companion object {
        const val DEFAULT_AD_COUNT = 3
        const val DEFAULT_RETRY_COUNT = 3
    }
}