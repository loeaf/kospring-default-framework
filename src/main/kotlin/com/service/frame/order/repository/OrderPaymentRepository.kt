package com.service.frame.order.repository

import com.service.frame.order.entity.OrderPayment
import com.service.frame.order.entity.PaymentStatus
import com.service.frame.order.entity.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OrderPaymentRepository : JpaRepository<OrderPayment, Long> {
    
    fun findByApplicationNumber(applicationNumber: String): OrderPayment?
    
    fun findByOrder(order: Order): List<OrderPayment>
    
    fun findByPaymentStatus(paymentStatus: PaymentStatus): List<OrderPayment>
    
    @Query("SELECT op FROM OrderPayment op WHERE op.order.id = :orderId ORDER BY op.createdAt DESC")
    fun findByOrderIdOrderByCreatedAtDesc(@Param("orderId") orderId: Long): List<OrderPayment>
    
    @Query("SELECT op FROM OrderPayment op WHERE op.order.id = :orderId")
    fun findByOrderId(@Param("orderId") orderId: Long): OrderPayment?
    
    @Query("SELECT op FROM OrderPayment op WHERE op.order.member.id = :memberId ORDER BY op.createdAt DESC")
    fun findByMemberIdOrderByCreatedAtDesc(@Param("memberId") memberId: Long): List<OrderPayment>
    
    @Query("SELECT op FROM OrderPayment op WHERE op.paymentStatus = :status ORDER BY op.createdAt ASC")
    fun findByPaymentStatusOrderByCreatedAtAsc(@Param("status") status: PaymentStatus): List<OrderPayment>
    
    @Query("SELECT COUNT(op) FROM OrderPayment op WHERE DATE(op.createdAt) = CURRENT_DATE")
    fun countTodayPayments(): Long
    
    @Query("""
        SELECT op 
        FROM OrderPayment op
        JOIN op.order o
        JOIN o.adTask at
        WHERE at.round.id = :roundId
        AND op.paymentStatus = 'WAITING'
    """)
    fun findPaymentsToConfirmByRound(@Param("roundId") roundId: Long): List<OrderPayment>

    @Query("""
        SELECT op 
        FROM OrderPayment op
        JOIN op.order o
        JOIN o.adTask at
        JOIN at.member m
        WHERE at.round.id = :roundId
        AND m.id = :memberId
        AND op.paymentStatus = 'WAITING'
    """)
    fun findPaymentsToConfirmByRoundAndMember(
        @Param("roundId") roundId: Long, 
        @Param("memberId") memberId: Long
    ): List<OrderPayment>
}