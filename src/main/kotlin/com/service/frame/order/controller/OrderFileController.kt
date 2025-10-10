package com.service.frame.order.controller

import com.service.frame.order.service.FileService
import com.service.frame.order.service.*
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/orders")
class OrderFileController(
    private val fileService: FileService
) {

    /**
     * 결과물 파일 업로드
     */
    @PostMapping("/{orderId}/files")
    fun uploadFiles(
        @PathVariable orderId: Long,
        @RequestParam("files") files: Array<MultipartFile>,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) submitterName: String?
    ): ResponseEntity<FileUploadResponse> {
        val result = fileService.uploadFiles(orderId, files, description, submitterName)
        return ResponseEntity.ok(result)
    }

    /**
     * 주문별 결과물 파일 목록 조회
     */
    @GetMapping("/{orderId}/files")
    fun getOrderFiles(@PathVariable orderId: Long): ResponseEntity<OrderFilesResponse> {
        val result = fileService.getOrderFiles(orderId)
        return ResponseEntity.ok(result)
    }

    /**
     * 결과물 파일 다운로드
     */
    @GetMapping("/{orderId}/files/{fileId}/download")
    fun downloadFile(
        @PathVariable orderId: Long,
        @PathVariable fileId: Long
    ): ResponseEntity<Resource> {
        val resource = fileService.downloadFile(orderId, fileId)
        val previewInfo = fileService.previewFile(orderId, fileId)
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(previewInfo.fileType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${previewInfo.originalFileName}\"")
            .body(resource)
    }

    /**
     * 결과물 파일 미리보기
     */
    @GetMapping("/{orderId}/files/{fileId}/preview")
    fun previewFile(
        @PathVariable orderId: Long,
        @PathVariable fileId: Long
    ): ResponseEntity<FilePreviewResponse> {
        val result = fileService.previewFile(orderId, fileId)
        return ResponseEntity.ok(result)
    }

    /**
     * 결과물 파일 삭제
     */
    @DeleteMapping("/{orderId}/files/{fileId}")
    fun deleteFile(
        @PathVariable orderId: Long,
        @PathVariable fileId: Long
    ): ResponseEntity<FileDeleteResponse> {
        val result = fileService.deleteFile(orderId, fileId)
        return ResponseEntity.ok(result)
    }
}