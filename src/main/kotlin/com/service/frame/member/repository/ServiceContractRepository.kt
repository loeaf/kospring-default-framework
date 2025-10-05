package com.service.frame.member.repository

import com.service.frame.member.entity.ServiceContract
import com.service.frame.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceContractRepository : JpaRepository<ServiceContract, Long> {
    fun findByMemberAndIsActive(member: Member, isActive: Boolean = true): ServiceContract?
    fun findByMember(member: Member): List<ServiceContract>
    fun findAllByMember(member: Member): List<ServiceContract>
    fun existsByMemberAndIsActive(member: Member, isActive: Boolean = true): Boolean
}