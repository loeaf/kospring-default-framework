package com.service.frame.revenue.entity

import com.service.frame.member.entity.Member
import com.service.frame.post.entity.AdvertisementAssignment
import com.service.frame.order.entity.Order
import javax.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "revenue_transactions")
class RevenueTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    var transactionType: TransactionType? = null,

    // 매출 관련 (광고 게시 수익)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    var assignment: AdvertisementAssignment? = null,

    @Column(name = "round_number")
    var roundNumber: String? = null,

    @Column(name = "round_category")
    var roundCategory: String? = null,

    @Column(name = "ctr_rate", precision = 5, scale = 2)
    var ctrRate: BigDecimal? = null,

    // 매입 관련 (발주 비용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    var order: Order? = null,

    @Column(name = "order_number")
    var orderNumber: String? = null,

    @Column(name = "order_status")
    var orderStatus: String? = null,

    // 공통 필드
    @Column(nullable = false)
    var title: String? = null,

    @Column(nullable = false, precision = 12, scale = 2)
    var amount: BigDecimal? = null,

    @Column(name = "transaction_date", nullable = false)
    var transactionDate: LocalDate? = null,

    @Column(name = "tax_invoice_issued")
    var taxInvoiceIssued: Boolean = false,

    @Column(name = "tax_invoice_number")
    var taxInvoiceNumber: String? = null,

    var description: String? = null,

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // JPA가 요구하는 기본 생성자
    constructor() : this(
        id = null,
        member = null,
        transactionType = null,
        assignment = null,
        roundNumber = null,
        roundCategory = null,
        ctrRate = null,
        order = null,
        orderNumber = null,
        orderStatus = null,
        title = null,
        amount = null,
        transactionDate = null,
        taxInvoiceIssued = false,
        taxInvoiceNumber = null,
        description = null,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}

enum class TransactionType {
    INCOME,  // 수입 (광고 게시로 받은 수익) - 매입
    EXPENSE  // 지출 (광고 주문 비용) - 매출
}