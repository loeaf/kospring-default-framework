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
    
    // 성능 최적화: fetch join을 사용한 메서드들
    @Query("""
        SELECT rt FROM RevenueTransaction rt 
        LEFT JOIN FETCH rt.member m
        LEFT JOIN FETCH rt.assignment a
        LEFT JOIN FETCH rt.order o
        WHERE rt.member.id = :memberId
        ORDER BY rt.transactionDate DESC
    """)
    fun findByMemberIdWithFetch(@Param("memberId") memberId: Long): List<RevenueTransaction>
    
    @Query("""
        SELECT rt FROM RevenueTransaction rt 
        LEFT JOIN FETCH rt.member m
        LEFT JOIN FETCH rt.assignment a
        LEFT JOIN FETCH rt.order o
        WHERE rt.member.id = :memberId AND rt.transactionType = :transactionType
        ORDER BY rt.transactionDate DESC
    """)
    fun findByMemberIdAndTransactionTypeWithFetch(
        @Param("memberId") memberId: Long, 
        @Param("transactionType") transactionType: TransactionType
    ): List<RevenueTransaction>
    
    // 기간별 집계 쿼리 추가
    @Query("""
        SELECT 
            rt.transactionType,
            COALESCE(SUM(rt.amount), 0) as totalAmount,
            COUNT(rt) as transactionCount
        FROM RevenueTransaction rt 
        WHERE rt.member.id = :memberId 
        AND rt.transactionDate >= :startDate 
        AND rt.transactionDate <= :endDate
        GROUP BY rt.transactionType
    """)
    fun getRevenueStatsByMemberIdAndPeriod(
        @Param("memberId") memberId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Array<Any>>
    
    // 기존 메서드들 (하위 호환성)
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