package com.service.frame.ad.dto

data class AdPreviewResponse(
    val featured: List<AdPreviewItem>,
    val statistics: AdPreviewStatistics
)

data class AdPreviewItem(
    val id: String,
    val title: MultiLanguageText,
    val description: MultiLanguageText,
    val client: MultiLanguageText,
    val publisher: MultiLanguageText,
    val tags: List<String>,
    val htmlPath: String,
    val previewHeight: String = "320px",
    val category: String,
    val status: String
)

data class MultiLanguageText(
    val ko: String = "",
    val en: String = ""
)

data class AdPreviewStatistics(
    val totalAds: Int,
    val featuredAds: Int,
    val categories: List<String>
)