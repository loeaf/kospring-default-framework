package com.service.frame.order.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class FileInfo(
    val id: Long,
    val fileName: String,
    val originalFileName: String,
    val fileSize: Long,
    val fileType: String,
    val filePath: String,
    val description: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val uploadedAt: LocalDateTime = LocalDateTime.now()
) {
    fun getDownloadUrl(orderId: Long): String {
        return "/api/orders/${orderId}/files/${id}/download"
    }

    fun isPreviewAvailable(): Boolean {
        return fileType.startsWith("image/") || fileType == "application/pdf"
    }

    fun getPreviewUrl(orderId: Long): String? {
        return if (isPreviewAvailable()) {
            "/api/orders/${orderId}/files/${id}/preview"
        } else null
    }

    fun getFormattedFileSize(): String {
        return when {
            fileSize < 1024 -> "${fileSize}B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024}KB"
            fileSize < 1024 * 1024 * 1024 -> "${fileSize / (1024 * 1024)}MB"
            else -> "${fileSize / (1024 * 1024 * 1024)}GB"
        }
    }

    fun getFileIcon(): String {
        return when {
            fileType.contains("pdf") -> "📄"
            fileType.contains("image") -> "🎨"
            fileType.contains("video") -> "🎥"
            fileType.contains("word") || fileType.contains("document") -> "📃"
            fileType.contains("powerpoint") || fileType.contains("presentation") -> "📈"
            fileType.contains("zip") || fileType.contains("compressed") -> "🗄"
            else -> "📁"
        }
    }
}