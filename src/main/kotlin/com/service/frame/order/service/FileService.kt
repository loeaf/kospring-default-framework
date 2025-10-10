package com.service.frame.order.service

import com.service.frame.order.repository.OrderRepository
import com.service.frame.ad.repository.AdTaskRepository
import com.service.frame.ad.entity.AdTask
import com.service.frame.order.dto.FileInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class FileService(
    private val orderRepository: OrderRepository,
    private val adTaskRepository: AdTaskRepository,
    private val objectMapper: ObjectMapper
) {
    
    private val uploadPath = Paths.get("uploads/orders")
    
    init {
        try {
            Files.createDirectories(uploadPath)
        } catch (e: IOException) {
            throw RuntimeException("Could not create upload directory!", e)
        }
    }

    fun uploadFiles(
        orderId: Long, 
        files: Array<MultipartFile>, 
        description: String? = null,
        submitterName: String? = null
    ): FileUploadResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }

        val orderDir = uploadPath.resolve(orderId.toString())
        Files.createDirectories(orderDir)

        val uploadedFiles = mutableListOf<FileInfo>()
        var totalSize = 0L

        files.forEach { file ->
            if (!file.isEmpty) {
                val fileInfo = saveFile(file, orderDir, description)
                uploadedFiles.add(fileInfo)
                totalSize += fileInfo.fileSize
            }
        }

        // AdTask에 파일 정보 저장
        saveFileInfoToAdTask(orderId, uploadedFiles, submitterName)

        return FileUploadResponse(
            orderId = orderId,
            uploadedFiles = uploadedFiles,
            totalFiles = uploadedFiles.size,
            totalSize = totalSize
        )
    }

    fun getOrderFiles(orderId: Long): OrderFilesResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }

        val adTask = order.adTask
        val files = emptyList<FileInfo>() // TODO: 파일 정보 저장 방식 재구현 필요

        return OrderFilesResponse(
            orderId = orderId,
            orderNumber = order.orderNumber ?: "",
            orderTitle = order.productName,
            orderStatus = order.status,
            files = files,
            totalFiles = files.size,
            totalSize = files.sumOf { it.fileSize }
        )
    }

    fun downloadFile(orderId: Long, fileId: Long): Resource {
        val files = getOrderFiles(orderId).files
        val fileInfo = files.find { it.id == fileId }
            ?: throw IllegalArgumentException("File not found: $fileId")

        val filePath = Paths.get(fileInfo.filePath)
        val resource = UrlResource(filePath.toUri())

        if (resource.exists() && resource.isReadable) {
            return resource
        } else {
            throw RuntimeException("Could not read file: ${fileInfo.fileName}")
        }
    }

    fun previewFile(orderId: Long, fileId: Long): FilePreviewResponse {
        val files = getOrderFiles(orderId).files
        val fileInfo = files.find { it.id == fileId }
            ?: throw IllegalArgumentException("File not found: $fileId")

        return FilePreviewResponse(
            id = fileInfo.id,
            fileName = fileInfo.fileName,
            originalFileName = fileInfo.originalFileName,
            fileSize = fileInfo.fileSize,
            fileType = fileInfo.fileType,
            description = fileInfo.description,
            uploadedAt = fileInfo.uploadedAt,
            previewAvailable = fileInfo.isPreviewAvailable(),
            previewUrl = fileInfo.getPreviewUrl(orderId),
            downloadUrl = fileInfo.getDownloadUrl(orderId)
        )
    }

    fun deleteFile(orderId: Long, fileId: Long): FileDeleteResponse {
        val orderFiles = getOrderFiles(orderId)
        val fileInfo = orderFiles.files.find { it.id == fileId }
            ?: throw IllegalArgumentException("File not found: $fileId")

        // 실제 파일 삭제
        try {
            val filePath = Paths.get(fileInfo.filePath)
            Files.deleteIfExists(filePath)
        } catch (e: IOException) {
            throw RuntimeException("Could not delete file: ${fileInfo.fileName}", e)
        }

        // AdTask에서 파일 정보 제거
        val updatedFiles = orderFiles.files.filter { it.id != fileId }
        updateAdTaskFiles(orderId, updatedFiles)

        return FileDeleteResponse(
            message = "파일이 성공적으로 삭제되었습니다.",
            deletedFileId = fileId,
            deletedFileName = fileInfo.fileName
        )
    }

    fun downloadAllFiles(orderId: Long): Resource {
        val orderFiles = getOrderFiles(orderId)
        if (orderFiles.files.isEmpty()) {
            throw IllegalArgumentException("No files found for order: $orderId")
        }

        val tempDir = Files.createTempDirectory("order_${orderId}_zip")
        val zipFile = tempDir.resolve("order_${orderId}_files.zip")

        try {
            java.util.zip.ZipOutputStream(Files.newOutputStream(zipFile)).use { zipOut ->
                orderFiles.files.forEach { fileInfo ->
                    val filePath = Paths.get(fileInfo.filePath)
                    if (Files.exists(filePath)) {
                        val entry = java.util.zip.ZipEntry(fileInfo.fileName)
                        zipOut.putNextEntry(entry)
                        Files.copy(filePath, zipOut)
                        zipOut.closeEntry()
                    }
                }
            }

            val resource = UrlResource(zipFile.toUri())
            if (resource.exists() && resource.isReadable) {
                return resource
            } else {
                throw RuntimeException("Could not create ZIP file for order: $orderId")
            }
        } catch (e: IOException) {
            throw RuntimeException("Error creating ZIP file for order: $orderId", e)
        }
    }

    private fun saveFile(file: MultipartFile, orderDir: Path, description: String?): FileInfo {
        val originalFileName = file.originalFilename ?: "unknown"
        val fileExtension = originalFileName.substringAfterLast(".", "")
        val fileName = "${UUID.randomUUID()}.${fileExtension}"
        val filePath = orderDir.resolve(fileName)

        Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

        return FileInfo(
            id = System.currentTimeMillis(),
            fileName = originalFileName,
            originalFileName = originalFileName,
            fileSize = file.size,
            fileType = file.contentType ?: "application/octet-stream",
            filePath = filePath.toString(),
            description = description,
            uploadedAt = LocalDateTime.now()
        )
    }

    private fun saveFileInfoToAdTask(orderId: Long, files: List<FileInfo>, submitterName: String?) {
        // TODO: 파일 정보 저장 방식 재구현 필요
        // AdTask에 resultFiles 필드가 없으므로 별도 테이블이나 다른 방법으로 저장 필요
    }

    private fun updateAdTaskFiles(orderId: Long, files: List<FileInfo>) {
        // TODO: 파일 정보 저장 방식 재구현 필요
        // AdTask에 resultFiles 필드가 없으므로 별도 테이블이나 다른 방법으로 저장 필요
    }
}

// Response DTOs
data class FileUploadResponse(
    val orderId: Long,
    val uploadedFiles: List<FileInfo>,
    val totalFiles: Int,
    val totalSize: Long
)

data class OrderFilesResponse(
    val orderId: Long,
    val orderNumber: String,
    val orderTitle: String,
    val orderStatus: com.service.frame.order.entity.OrderStatus,
    val files: List<FileInfo>,
    val totalFiles: Int,
    val totalSize: Long
)

data class FilePreviewResponse(
    val id: Long,
    val fileName: String,
    val originalFileName: String,
    val fileSize: Long,
    val fileType: String,
    val description: String?,
    val uploadedAt: LocalDateTime,
    val previewAvailable: Boolean,
    val previewUrl: String?,
    val downloadUrl: String
)

data class FileDeleteResponse(
    val message: String,
    val deletedFileId: Long,
    val deletedFileName: String
)