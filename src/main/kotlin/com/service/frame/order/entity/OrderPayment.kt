package com.service.frame.order.entity

import com.service.frame.member.entity.Member
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "order_payments")
data class OrderPayment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order = Order(),

    @Column(name = "application_number", unique = true, nullable = false, length = 20)
    val applicationNumber: String = "",

    @Column(name = "payment_amount", nullable = false, precision = 12, scale = 2)
    val paymentAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "depositor_name", length = 100)
    val depositorName: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    val paymentStatus: PaymentStatus = PaymentStatus.WAITING,

    @Column(name = "payment_confirmed_at")
    val paymentConfirmedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_confirmed_by")
    val paymentConfirmedBy: Member? = null,

    @Column(name = "bank_account_number", nullable = false, length = 50)
    val bankAccountNumber: String = "",

    @Column(name = "bank_name", nullable = false, length = 100)
    val bankName: String = "",

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentStatus {
    WAITING,    // 입금 대기
    CONFIRMED,  // 입금 확인
    FAILED      // 입금 실패
}