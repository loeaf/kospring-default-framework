package com.service.frame.member.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class FileUploadService {

    private val uploadDir = "uploads/member-files/"

    fun uploadFile(file: MultipartFile, prefix: String): String {
        if (file.isEmpty) {
            throw IllegalArgumentException("파일이 비어있습니다.")
        }

        val uploadPath = File(uploadDir)
        if (!uploadPath.exists()) {
            uploadPath.mkdirs()
        }

        val originalFilename = file.originalFilename ?: throw IllegalArgumentException("파일명이 없습니다.")
        val extension = originalFilename.substringAfterLast(".", "")
        val filename = "${prefix}_${UUID.randomUUID()}.${extension}"
        val filePath = Paths.get(uploadDir, filename)

        Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

        return filePath.toString()
    }

    fun deleteFile(filePath: String) {
        try {
            Files.deleteIfExists(Paths.get(filePath))
        } catch (e: Exception) {
            // 로그 처리
        }
    }
}