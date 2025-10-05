package com.service.frame.round.service

import com.service.frame.round.entity.RoundStatus
import com.service.frame.round.repository.RoundRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class RoundScheduledService(
    private val roundRepository: RoundRepository
) {
    private val logger = LoggerFactory.getLogger(RoundScheduledService::class.java)

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    fun updateExpiredRounds() {
        val koreaTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        
        try {
            val expiredRounds = roundRepository.findExpiredActiveRounds(koreaTime)
            
            if (expiredRounds.isNotEmpty()) {
                logger.info("Found ${expiredRounds.size} expired rounds to update")
                
                expiredRounds.forEach { round ->
                    logger.info("Updating round ${round.roundNumber} from ${round.status} to CLOSED")
                }
                
                val updatedCount = roundRepository.updateExpiredRoundsToClose(koreaTime)
                logger.info("Updated $updatedCount rounds to CLOSED status")
            }
        } catch (e: Exception) {
            logger.error("Error updating expired rounds: ${e.message}", e)
        }
    }
}