package com.service.frame.member.repository

import com.service.frame.member.entity.Member
import com.service.frame.member.entity.SettlementAccount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SettlementAccountRepository : JpaRepository<SettlementAccount, Long> {
    
    fun findByMemberId(memberId: Long): List<SettlementAccount>
    
    fun findByMemberAndIsActiveTrue(member: Member): List<SettlementAccount>
    
    fun findByMemberIdAndIsActiveTrue(memberId: Long): List<SettlementAccount>
    
    @Query("SELECT sa FROM SettlementAccount sa WHERE sa.member.id = :memberId AND sa.isDefault = true AND sa.isActive = true")
    fun findDefaultAccountByMemberId(@Param("memberId") memberId: Long): SettlementAccount?
    
    fun findByMemberAndAccountNumber(member: Member, accountNumber: String): SettlementAccount?
    
    fun existsByMemberIdAndAccountNumber(memberId: Long, accountNumber: String): Boolean
}