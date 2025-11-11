#!/bin/bash

# 기존 라운드에서 광고를 만들지 않은 신규 회원들을 위한 광고 생성 스크립트
# 사용법: ./update_round_ads.sh [ROUND_ID]

ROUND_ID=${1:-1}  # 첫 번째 파라미터로 라운드 ID 받기, 기본값은 1

echo "=== 라운드 $ROUND_ID 신규 회원 광고 생성 ==="

echo "1. 현재 라운드 상태 확인..."
curl -s -X GET "http://localhost:8080/api/rounds/$ROUND_ID" \
  -H "Content-Type: application/json" | jq '.title, .description, .status'

echo -e "\n2. 현재 라운드의 기존 광고 통계 확인..."
curl -s -X GET "http://localhost:8080/api/ad-tasks/rounds/$ROUND_ID/statistics" \
  -H "Content-Type: application/json" | jq '.'

echo -e "\n3. 신규 회원들을 위한 누락된 광고 생성 중..."
response=$(curl -s -X POST "http://localhost:8080/api/ad-tasks/rounds/$ROUND_ID/create-missing-ads" \
  -H "Content-Type: application/json")

echo "$response" | jq '.'

# 생성된 광고 수 확인
new_members_count=$(echo "$response" | jq -r '.newMembersCount // 0')
total_ads_created=$(echo "$response" | jq -r '.totalAdTasksCreated // 0')

echo -e "\n=== 결과 요약 ==="
echo "라운드 ID: $ROUND_ID"
echo "신규 회원 수: $new_members_count"
echo "생성된 광고 수: $total_ads_created"

if [ "$new_members_count" -gt 0 ]; then
    echo -e "\n4. 업데이트된 라운드 광고 통계 확인..."
    sleep 2  # 잠시 대기
    curl -s -X GET "http://localhost:8080/api/ad-tasks/rounds/$ROUND_ID/statistics" \
      -H "Content-Type: application/json" | jq '.'
    
    echo -e "\n5. PENDING 광고들을 큐에 추가 (광고 생성 시작)..."
    curl -s -X POST "http://localhost:8080/api/ad-tasks/rounds/$ROUND_ID/enqueue-pending" \
      -H "Content-Type: application/json" | jq '.'
      
    echo -e "\n=== 완료 ==="
    echo "신규 회원 $new_members_count명에 대해 $total_ads_created개의 광고가 생성되었고,"
    echo "PENDING 상태의 광고들이 처리 큐에 추가되었습니다."
    echo "Python 워커가 이 광고들을 자동으로 처리할 예정입니다."
else
    echo -e "\n=== 완료 ==="
    echo "신규 회원이 없어서 추가로 생성할 광고가 없습니다."
    echo "모든 활성 회원들이 이미 이 라운드의 광고를 가지고 있습니다."
fi

echo -e "\n6. 라운드 $ROUND_ID의 모든 광고 목록 확인..."
curl -s -X GET "http://localhost:8080/api/ad-tasks/rounds/$ROUND_ID" \
  -H "Content-Type: application/json" | jq '.ads[] | {id, memberId, memberCompanyName, status, adType}'