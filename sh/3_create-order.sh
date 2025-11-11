
#!/bin/bash
# ad_task 정보를 order로 옮김 (이체)
echo "=== 새로운 테스트 방식 - 라운드 ID로 자동 선택 ==="
curl -X POST "http://localhost:8080/api/orders/test/round/1" \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "마케팅 광고 서비스",
    "quantity": 1,
    "requirements": "라운드 ID 1 ad_index=1 AdTask로 자동 주문 생성",
    "deadline": "2025-12-31"
  }'

echo -e "\n\n"

# order의 정보를
echo "=== 새로운 테스트 방식 - 라운드 ID로 자동 결제 생성 ==="
curl -X POST "http://localhost:8080/api/orders/test/payments/round/1" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentAmount": 50000.00,

    "depositorName": "테스트 회사",
    "bankAccountNumber": "123-456-789",
    "bankName": "국민은행",
    "notes": "라운드 ID 32의 주문에 대한 자동 결제 생성"
  }'

echo -e "\n\n"

# 새로운 테스트 방식 결제 승인 - 라운드 ID로 모든 결제 일괄 승인 ( 입금대기 -> 입금확인으로 변경 )
echo "=== 새로운 테스트 방식 - 라운드 ID로 수정완료 승인 ==="
curl -X PUT "http://localhost:8080/api/orders/test/payments/round/1/confirm-all" \
  -H "Content-Type: application/json"

echo -e "\n\n"

# 입금 확인 완료 후 광고 게시중 상태로 변경 ( 입금 확인 -> 광고 게시중)
echo "=== 입금 확인 완료 후 광고 게시중 상태로 변경 ==="
curl -X PUT "http://localhost:8080/api/orders/round/1/start-advertising" \
  -H "Content-Type: application/json"

echo -e "\n\n"

# 주문 상태 확인
echo "=== 주문 상태 확인 ==="
curl -X GET "http://localhost:8080/api/orders/round/1/info" \
  -H "Content-Type: application/json"






