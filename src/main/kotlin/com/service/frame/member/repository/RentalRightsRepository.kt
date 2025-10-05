package com.service.frame.member.repository

import com.service.frame.member.entity.RentalRights
import com.service.frame.member.entity.RentalRightsStatus
import com.service.frame.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface RentalRightsRepository : JpaRepository<RentalRights, Long> {
    fun findByMemberAndStatus(member: Member, status: RentalRightsStatus): List<RentalRights>
    
    @Query("SELECT r FROM RentalRights r WHERE r.member = :member AND r.status = 'ACTIVE' AND r.expiryDate > :currentDate")
    fun findActiveRentalRights(member: Member, currentDate: LocalDate = LocalDate.now()): List<RentalRights>
    
    fun findByMember(member: Member): List<RentalRights>
    
    @Query("SELECT r FROM RentalRights r WHERE r.expiryDate BETWEEN :startDate AND :endDate AND r.status = 'ACTIVE'")
    fun findExpiringRentalRights(startDate: LocalDate, endDate: LocalDate): List<RentalRights>
}