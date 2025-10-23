package com.service.frame.revenue.repository

import com.service.frame.revenue.entity.RevenueTransaction
import com.service.frame.revenue.entity.TransactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface RevenueTransactionRepository : JpaRepository<RevenueTransaction, Long> {
    
    fun findByMemberIdOrderByTransactionDateDesc(memberId: Long): List<RevenueTransaction>
    
    // RevenueService에서 사용하는 메서드들 추가
    fun findByMemberId(memberId: Long): List<RevenueTransaction>
    
    fun findByMemberIdAndTransactionType(
        memberId: Long, 
        transactionType: TransactionType
    ): List<RevenueTransaction>
    
    fun findByMemberIdAndTransactionTypeOrderByTransactionDateDesc(
        memberId: Long, 
        transactionType: TransactionType
    ): List<RevenueTransaction>
    
    fun findByMemberIdAndTransactionDateBetweenOrderByTransactionDateDesc(
        memberId: Long, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<RevenueTransaction>
    
    // 이전 기간 비교를 위한 메서드
    fun findByTransactionDateBetween(
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<RevenueTransaction>
    
    @Query("SELECT SUM(rt.amount) FROM RevenueTransaction rt WHERE rt.member.id = :memberId AND rt.transactionType = :transactionType")
    fun sumAmountByMemberIdAndTransactionType(
        @Param("memberId") memberId: Long, 
        @Param("transactionType") transactionType: TransactionType
    ): java.math.BigDecimal?
    
    @Query("SELECT SUM(rt.amount) FROM RevenueTransaction rt WHERE rt.member.id = :memberId")
    fun sumTotalAmountByMemberId(@Param("memberId") memberId: Long): java.math.BigDecimal?
    
    // 주문 ID로 revenue transaction 조회
    fun findByOrderId(orderId: Long): List<RevenueTransaction>
    
    // 주문 ID와 거래 유형으로 revenue transaction 조회
    fun findByOrderIdAndTransactionType(orderId: Long, transactionType: TransactionType): List<RevenueTransaction>
}