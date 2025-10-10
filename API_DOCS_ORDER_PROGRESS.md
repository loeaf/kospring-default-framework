# Order Progress Tracking API Documentation

## 개요
포스트 게시 진행률에 따른 주문 진행률 실시간 업데이트 시스템입니다.
각 포스트가 PUBLISHED 상태로 변경될 때마다 해당 주문의 진행률이 자동으로 계산되어 업데이트됩니다.

## 핵심 기능

### 1. 자동 진행률 업데이트
- **트리거**: 포스트 상태가 PUBLISHED로 변경될 때
- **계산 방식**: (게시된 포스트 수 / 전체 포스트 수) × 100
- **적용 범위**: IN_PROGRESS 상태의 주문만

### 2. 실시간 진행률 계산
```sql
-- AdTask별 포스트 진행률 계산 쿼리
SELECT CASE 
    WHEN COUNT(aa) = 0 THEN 0
    WHEN COUNT(ap) = 0 THEN 0
    ELSE ROUND((COUNT(CASE WHEN ap.postStatus = 'PUBLISHED' THEN 1 END) * 100.0) / COUNT(aa), 0)
END
FROM AdvertisementAssignment aa
LEFT JOIN AdvertisementPost ap ON aa.id = ap.assignment.id
WHERE aa.adTask.id = :adTaskId
```

## API 엔드포인트

### 라운드별 주문 진행률 업데이트

**Endpoint**: `PUT /api/orders/round/{roundId}/update-progress`

**설명**: 라운드의 모든 주문에 대해 포스트 게시 진행률을 계산하여 진행률을 업데이트합니다.

**Path Parameters**:
- `roundId` (Long, required): Round ID

**Request**:
```bash
curl -X PUT "http://localhost:8080/api/orders/round/32/update-progress" \
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
    "completionDate": null,
    "failureDate": null,
    "progressRate": 75,
    "failureReason": null,
    "status": "IN_PROGRESS",
    "submittedAt": "2024-01-01T09:00:00",
    "reviewedAt": "2024-01-15T10:00:00",
    "reviewedByName": "관리자",
    "notes": "진행 중",
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

## 자동 업데이트 메커니즘

### 개별 포스트 상태 변경 시
포스트 상태가 PUBLISHED로 변경되면 `AdvertisementPostService.updatePostStatus()` 메소드에서 자동으로:
1. 해당 포스트의 라운드 ID 추출
2. `OrderService.updateOrderProgressByRound()` 호출
3. 라운드의 모든 주문 진행률 재계산 및 업데이트

### 대량 포스트 상태 변경 시
`updateAllPostStatusInRound()` 메소드에서 PUBLISHED 상태로 변경 시 자동으로 진행률 업데이트

## 진행률 계산 로직

### 시나리오 예시
**라운드에 4개의 포스트가 있는 경우:**

| 게시된 포스트 수 | 전체 포스트 수 | 진행률 |
|------------------|----------------|--------|
| 0 | 4 | 0% |
| 1 | 4 | 25% |
| 2 | 4 | 50% |
| 3 | 4 | 75% |
| 4 | 4 | 100% |

### 특수 케이스
- **AdTask에 할당된 포스트가 없는 경우**: 0%
- **할당은 있지만 포스트가 생성되지 않은 경우**: 0%
- **모든 포스트가 PUBLISHED인 경우**: 100% + 주문 상태 COMPLETED로 변경

## 워크플로우 통합

### 수정된 4_create-post.sh 스크립트
```bash
# 기존 단계들...

# 포스트 승인 후 진행률 확인
echo "=== 3-1. 주문 진행률 업데이트 (승인 후) ==="
curl -X PUT "http://localhost:8080/api/orders/round/32/update-progress"

# 포스트 게시 (자동으로 진행률 100% 업데이트됨)
echo "=== 4. 모든 포스트 게시 ==="
curl -X PUT "http://localhost:8080/api/advertisement-posts/test/rounds/32/status/PUBLISHED"

# 진행률 확인
echo "=== 4-1. 주문 진행률 확인 (게시 후) ==="
curl -X GET "http://localhost:8080/api/orders/round/32/info"
```

## UI 연동 가이드

### 프론트엔드에서 진행률 표시
```html
<div class="flex items-center space-x-2 mb-1">
    <div class="w-2 h-2 bg-blue-500 rounded-full"></div>
    <span class="text-sm font-medium text-blue-800">
        광고 게시 진행률 {{ order.progressRate }}%
    </span>
</div>
<div class="w-full bg-blue-200 rounded-full h-1.5 mt-2">
    <div class="bg-blue-500 h-1.5 rounded-full" 
         :style="{ width: order.progressRate + '%' }">
    </div>
</div>
```

### 실시간 업데이트 방법
1. **폴링 방식**: 주기적으로 주문 정보 API 호출
2. **WebSocket**: 실시간 진행률 변경 알림
3. **이벤트 기반**: 포스트 상태 변경 시 진행률 업데이트 트리거

## 로깅 및 모니터링

### 자동 업데이트 로그
```
INFO  : AdTask 47의 주문 진행률 업데이트: 75%
INFO  : 라운드 32 의 1개 주문 진행률이 업데이트되었습니다.
```

### 수동 업데이트 로그
```
INFO  : 라운드 32 의 3개 주문 진행률이 업데이트되었습니다.
```

### 오류 처리 로그
```
ERROR : AdTask 47 의 포스트 진행률 계산 중 오류 발생
주문 진행률 업데이트 중 오류 발생: Connection timeout
```

## 성능 고려사항

### 최적화된 쿼리
- 단일 쿼리로 진행률 계산
- 인덱스 활용으로 빠른 조회
- 트랜잭션 범위 최소화

### 배치 처리
- 라운드별 일괄 업데이트
- 중복 계산 방지
- 오류 격리 (한 주문 실패가 전체에 영향 없음)

## 예외 상황 처리

### 순환 의존성 방지
- OrderService와 AdvertisementPostService 간 순환 의존성 없음
- 비동기 처리로 트랜잭션 분리
- 오류 발생 시 원본 작업(포스트 상태 변경)에 영향 없음

### 데이터 일관성
- 트랜잭션 내에서 진행률 계산 및 업데이트
- 동시성 제어로 데이터 무결성 보장
- 재시도 로직으로 일시적 오류 복구

## 주의사항

1. **자동 업데이트**: 포스트를 PUBLISHED로 변경하면 자동으로 진행률이 업데이트됨
2. **범위 제한**: 0-100% 범위로 제한되어 있음
3. **상태 필터링**: IN_PROGRESS 상태의 주문만 업데이트 대상
4. **오류 격리**: 진행률 업데이트 실패가 포스트 상태 변경을 막지 않음

## 확장 가능성

### 미래 개선사항
- **가중치 기반 진행률**: 포스트별 중요도에 따른 가중치 적용
- **단계별 진행률**: APPROVED(50%), PUBLISHED(100%) 등 단계별 진행률
- **실시간 알림**: 진행률 변경 시 실시간 알림 시스템
- **대시보드**: 전체 라운드 진행률 통계 및 모니터링