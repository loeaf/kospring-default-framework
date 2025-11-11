#!/bin/bash

# 라운드 전체 워크플로우 스크립트
# 라운드 생성 → 기존 회원 광고 생성 → 신규 회원 확인 및 광고 생성

echo "=== CNC 라운드 전체 워크플로우 ==="

# 1. 라운드 생성
echo "1단계: 새로운 라운드 생성"
echo "실행: ./1_create-round.sh"
./1_create-round.sh

echo -e "\n새로 생성된 라운드 ID를 확인하세요."
read -p "생성된 라운드 ID를 입력하세요: " ROUND_ID

if [[ ! "$ROUND_ID" =~ ^[0-9]+$ ]]; then
    echo "오류: 유효한 라운드 ID를 입력해주세요."
    exit 1
fi

# 2. 기존 회원들 광고 생성
echo -e "\n2단계: 기존 회원들을 위한 광고 생성"
echo "실행: ./2_create_ad_tasks.sh (라운드 ID: $ROUND_ID)"
# 2_create_ad_tasks.sh를 동적으로 수정
sed "s/\"roundId\": [0-9]*/\"roundId\": $ROUND_ID/" 2_create_ad_tasks.sh > temp_create_ad_tasks.sh
chmod +x temp_create_ad_tasks.sh
./temp_create_ad_tasks.sh
rm temp_create_ad_tasks.sh

# 3. 현재 상황 확인
echo -e "\n3단계: 현재 라운드 상황 확인"
./check_round_members.sh $ROUND_ID

# 4. 신규 회원 광고 생성 (있다면)
echo -e "\n4단계: 신규 회원 확인 및 광고 생성"
./update_round_ads.sh $ROUND_ID

# 5. 최종 확인
echo -e "\n5단계: 최종 상태 확인"
echo "라운드 $ROUND_ID의 최종 광고 통계:"
curl -s -X GET "http://localhost:8080/api/ad-tasks/rounds/$ROUND_ID/statistics" \
  -H "Content-Type: application/json" | jq '.'

echo -e "\n=== 워크플로우 완료 ==="
echo "라운드 ID: $ROUND_ID"
echo "다음 단계:"
echo "1. Python 워커가 광고를 처리할 때까지 대기"
echo "2. 주문 생성: ./3_create-order.sh"
echo "3. 포스트 생성: ./4_create-post.sh"
echo ""
echo "신규 회원이 추가로 가입하면 다음 명령으로 광고 추가:"
echo "./update_round_ads.sh $ROUND_ID"