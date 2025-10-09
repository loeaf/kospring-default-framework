package com.service.frame.ad.service

import com.service.frame.ad.dto.AdTaskMessage
import com.service.frame.ad.entity.AdTask
import com.service.frame.ad.entity.AdTaskStatus
import com.service.frame.ad.repository.AdTaskRepository
import com.service.frame.member.entity.Member
import com.service.frame.round.entity.Round
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AdQueueService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val adTaskRepository: AdTaskRepository,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(AdQueueService::class.java)
    
    companion object {
        const val AD_GENERATION_QUEUE = "ad_generation_queue"
        const val AD_GENERATION_PROCESSING_QUEUE = "ad_generation_processing"
        const val AD_GENERATION_FAILED_QUEUE = "ad_generation_failed"
    }

    /**
     * 라운드와 모든 활성 회원에 대해 광고 생성 작업을 큐에 추가
     */
    fun enqueueAdGenerationTasks(round: Round, members: List<Member>) {
        logger.info("라운드 ${round.id}에 대해 ${members.size}개의 광고 생성 작업을 큐에 추가 시작")
        
        members.forEach { member ->
            try {
                // AdTask 엔티티 생성 및 저장
                val adTask = createAdTask(round, member)
                val savedTask = adTaskRepository.save(adTask)
                
                // Redis 큐에 작업 메시지 추가
                val taskMessage = createAdTaskMessage(savedTask)
                enqueueMessage(AD_GENERATION_QUEUE, taskMessage)
                
                logger.debug("회원 ${member.id}에 대한 광고 생성 작업이 큐에 추가됨 (Task ID: ${savedTask.id})")
                
            } catch (e: Exception) {
                logger.error("회원 ${member.id}에 대한 광고 생성 작업 큐 추가 실패", e)
            }
        }
        
        logger.info("라운드 ${round.id}에 대한 광고 생성 작업 큐 추가 완료")
    }

    /**
     * 단일 광고 생성 작업을 큐에 추가
     */
    fun enqueueAdGenerationTask(round: Round, member: Member): AdTask {
        logger.info("라운드 ${round.id}, 회원 ${member.id}에 대한 광고 생성 작업을 큐에 추가")
        
        val adTask = createAdTask(round, member)
        val savedTask = adTaskRepository.save(adTask)
        
        val taskMessage = createAdTaskMessage(savedTask)
        enqueueMessage(AD_GENERATION_QUEUE, taskMessage)
        
        logger.info("광고 생성 작업이 큐에 추가됨 (Task ID: ${savedTask.id})")
        return savedTask
    }

    /**
     * 실패한 작업을 재시도 큐에 추가
     */
    fun requeueFailedTask(taskId: Long, errorMessage: String) {
        logger.info("실패한 작업 재시도 큐 추가: Task ID $taskId")
        
        val task = adTaskRepository.findById(taskId).orElse(null)
        if (task == null) {
            logger.error("작업을 찾을 수 없음: Task ID $taskId")
            return
        }
        
        val updatedTask = task.copy(
            status = AdTaskStatus.RETRY,
            errorMessage = errorMessage,
            retryCount = task.retryCount + 1
        )
        adTaskRepository.save(updatedTask)
        
        // 재시도 횟수가 3회 미만인 경우에만 다시 큐에 추가
        if (updatedTask.retryCount < 3) {
            val taskMessage = createAdTaskMessage(updatedTask)
            enqueueMessage(AD_GENERATION_QUEUE, taskMessage)
            logger.info("작업이 재시도 큐에 추가됨 (Task ID: $taskId, 시도 횟수: ${updatedTask.retryCount})")
        } else {
            logger.warn("작업이 최대 재시도 횟수를 초과함 (Task ID: $taskId)")
            val failedTask = updatedTask.copy(status = AdTaskStatus.FAILED)
            adTaskRepository.save(failedTask)
        }
    }

    /**
     * 작업 상태 업데이트
     */
    fun updateTaskStatus(taskId: Long, status: AdTaskStatus, htmlFilePath: String? = null, errorMessage: String? = null) {
        val task = adTaskRepository.findById(taskId).orElse(null)
        if (task == null) {
            logger.error("작업을 찾을 수 없음: Task ID $taskId")
            return
        }
        
        val now = java.time.LocalDateTime.now()
        val updatedTask = task.copy(
            status = status,
            htmlFilePath = htmlFilePath,
            errorMessage = errorMessage,
            updatedAt = now,
            startedAt = if (status == AdTaskStatus.PROCESSING) now else task.startedAt,
            completedAt = if (status == AdTaskStatus.COMPLETED || status == AdTaskStatus.FAILED) now else task.completedAt
        )
        
        adTaskRepository.save(updatedTask)
        logger.info("작업 상태 업데이트: Task ID $taskId, Status: $status")
    }

    /**
     * 라운드별 작업 통계 조회
     */
    @Transactional(readOnly = true)
    fun getRoundTaskStatistics(round: Round): Map<AdTaskStatus, Long> {
        return AdTaskStatus.values().associateWith { status ->
            adTaskRepository.countByRoundAndStatus(round, status)
        }
    }

    /**
     * 수동으로 PENDING 상태의 작업들을 큐에 추가
     */
    fun manualEnqueuePendingTasks(round: Round): Int {
        logger.info("라운드 ${round.id}의 PENDING 작업들을 수동으로 큐에 추가 시작")
        
        val pendingTasks = adTaskRepository.findByRoundAndStatus(round, AdTaskStatus.PENDING)
        logger.info("발견된 PENDING 작업 수: ${pendingTasks.size}")
        
        var enqueuedCount = 0
        pendingTasks.forEach { task ->
            try {
                val taskMessage = createAdTaskMessage(task)
                enqueueMessage(AD_GENERATION_QUEUE, taskMessage)
                enqueuedCount++
                logger.info("Task ID ${task.id}를 큐에 추가 성공")
            } catch (e: Exception) {
                logger.error("Task ID ${task.id} 큐 추가 실패", e)
            }
        }
        
        logger.info("라운드 ${round.id}의 PENDING 작업 큐 추가 완료: $enqueuedCount/${pendingTasks.size}")
        return enqueuedCount
    }

    private fun createAdTask(round: Round, member: Member): AdTask {
        return AdTask(
            round = round,
            member = member,
            status = AdTaskStatus.PENDING
        )
    }

    private fun createAdTaskMessage(adTask: AdTask): AdTaskMessage {
        return AdTaskMessage(
            taskId = adTask.id!!,
            roundId = adTask.round.id!!,
            memberId = adTask.member.id!!,
            roundInfo = com.service.frame.ad.dto.RoundInfo(
                id = adTask.round.id!!,
                title = adTask.round.title,
                description = adTask.round.description,
                category = adTask.round.category,
                orderAmount = adTask.round.orderAmount,
                templateCost = adTask.round.templateCost,
                aiGenerationCost = adTask.round.aiGenerationCost,
                targetingPostingCost = adTask.round.targetingPostingCost,
                serverRentalCost = adTask.round.serverRentalCost,
                otherCosts = adTask.round.otherCosts,
                startDate = adTask.round.startDate,
                endDate = adTask.round.endDate,
                maxParticipants = adTask.round.maxParticipants
            ),
            memberInfo = com.service.frame.ad.dto.MemberInfo(
                id = adTask.member.id!!,
                email = adTask.member.email,
                companyName = adTask.member.companyName,
                businessRegistrationNumber = adTask.member.businessRegistrationNumber,
                contactNumber = adTask.member.contactNumber
            )
        )
    }

    private fun enqueueMessage(queueName: String, message: AdTaskMessage) {
        try {
            logger.info("큐에 메시지 추가 시도: $queueName, Task ID: ${message.taskId}")
            
            // Python ad_generator가 기대하는 전체 구조로 변경
            val fullMessage = mapOf(
                "taskId" to message.taskId,
                "roundId" to message.roundId,
                "memberId" to message.memberId,
                "roundInfo" to mapOf(
                    "id" to message.roundInfo.id,
                    "title" to message.roundInfo.title,
                    "description" to (message.roundInfo.description ?: ""),
                    "category" to (message.roundInfo.category ?: ""),
                    "orderAmount" to message.roundInfo.orderAmount,
                    "startDate" to message.roundInfo.startDate.toString(),
                    "endDate" to message.roundInfo.endDate.toString(),
                    "maxParticipants" to message.roundInfo.maxParticipants
                ),
                "memberInfo" to mapOf(
                    "id" to message.memberInfo.id,
                    "email" to message.memberInfo.email,
                    "companyName" to (message.memberInfo.companyName ?: ""),
                    "contactNumber" to (message.memberInfo.contactNumber ?: ""),
                    "businessRegistrationNumber" to (message.memberInfo.businessRegistrationNumber ?: "")
                ),
                "timestamp" to System.currentTimeMillis()
            )
            
            // JSON 직렬화
            val jsonMessage = objectMapper.writeValueAsString(fullMessage)
            logger.info("JSON 직렬화 성공: $jsonMessage")
            
            // Redis에 직접 추가 (트랜잭션 외부에서)
            val result = redisTemplate.execute { connection ->
                connection.listCommands().lPush(queueName.toByteArray(), jsonMessage.toByteArray())
            }
            
            logger.info("큐에 메시지 추가 성공: $queueName, 결과: $result")
            
            // 큐 길이 확인
            val queueLength = redisTemplate.opsForList().size(queueName)
            logger.info("큐 $queueName 현재 길이: $queueLength")
            
        } catch (e: Exception) {
            logger.error("큐에 메시지 추가 실패: $queueName, Task ID: ${message.taskId}", e)
            logger.error("오류 상세: ${e.message}")
            logger.error("오류 타입: ${e.javaClass.simpleName}")
            // 큐 추가 실패가 전체 프로세스를 중단시키지 않도록 예외를 다시 던지지 않음
            logger.warn("큐 추가 실패했지만 프로세스 계속 진행")
        }
    }
}