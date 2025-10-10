# Order Advertising Start API Documentation

## 개요
입금 확인 완료 후 주문 상태를 "광고 게시중"으로 변경하는 API입니다.
PAYMENT_CONFIRMED 상태의 주문들을 IN_PROGRESS 상태로 일괄 변경하여 실제 광고 게시 작업을 시작합니다.

## 주문 상태 플로우

```
PENDING → PAYMENT_WAITING → PAYMENT_CONFIRMED → IN_PROGRESS → COMPLETED
   ↓              ↓                ↓               ↓            ↓
  대기         입금 대기        입금 확인       광고 게시중      완료
```

## API 엔드포인트

### 라운드별 광고 게시 시작

**Endpoint**: `PUT /api/orders/round/{roundId}/start-advertising`

**설명**: 라운드의 입금 확인된 주문들을 광고 게시중 상태로 변경합니다.

**Path Parameters**:
- `roundId` (Long, required): Round ID

**Query Parameters**:
- `notes` (String, optional): 상태 변경 시 추가할 메모 (기본값: "입금 확인 완료, 광고 게시 시작")

**동작 로직**:
1. 라운드 ID로 모든 관련 AdTask 조회
2. 각 AdTask와 연결된 주문 중 PAYMENT_CONFIRMED 상태인 것들만 필터링
3. 해당 주문들을 IN_PROGRESS 상태로 업데이트
4. 진행률을 0%로 초기화 (게시 작업 시작)
5. startDate를 현재 날짜로 설정

**Request**:
```bash
curl -X PUT "http://localhost:8080/api/orders/round/32/start-advertising" \
  -H "Content-Type: application/json"
```

**Custom Notes 포함 요청**:
```bash
curl -X PUT "http://localhost:8080/api/orders/round/32/start-advertising?notes=관리자%20승인으로%20광고%20게시%20시작" \
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
    "startDate": "2024-01-20",
    "completionDate": null,
    "failureDate": null,
    "progressRate": 0,
    "failureReason": null,
    "status": "IN_PROGRESS",
    "submittedAt": "2024-01-01T09:00:00",
    "reviewedAt": "2024-01-20T10:00:00",
    "reviewedByName": "관리자",
    "notes": "입금 확인 완료, 광고 게시 시작",
    "createdAt": "2024-01-01T09:00:00",
    "updatedAt": "2024-01-20T10:00:00",
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
- **입금 확인된 주문 존재**: 해당 주문들이 IN_PROGRESS로 변경됨
- **진행률 초기화**: progressRate가 0으로 설정됨
- **시작일 설정**: startDate가 현재 날짜로 설정됨

### 처리할 주문이 없는 케이스
- **AdTask 없음**: 빈 배열 반환
- **입금 확인된 주문 없음**: 빈 배열 반환 (이미 IN_PROGRESS이거나 다른 상태)

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

### 수정된 3_create-order.sh 스크립트
```bash
# 기존 단계들...

# 결제 승인
echo "=== 새로운 테스트 방식 - 라운드 ID로 일괄 결제 승인 ==="
curl -X PUT "http://localhost:8080/api/orders/test/payments/round/32/confirm-all"

# 🆕 광고 게시 시작 (입금 확인 → 게시 중)
echo "=== 입금 확인 완료 후 광고 게시중 상태로 변경 ==="
curl -X PUT "http://localhost:8080/api/orders/round/32/start-advertising"

# 주문 상태 확인
echo "=== 주문 상태 확인 ==="
curl -X GET "http://localhost:8080/api/orders/round/32/info"
```

### 전체 워크플로우
1. **라운드 생성** (`1_create-round.sh`)
2. **AdTask 생성** (`2_create_ad_tasks.sh`) 
3. **주문 및 결제 처리** (`3_create-order.sh`)
   - 주문 생성
   - 결제 정보 생성
   - 결제 승인 (PAYMENT_CONFIRMED)
   - **🆕 광고 게시 시작** (IN_PROGRESS) ← 새로 추가된 단계
4. **포스트 생성 및 게시** (`4_create-post.sh`)
   - 할당 생성
   - 포스트 생성 및 게시
   - 진행률 자동 업데이트
   - 주문 완료 처리

## 비즈니스 로직

### 상태 변경 조건
```kotlin
// PAYMENT_CONFIRMED 상태의 주문만 대상
order.status == OrderStatus.PAYMENT_CONFIRMED

