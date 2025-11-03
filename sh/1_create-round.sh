echo "=== 라운드 생성 ==="
START_DATE=$(date -v+1m '+%Y-%m-%dT09:00:00')
END_DATE=$(date -v+2m '+%Y-%m-%dT18:00:00')

curl -X POST http://localhost:8080/api/rounds?createdById=1 \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"도현이 부자만들기 찬성 라운드 #2\",
    \"description\": \"도현이 부자 만들기 라운드를 신청해주세요.\",
    \"category\": \"회사소개\",
    \"orderAmount\": 10000000,
    \"templateCost\": 0,
    \"aiGenerationCost\": 0,
    \"targetingPostingCost\": 0,
    \"serverRentalCost\": 0,
    \"otherCosts\": 0,
    \"startDate\": \"$START_DATE\",
    \"endDate\": \"$END_DATE\",
    \"postDurationDays\": 3,
    \"maxParticipants\": 5
  }"

echo -e "\n\n"

echo "=== 활성 라운드 목록 조회 ==="
curl -X GET http://localhost:8080/api/rounds?status=ACTIVE \
  -H "Content-Type: application/json"

curl -X GET "http://localhost:8080/api/orders/round/1/keys" \
  -H "Content-Type: application/json"