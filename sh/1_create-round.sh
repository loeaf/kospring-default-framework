START_DATE=$(date -v+1m '+%Y-%m-%dT09:00:00')
END_DATE=$(date -v+2m '+%Y-%m-%dT18:00:00')

echo "=== 라운드 생성 ==="
curl -X POST http://localhost:8080/api/rounds?createdById=15 \
  -H "Content-Type: application/json" \
  -d "{
    \"title\": \"연말 111 라운드 #1\",
    \"description\": \"회사 소개 분야 광고 라운드입니다.\",
    \"category\": \"회사소개\",
    \"orderAmount\": 100000000.00,
    \"templateCost\": 10000000.00,
    \"aiGenerationCost\": 20000000.00,
    \"targetingPostingCost\": 30000000.00,
    \"serverRentalCost\": 25000000.00,
    \"otherCosts\": 15000000.00,
    \"startDate\": \"$START_DATE\",
    \"endDate\": \"$END_DATE\",
    \"maxParticipants\": 50
  }"

echo -e "\n\n"

echo "=== 활성 라운드 목록 조회 ==="
curl -X GET http://localhost:8080/api/rounds?status=ACTIVE \
  -H "Content-Type: application/json"

curl -X GET "http://localhost:8080/api/orders/round/32/keys" \
  -H "Content-Type: application/json"