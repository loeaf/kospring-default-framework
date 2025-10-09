package com.service.frame.member.repository

import com.service.frame.member.entity.Member
import com.service.frame.member.entity.RentalStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun existsByEmail(email: String): Boolean
    fun existsByBusinessRegistrationNumber(businessRegistrationNumber: String): Boolean
    fun findByEmail(email: String): Member?
    
    // 활성 임대권을 가진 회원들 조회
    fun findByRentalStatus(rentalStatus: RentalStatus): List<Member>
    
    @Query("SELECT m FROM Member m WHERE m.rentalStatus = 'ACTIVE' AND m.currentRentalExpiry > CURRENT_DATE")
    fun findActiveMembers(): List<Member>
    
    // 프리미엄 활성 회원들만 조회
    @Query("SELECT m FROM Member m WHERE m.rentalStatus = 'ACTIVE' AND m.currentRentalExpiry > CURRENT_DATE AND m.isPremium = true")
    fun findActivePremiumMembers(): List<Member>
}