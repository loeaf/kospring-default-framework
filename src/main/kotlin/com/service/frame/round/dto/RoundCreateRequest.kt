package com.service.frame.round.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.validation.constraints.*

data class RoundCreateRequest(
    @field:NotBlank(message = "라운드 제목은 필수입니다")
    @field:Size(max = 255, message = "라운드 제목은 255자를 초과할 수 없습니다")
    val title: String,

    @field:Size(max = 1000, message = "라운드 설명은 1000자를 초과할 수 없습니다")
    val description: String? = null,

    @field:Size(max = 100, message = "카테고리는 100자를 초과할 수 없습니다")
    val category: String? = null,

    @field:NotNull(message = "발주 금액은 필수입니다")
    @field:DecimalMin(value = "0.01", message = "발주 금액은 0보다 커야 합니다")
    @field:Digits(integer = 10, fraction = 2, message = "발주 금액 형식이 올바르지 않습니다")
    val orderAmount: BigDecimal,

    @field:NotNull(message = "시작일은 필수입니다")
    @field:Future(message = "시작일은 현재 시간 이후여야 합니다")
    val startDate: LocalDateTime,

    @field:NotNull(message = "종료일은 필수입니다")
    val endDate: LocalDateTime,

    @field:Min(value = 1, message = "최대 참여자 수는 1명 이상이어야 합니다")
    val maxParticipants: Int? = null
) {
    init {
        require(endDate.isAfter(startDate)) { "종료일은 시작일 이후여야 합니다" }
    }
}