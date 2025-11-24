package com.service.frame.member.entity

import javax.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "settlement_accounts")
class SettlementAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member? = null,
    
    @Column(name = "bank_code", nullable = false, length = 20)
    val bankCode: String = "",
    
    @Column(name = "bank_name", nullable = false, length = 100)
    val bankName: String = "",
    
    @Column(name = "account_number", nullable = false, length = 50)
    val accountNumber: String = "",
    
    @Column(name = "account_holder", nullable = false, length = 100)
    val accountHolder: String = "",
    
    @Column(name = "is_default", nullable = false)
    val isDefault: Boolean = false,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(
        id = null,
        member = null,
        bankCode = "",
        bankName = "",
        accountNumber = "",
        accountHolder = "",
        isDefault = false,
        isActive = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
    
    fun copy(
        id: Long? = this.id,
        member: Member? = this.member,
        bankCode: String = this.bankCode,
        bankName: String = this.bankName,
        accountNumber: String = this.accountNumber,
        accountHolder: String = this.accountHolder,
        isDefault: Boolean = this.isDefault,
        isActive: Boolean = this.isActive,
        createdAt: LocalDateTime = this.createdAt,
        updatedAt: LocalDateTime = LocalDateTime.now()
    ) = SettlementAccount(
        id = id,
        member = member,
        bankCode = bankCode,
        bankName = bankName,
        accountNumber = accountNumber,
        accountHolder = accountHolder,
        isDefault = isDefault,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}