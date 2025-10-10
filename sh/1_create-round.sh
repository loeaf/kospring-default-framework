START_DATE=$(date -v+1m '+%Y-%m-%dT09:00:00')
END_DATE=$(date -v+2m '+%Y-%m-%dT18:00:00')

echo "=== 라운드 생성 ==="
curl -X POST http://localhost:8080/api/rounds?createdById=15 \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"가즈아 라운드 #1\",
    \"description\": \"회사 소개 분야 광고 라운드입니다.\",
    \"category\": \"회사소개\",
    \"orderAmount\": 100000000.00,
    \"templateCost\": 0,
    \"aiGenerationCost\": 0,
    \"targetingPostingCost\": 0,
    \"serverRentalCost\": 0,
    \"otherCosts\": 0,
    \"startDate\": \"$START_DATE\",
    \"endDate\": \"$END_DATE\",
    \"postDurationDays\": 7,
    \"maxParticipants\": 50
  }"

echo -e "\n\n"

echo "=== 활성 라운드 목록 조회 ==="
curl -X GET http://localhost:8080/api/rounds?status=ACTIVE \
  -H "Content-Type: application/json"

curl -X GET "http://localhost:8080/api/orders/round/32/keys" \
  -H "Content-Type: application/json"