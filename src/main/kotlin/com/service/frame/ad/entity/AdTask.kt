package com.service.frame.ad.entity

import com.service.frame.member.entity.Member
import com.service.frame.round.entity.Round
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "ad_tasks", 
    uniqueConstraints = [UniqueConstraint(columnNames = ["round_id", "member_id", "ad_index"])]
)
data class AdTask(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    val round: Round = Round(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member = Member(),

    @Column(name = "task_status", nullable = false)
    @Enumerated(EnumType.STRING)
    val status: AdTaskStatus = AdTaskStatus.PENDING,

    @Column(name = "ad_content", columnDefinition = "TEXT")
    val adContent: String? = null,

    @Column(name = "html_file_path")
    val htmlFilePath: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @Column(name = "started_at")
    val startedAt: LocalDateTime? = null,

    @Column(name = "completed_at")
    val completedAt: LocalDateTime? = null,

    @Column(name = "error_message")
    val errorMessage: String? = null,

    @Column(name = "retry_count", nullable = false)
    val retryCount: Int = 0,

    @Column(name = "web_url")
    val webUrl: String? = null,

    @Column(name = "ad_type")
    val adType: String? = null,

    @Column(name = "description")
    val description: String? = null,

    @Column(name = "ad_index")
    val adIndex: Int? = null
)

enum class AdTaskStatus {
    PENDING,     // 대기 중
    PROCESSING,  // 처리 중
    COMPLETED,   // 완료
    FAILED,      // 실패
    RETRY        // 재시도 대기
}