# Order Completion API Documentation

## 개요
포스트 게시 완료에 따른 주문 상태 자동 업데이트 API입니다.
라운드의 모든 포스트가 PUBLISHED 상태가 되면, 해당 라운드의 IN_PROGRESS 상태 주문들을 COMPLETED로 자동 업데이트합니다.

## API 엔드포인트

### 라운드별 주문 완료 처리

**Endpoint**: `PUT /api/orders/round/{roundId}/complete-if-posts-published`

**설명**: 라운드의 모든 포스트가 게시 완료되었을 때 관련 주문들을 완료 상태로 업데이트합니다.

**Path Parameters**:
- `roundId` (Long, required): Round ID

**Query Parameters**:
- `notes` (String, optional): 주문 완료 시 추가할 메모 (기본값: "모든 포스트 게시 완료로 인한 주문 완료")

**동작 로직**:
1. 라운드 ID로 모든 관련 AdTask 조회
2. 각 AdTask와 연결된 주문 중 IN_PROGRESS 상태인 것들만 필터링
3. 라운드의 모든 포스트가 PUBLISHED 상태인지 확인
4. 조건이 충족되면 주문들을 COMPLETED 상태로 업데이트 (progressRate: 100%)

**Request**:
```bash
curl -X PUT "http://localhost:8080/api/orders/round/32/complete-if-posts-published" \
  -H "Content-Type: application/json"
```

**Custom Notes 포함 요청**:
```bash
curl -X PUT "http://localhost:8080/api/orders/round/32/complete-if-posts-published?notes=수동%20완료%20처리" \
  -H "Content-Type: application/json"
```

**Response**:
```json
[
  {
    "id": 8,
    "orderNumber": "ORD-240101-001",
    "adTaskId": 47,
    "adTaskTitle": "https://example.com/ad-content",
    "memberId": 32,
    "memberCompanyName": "광고주 회사",
    "memberEmail": "advertiser@company.com",
    "productName": "마케팅 광고 서비스",
    "quantity": 1,
    "requirements": "특별한 요구사항이 있습니다.",
    "deadline": "2025-12-31",
    "startDate": "2024-01-15",
    "completionDate": "2024-01-20",
    "failureDate": null,
    "progressRate": 100,
    "failureReason": null,
    "status": "COMPLETED",
    "submittedAt": "2024-01-01T09:00:00",
    "reviewedAt": "2024-01-20T14:30:00",
    "reviewedByName": "관리자",
    "notes": "모든 포스트 게시 완료로 인한 주문 완료",
    "createdAt": "2024-01-01T09:00:00",
    "updatedAt": "2024-01-20T14:30:00",
    "paymentInfo": {
      "id": 12,
      "applicationNumber": "PAY-240101-001",
      "paymentAmount": 50000.00,
      "depositorName": "테스트 회사",
      "paymentStatus": "CONFIRMED",
      "bankAccountNumber": "123-456-789",
      "bankName": "국민은행",
      "paymentConfirmedAt": "2024-01-02T10:00:00",
      "notes": "입금 확인됨"
    }
  }
]
```

**빈 응답 케이스**:
```json
[]
```

## 응답 시나리오

### 성공 케이스
- **모든 포스트 게시 완료**: 해당 라운드의 IN_PROGRESS 주문들이 COMPLETED로 업데이트
- **진행중인 주문 없음**: 빈 배열 반환
- **AdTask 없음**: 빈 배열 반환

### 아직 완료되지 않은 케이스
- **일부 포스트가 미게시**: 빈 배열 반환, 로그에 "모든 포스트가 아직 게시되지 않았습니다" 메시지

### 오류 케이스
- **404**: 라운드를 찾을 수 없음
- **500**: 서버 내부 오류

**오류 응답 예시**:
```json
{
  "error": "Round not found",
  "message": "Round not found with id: 999"
}
```

## 워크플로우 통합

### 기존 스크립트 수정
`4_create-post.sh` 스크립트에 다음 단계가 추가됨:

```bash
# 6. 포스트 게시 완료로 주문 상태 업데이트
echo "=== 6. 포스트 게시 완료로 주문 상태 업데이트 ==="
curl -X PUT "http://localhost:8080/api/orders/round/32/complete-if-posts-published" \
  -H "Content-Type: application/json"
```

### 전체 워크플로우
1. **라운드 생성** (`1_create-round.sh`)
2. **AdTask 생성** (`2_create_ad_tasks.sh`)
3. **주문 생성 및 결제** (`3_create-order.sh`)
4. **포스트 생성 및 게시** (`4_create-post.sh`)
   - 할당 생성
   - 포스트 생성
   - 포스트 승인
   - 포스트 게시
   - **🆕 주문 완료 처리** ← 새로 추가된 단계

## 데이터베이스 쿼리

API는 다음 쿼리를 사용하여 포스트 게시 상태를 확인:

```sql
SELECT CASE 
    WHEN COUNT(aa) = 0 THEN false
    WHEN COUNT(ap) = 0 THEN false
    WHEN COUNT(ap) = COUNT(CASE WHEN ap.postStatus = 'PUBLISHED' THEN 1 END) THEN true
    ELSE false
END
FROM AdvertisementAssignment aa
LEFT JOIN AdvertisementPost ap ON aa.id = ap.assignment.id
WHERE aa.round.id = :roundId
```

## 로깅

API 실행 시 다음과 같은 로그가 생성됩니다:

```
INFO  : 라운드 32 포스트 게시 상태 확인 결과: true
INFO  : 라운드 32 의 3개 주문이 완료 상태로 업데이트되었습니다.
```

또는

```
INFO  : 라운드 32 에 진행중인 주문이 없습니다.
INFO  : 라운드 32 의 모든 포스트가 아직 게시되지 않았습니다.
```

## 사용 권장사항

1. **자동화**: 포스트 게시 완료 후 자동으로 호출하여 워크플로우 완성
2. **모니터링**: 로그를 통해 주문 완료 처리 상태 확인
3. **예외 처리**: 네트워크 오류나 서버 오류에 대한 재시도 로직 구현 권장

## 주의사항

- 이미 COMPLETED 상태인 주문은 영향받지 않음
- IN_PROGRESS 상태의 주문만 대상
- 포스트가 하나라도 PUBLISHED가 아니면 처리되지 않음
- 트랜잭션으로 처리되어 원자성 보장