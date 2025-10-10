package com.service.frame.order.entity

import com.service.frame.member.entity.Member
import com.service.frame.ad.entity.AdTask
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "order_number", unique = true, length = 20)
    val orderNumber: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_task_id", nullable = false)
    val adTask: AdTask = AdTask(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member = Member(),

    @Column(name = "product_name", nullable = false)
    val productName: String = "",

    @Column(nullable = false)
    val quantity: Int = 1,

    @Column(columnDefinition = "TEXT")
    val requirements: String? = null,

    @Column
    val deadline: LocalDate? = null,

    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    @Column(name = "completion_date")
    val completionDate: LocalDate? = null,

    @Column(name = "failure_date")
    val failureDate: LocalDate? = null,

    @Column(name = "progress_rate", nullable = false)
    val progressRate: Int = 0,

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    val failureReason: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "submitted_at", nullable = false)
    val submittedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "reviewed_at")
    val reviewedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    val reviewedBy: Member? = null,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val payments: List<OrderPayment> = emptyList()
) {
    fun updateStatus(newStatus: OrderStatus, notes: String? = null, progressRate: Int? = null, failureReason: String? = null, reviewedBy: Member? = null): Order {
        return this.copy(
            status = newStatus,
            notes = notes ?: this.notes,
            progressRate = progressRate ?: this.progressRate,
            failureReason = failureReason ?: this.failureReason,
            reviewedAt = if (newStatus != OrderStatus.PENDING) LocalDateTime.now() else this.reviewedAt,
            reviewedBy = reviewedBy ?: this.reviewedBy,
            startDate = if (newStatus == OrderStatus.IN_PROGRESS && this.startDate == null) LocalDate.now() else this.startDate,
            completionDate = if (newStatus == OrderStatus.COMPLETED) LocalDate.now() else this.completionDate,
            failureDate = if (newStatus == OrderStatus.FAILED) LocalDate.now() else this.failureDate,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateInfo(deadline: LocalDate? = null, requirements: String? = null): Order {
        return this.copy(
            deadline = deadline ?: this.deadline,
            requirements = requirements ?: this.requirements,
            updatedAt = LocalDateTime.now()
        )
    }

    fun canBeModified(): Boolean {
        return status in listOf(OrderStatus.PENDING, OrderStatus.PAYMENT_WAITING)
    }

    fun canBeCancelled(): Boolean {
        return status !in listOf(OrderStatus.COMPLETED, OrderStatus.FAILED, OrderStatus.CANCELLED)
    }

    fun getCurrentPayment(): OrderPayment? {
        return payments.maxByOrNull { it.createdAt }
    }
}

enum class OrderStatus {
    PENDING,            // 대기
    PAYMENT_WAITING,    // 입금 대기
    PAYMENT_CONFIRMED,  // 입금 확인
    APPROVED,           // 승인
    IN_PROGRESS,        // 진행 중
    COMPLETED,          // 완료
    FAILED,             // 실패
    CANCELLED           // 취소
}