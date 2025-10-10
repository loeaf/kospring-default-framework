package com.service.frame.post.service

import com.service.frame.ad.entity.AdTask
import com.service.frame.member.entity.Member
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class CircularAssignmentService {

    data class Assignment(
        val author: Member,
        val targetAdTask: AdTask,
        val assignedCost: BigDecimal
    )

    data class CircularSolution(
        val assignments: List<Assignment>,
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun calculateCircularAssignments(
        roundParticipants: List<Member>,
        adTasks: List<AdTask>
    ): CircularSolution {
        
        if (roundParticipants.size != adTasks.size) {
            return CircularSolution(
                assignments = emptyList(),
                isValid = false,
                errorMessage = "참여자 수와 광고 수가 일치하지 않습니다."
            )
        }

        if (roundParticipants.size < 2) {
            return CircularSolution(
                assignments = emptyList(),
                isValid = false,
                errorMessage = "최소 2명 이상의 참여자가 필요합니다."
            )
        }

        try {
            // 투자금액 정보 수집 (Order 테이블에서 가져와야 함)
            val investments = roundParticipants.associateWith { member ->
                // 실제로는 Order나 다른 테이블에서 투자금액을 가져와야 함
                // 여기서는 임시로 AdTask의 금액을 사용
                val memberAdTask = adTasks.find { it.member.id == member.id }
                memberAdTask?.let { getAdTaskCost(it) } ?: BigDecimal.ZERO
            }

            val solution = solveCircularAssignment(investments)
            
            if (solution == null) {
                return CircularSolution(
                    assignments = emptyList(),
                    isValid = false,
                    errorMessage = "순환발주 해를 찾을 수 없습니다."
                )
            }

            // Assignment 객체들 생성
            val assignments = mutableListOf<Assignment>()
            
            for (author in roundParticipants) {
                for (targetAd in adTasks) {
                    if (author.id != targetAd.member.id) {
                        // solution은 Member ID -> Member ID 매핑이므로, targetAd.member.id를 사용해야 함
                        val cost = solution[author.id]?.get(targetAd.member.id) ?: BigDecimal.ZERO
                        if (cost > BigDecimal.ZERO) {
                            assignments.add(
                                Assignment(
                                    author = author,
                                    targetAdTask = targetAd,
                                    assignedCost = cost
                                )
                            )
                        }
                    }
                }
            }

            return CircularSolution(
                assignments = assignments,
                isValid = true
            )

        } catch (e: Exception) {
            return CircularSolution(
                assignments = emptyList(),
                isValid = false,
                errorMessage = "계산 중 오류 발생: ${e.message}"
            )
        }
    }

    private fun getAdTaskCost(adTask: AdTask): BigDecimal {
        // Round의 orderAmount를 투자금액으로 사용
        // 이는 각 참여자가 해당 라운드에 투자한 금액을 의미
        return adTask.round?.orderAmount ?: BigDecimal.ZERO
    }

    private fun solveCircularAssignment(
        investments: Map<Member, BigDecimal>
    ): Map<Long, Map<Long, BigDecimal>>? {
        
        val people = investments.keys.toList()
        val n = people.size

        when {
            n == 2 -> return solve2PersonSystem(investments)
            n == 3 -> return solve3PersonSystem(investments)
            n >= 4 -> return solveIterativeSystem(investments)
            else -> return null
        }
    }

    private fun solve2PersonSystem(
        investments: Map<Member, BigDecimal>
    ): Map<Long, Map<Long, BigDecimal>>? {
        
        val people = investments.keys.toList()
        if (people.size != 2) return null

        val person1 = people[0]
        val person2 = people[1]
        val amount1 = investments[person1] ?: BigDecimal.ZERO
        val amount2 = investments[person2] ?: BigDecimal.ZERO

        return mapOf(
            person1.id!! to mapOf(person2.id!! to amount1),
            person2.id!! to mapOf(person1.id!! to amount2)
        )
    }

    private fun solve3PersonSystem(
        investments: Map<Member, BigDecimal>
    ): Map<Long, Map<Long, BigDecimal>>? {
        
        val people = investments.keys.toList()
        if (people.size != 3) return null

        val A = people[0]
        val B = people[1] 
        val C = people[2]
        val a = investments[A] ?: BigDecimal.ZERO
        val b = investments[B] ?: BigDecimal.ZERO
        val c = investments[C] ?: BigDecimal.ZERO

        // 3명 시스템의 정확한 해
        // Python 코드의 solve_3_person_system 로직 구현
        // x_AB = 0, x_AC = a
        // x_BA = 0, x_BC = b
        // x_CA = a, x_CB = b
        
        val xAB = BigDecimal.ZERO
        val xAC = a
        val xBA = BigDecimal.ZERO
        val xBC = b
        val xCA = a
        val xCB = b

        // 제약조건 확인
        val constraint1 = (xAB + xAC == a) // A가 받는 총액
        val constraint2 = (xBA + xBC == b) // B가 받는 총액
        val constraint3 = (xCA + xCB == c) // C가 받는 총액
        val constraint4 = (xBA + xCA == a) // A발주 분할
        val constraint5 = (xAB + xCB == b) // B발주 분할
        val constraint6 = (xAC + xBC == c) // C발주 분할

        if (constraint1 && constraint2 && constraint3 && constraint4 && constraint5 && constraint6) {
            return mapOf(
                A.id!! to mapOf(B.id!! to xAB, C.id!! to xAC),
                B.id!! to mapOf(A.id!! to xBA, C.id!! to xBC),
                C.id!! to mapOf(A.id!! to xCA, B.id!! to xCB)
            )
        }
        
        return null
    }

    private fun solveIterativeSystem(
        investments: Map<Member, BigDecimal>
    ): Map<Long, Map<Long, BigDecimal>>? {
        
        val people = investments.keys.toList()
        val n = people.size
        val solution = mutableMapOf<Long, MutableMap<Long, BigDecimal>>()

        // 초기화
        for (person in people) {
            solution[person.id!!] = mutableMapOf()
            for (other in people) {
                if (person.id != other.id) {
                    solution[person.id]!![other.id!!] = BigDecimal.ZERO
                }
            }
        }

        val maxIterations = 1000
        val tolerance = BigDecimal("0.01")
        
        // Python 코드의 반복적 해법을 Kotlin으로 구현
        // 각 사람이 받아야 할 금액 = 투자금액이 되도록 조정

        for (iteration in 0 until maxIterations) {
            var converged = true

            // 각 사람이 받아야 할 총액과 현재 받는 총액 비교
            for (person in people) {
                val target = investments[person] ?: BigDecimal.ZERO
                var current = BigDecimal.ZERO

                // 현재 이 사람이 받는 총액 계산
                for (other in people) {
                    if (person.id != other.id) {
                        current = current.add(solution[person.id!!]?.get(other.id!!) ?: BigDecimal.ZERO)
                    }
                }

                val diff = target.subtract(current)
                if (diff.abs() > tolerance) {
                    converged = false

                    // 조정
                    val others = people.filter { it.id != person.id }
                    val totalOthersInvestment = others.sumOf { investments[it] ?: BigDecimal.ZERO }

                    if (totalOthersInvestment > BigDecimal.ZERO) {
                        for (other in others) {
                            val otherInvestment = investments[other] ?: BigDecimal.ZERO
                            val ratio = otherInvestment.divide(totalOthersInvestment, 10, RoundingMode.HALF_UP)
                            val adjustment = diff.multiply(ratio).multiply(BigDecimal("0.1"))

                            val currentAmount = solution[person.id!!]?.get(other.id!!) ?: BigDecimal.ZERO
                            val newAmount = currentAmount.add(adjustment).max(BigDecimal.ZERO)
                            solution[person.id!!]!![other.id!!] = newAmount
                        }
                    }
                }
            }

            // 발주 분할 제약조건 맞추기
            for (orderer in people) {
                var currentSplit = BigDecimal.ZERO
                for (executor in people) {
                    if (executor.id != orderer.id) {
                        currentSplit = currentSplit.add(solution[executor.id!!]?.get(orderer.id!!) ?: BigDecimal.ZERO)
                    }
                }

                val target = investments[orderer] ?: BigDecimal.ZERO
                if (currentSplit > BigDecimal.ZERO && (currentSplit.subtract(target)).abs() > tolerance) {
                    val ratio = target.divide(currentSplit, 10, RoundingMode.HALF_UP)
                    
                    for (executor in people) {
                        if (executor.id != orderer.id) {
                            val current = solution[executor.id!!]?.get(orderer.id!!) ?: BigDecimal.ZERO
                            val adjusted = current.multiply(ratio)
                            solution[executor.id!!]!![orderer.id!!] = adjusted
                        }
                    }
                }
            }

            if (converged) {
                break
            }
        }

        return solution.mapValues { (_, innerMap) ->
            innerMap.toMap()
        }
    }
}