package com.service.frame.order.repository

import com.service.frame.order.entity.Order
import com.service.frame.order.entity.OrderStatus
import com.service.frame.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    
    fun findByOrderNumber(orderNumber: String): Order?
    
    fun findByMember(member: Member): List<Order>
    
    fun findByStatus(status: OrderStatus): List<Order>
    
    @Query("SELECT o FROM Order o WHERE o.member.id = :memberId ORDER BY o.submittedAt DESC")
    fun findByMemberIdOrderBySubmittedAtDesc(@Param("memberId") memberId: Long): List<Order>
    
    @Query("""
        SELECT o FROM Order o 
        JOIN FETCH o.adTask at
        JOIN FETCH at.round r
        JOIN FETCH o.member m
        LEFT JOIN FETCH o.payments p
        WHERE o.member.id = :memberId 
        ORDER BY o.submittedAt DESC
    """)
    fun findByMemberIdWithDetailsOrderBySubmittedAtDesc(@Param("memberId") memberId: Long): List<Order>
    
    @Query("SELECT o FROM Order o WHERE o.adTask.id = :adTaskId")
    fun findByAdTaskId(@Param("adTaskId") adTaskId: Long): Order?
    
    @Query("SELECT o FROM Order o WHERE o.adTask.id IN :adTaskIds")
    fun findByAdTaskIdIn(@Param("adTaskIds") adTaskIds: List<Long>): List<Order>
    
    
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.submittedAt DESC")
    fun findByStatusInOrderBySubmittedAtDesc(@Param("statuses") statuses: List<OrderStatus>): List<Order>
    
    // 통계를 위한 추가 쿼리들
    @Query("SELECT COUNT(o) FROM Order o WHERE o.member.id = :memberId")
    fun countByMemberId(@Param("memberId") memberId: Long): Long
    
    @Query("SELECT COUNT(DISTINCT o.adTask.round.id) FROM Order o WHERE o.member.id = :memberId")
    fun countDistinctRoundsByMemberId(@Param("memberId") memberId: Long): Long
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.member.id = :memberId AND o.status = :status")
    fun countByMemberIdAndStatus(@Param("memberId") memberId: Long, @Param("status") status: OrderStatus): Long
    
    @Query("SELECT o FROM Order o WHERE o.member.id = :memberId AND o.createdAt >= :startDate AND o.createdAt < :endDate ORDER BY o.createdAt DESC")
    fun findByMemberIdAndCreatedAtBetween(
        @Param("memberId") memberId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Order>
    
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt < :endDate ORDER BY o.createdAt DESC")
    fun findByCreatedAtBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Order>
    
    @Query("""
        SELECT new map(
            o.status as status,
            COUNT(o) as count
        )
        FROM Order o 
        WHERE o.member.id = :memberId
        GROUP BY o.status
    """)
    fun getStatusDistributionByMember(@Param("memberId") memberId: Long): List<Map<String, Any>>
    
    @Query("""
        SELECT new map(
            YEAR(o.createdAt) as year,
            MONTH(o.createdAt) as month,
            COUNT(o) as totalOrders,
            COUNT(CASE WHEN o.status = 'COMPLETED' THEN 1 END) as completedOrders
        )
        FROM Order o 
        WHERE o.member.id = :memberId 
            AND o.createdAt >= :startDate 
            AND o.createdAt < :endDate
        GROUP BY YEAR(o.createdAt), MONTH(o.createdAt)
        ORDER BY YEAR(o.createdAt), MONTH(o.createdAt)
    """)
    fun getMonthlyTrendByMember(
        @Param("memberId") memberId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Map<String, Any>>
    
    @Query("""
        SELECT new map(
            YEAR(o.createdAt) as year,
            MONTH(o.createdAt) as month,
            DAY(o.createdAt) as day,
            COUNT(o) as newOrders,
            COUNT(CASE WHEN o.status = 'COMPLETED' THEN 1 END) as completedOrders
        )
        FROM Order o 
        WHERE o.createdAt >= :startDate AND o.createdAt < :endDate
        GROUP BY YEAR(o.createdAt), MONTH(o.createdAt), DAY(o.createdAt)
        ORDER BY YEAR(o.createdAt), MONTH(o.createdAt), DAY(o.createdAt)
    """)
    fun getDailyOrderStats(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Map<String, Any>>
    
    @Query("""
        SELECT CASE 
            WHEN COUNT(aa) = 0 THEN false
            WHEN COUNT(ap) = 0 THEN false
            WHEN COUNT(ap) = COUNT(CASE WHEN ap.postStatus = 'PUBLISHED' THEN 1 END) THEN true
            ELSE false
        END
        FROM AdvertisementAssignment aa
        LEFT JOIN AdvertisementPost ap ON aa.id = ap.assignment.id
        WHERE aa.round.id = :roundId
    """)
    fun checkAllPostsPublishedInRound(@Param("roundId") roundId: Long): Boolean
    
    @Query("""
        SELECT CASE 
            WHEN COUNT(aa) = 0 THEN 0
            WHEN COUNT(ap) = 0 THEN 0
            ELSE ROUND((COUNT(CASE WHEN ap.postStatus = 'PUBLISHED' THEN 1 END) * 100.0) / COUNT(aa), 0)
        END
        FROM AdvertisementAssignment aa
        LEFT JOIN AdvertisementPost ap ON aa.id = ap.assignment.id
        WHERE aa.adTask.id = :adTaskId
    """)
    fun calculatePostProgressForAdTask(@Param("adTaskId") adTaskId: Long): Int
    
    @Query("""
        SELECT new map(
            m.id as memberId,
            m.companyName as companyName,
            o.submittedAt as orderDate,
            o.status as orderStatus
        )
        FROM Order o
        JOIN o.member m
        JOIN o.adTask at
        WHERE at.round.id = :roundId
        ORDER BY o.submittedAt
    """)
    fun findRoundParticipants(@Param("roundId") roundId: Long): List<Map<String, Any>>
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.adTask.round.id = :roundId")
    fun countByRoundId(@Param("roundId") roundId: Long): Long
}