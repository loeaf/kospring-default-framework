#!/bin/bash

# 라운드별 회원 현황 및 광고 상태 확인 스크립트
# 사용법: ./check_round_members.sh [ROUND_ID]

ROUND_ID=${1:-1}

echo "=== 라운드 $ROUND_ID 회원 및 광고 현황 ==="

echo "1. 라운드 기본 정보"
curl -s -X GET "http://localhost:8080/api/rounds/$ROUND_ID" \
  -H "Content-Type: application/json" | jq '{
    id, title, description, status, 
    startDate, endDate, 
    maxParticipants, 
    orderAmount
  }'

echo -e "\n2. 전체 활성 회원 목록"
curl -s -X GET "http://localhost:8080/api/members/active" \
  -H "Content-Type: application/json" | jq '.[] | {
    id, email, companyName, rentalStatus, currentRentalExpiry
  }'

echo -e "\n3. 라운드별 광고 통계"
curl -s -X GET "http://localhost:8080/api/ad-tasks/rounds/$ROUND_ID/statistics" \
  -H "Content-Type: application/json"

echo -e "\n4. 라운드의 모든 광고 현황 (회원별)"
curl -s -X GET "http://localhost:8080/api/ad-tasks/rounds/$ROUND_ID" \
  -H "Content-Type: application/json" | jq '.ads | group_by(.memberId) | map({
    memberId: .[0].memberId,
    memberEmail: .[0].memberEmail,
    companyName: .[0].memberCompanyName,
    adCount: length,
    statuses: [.[].status] | unique,
    adTypes: [.[].adType] | unique
  })'

echo -e "\n5. 광고가 없는 회원 확인을 위한 API 호출"
echo "POST /api/ad-tasks/rounds/$ROUND_ID/create-missing-ads (DRY RUN 모드라면 좋겠지만, 실제로는 생성됨)"