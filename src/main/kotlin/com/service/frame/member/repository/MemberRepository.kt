package com.service.frame.member.repository

import com.service.frame.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun existsByEmail(email: String): Boolean
    fun existsByBusinessRegistrationNumber(businessRegistrationNumber: String): Boolean
    fun findByEmail(email: String): Member?
}