// IN_PROGRESS로 변경 시 설정되는 값들
newStatus = OrderStatus.IN_PROGRESS
progressRate = 0                    // 진행률 초기화
startDate = LocalDate.now()         // 시작일 설정
notes = "입금 확인 완료, 광고 게시 시작"  // 기본 메모
```

### 진행률 관리
- **시작 시**: 0% (광고 게시 작업 시작)
- **포스트 게시마다**: 자동 계산 및 업데이트
- **완료 시**: 100% (모든 포스트 게시 완료)

## 로깅

### 성공 로그
```
INFO  : 라운드 32 의 3개 주문이 광고 게시중 상태로 변경되었습니다.
```

### 정보 로그
```
INFO  : 라운드 32 에 AdTask가 없습니다.
INFO  : 라운드 32 에 입금 확인된 주문이 없습니다.
```

## UI 통합 가이드

### 주문 상태 표시
```html
<!-- 상태별 아이콘 및 색상 -->
<div class="status-badge">
  <span v-if="order.status === 'PAYMENT_CONFIRMED'" class="text-yellow-600">
    💰 입금 확인됨
  </span>
  <span v-if="order.status === 'IN_PROGRESS'" class="text-blue-600">
    📢 광고 게시중 ({{ order.progressRate }}%)
  </span>
  <span v-if="order.status === 'COMPLETED'" class="text-green-600">
    ✅ 게시 완료
  </span>
</div>

<!-- 진행률 표시 (IN_PROGRESS 상태일 때만) -->
<div v-if="order.status === 'IN_PROGRESS'" class="progress-section">
  <div class="flex items-center space-x-2 mb-1">
    <div class="w-2 h-2 bg-blue-500 rounded-full"></div>
    <span class="text-sm font-medium text-blue-800">
      광고 게시 진행률 {{ order.progressRate }}%
    </span>
  </div>
  <div class="w-full bg-blue-200 rounded-full h-1.5">
    <div class="bg-blue-500 h-1.5 rounded-full" 
         :style="{ width: order.progressRate + '%' }">
    </div>
  </div>
</div>
```

### 관리자 액션 버튼
```html
<button v-if="order.status === 'PAYMENT_CONFIRMED'" 
        @click="startAdvertising(order.roundId)"
        class="btn-primary">
  광고 게시 시작
</button>
```

## 보안 및 권한

### 권한 체크 (향후 구현 권장)
```kotlin
// 관리자 또는 해당 주문의 소유자만 실행 가능
if (!userService.isAdmin(userId) && !userService.isOrderOwner(userId, orderId)) {
    throw UnauthorizedException("권한이 없습니다")
}
```

### 비즈니스 규칙
1. **결제 확인 필수**: PAYMENT_CONFIRMED 상태의 주문만 처리
2. **중복 실행 방지**: 이미 IN_PROGRESS인 주문은 제외
3. **트랜잭션 보장**: 모든 주문이 성공적으로 변경되거나 모두 실패
4. **감사 로그**: 상태 변경 이력 추적

## 확장 가능성

### 미래 개선사항
1. **개별 주문 처리**: 특정 주문만 광고 게시 시작
2. **조건부 시작**: 특정 조건 만족 시에만 시작 (예: 계약서 업로드 완료)
3. **예약 시작**: 지정된 날짜/시간에 자동 시작
4. **알림 시스템**: 광고주에게 게시 시작 알림
5. **승인 워크플로우**: 관리자 승인 후 게시 시작

## 주의사항

1. **원자성**: 모든 주문이 함께 변경되거나 모두 실패
2. **멱등성**: 같은 요청을 여러 번 실행해도 안전함
3. **상태 검증**: PAYMENT_CONFIRMED 상태의 주문만 대상
4. **로깅**: 모든 상태 변경이 로그에 기록됨
5. **예외 처리**: 오류 발생 시 원본 상태 유지