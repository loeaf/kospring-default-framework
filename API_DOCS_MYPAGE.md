# 마이페이지 API 문서

## 개요
마이페이지 API는 사용자의 프로필 정보, 통계, AI 설정, 계정 관리 등을 제공합니다.

## Base URL
```
http://localhost:8080/api/mypage
```

---

## 사용자 정보 API

### 1. 사용자 프로필 조회
사용자의 기본 프로필 정보를 조회합니다.

```http
GET /api/mypage/profile
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "id": 1,
  "name": "김도현",
  "email": "user@example.com",
  "membership": "PREMIUM",
  "joinDate": "2024-01-01T00:00:00",
  "nextPaymentDate": "2024-09-01T00:00:00",
  "stats": {
    "rounds": 12,
    "totalOrders": 45,
    "totalAds": 28,
    "impressions": 45000,
    "clicks": 28000
  }
}
```

### 2. 월간 통계 조회
사용자의 월간 활동 통계를 조회합니다.

```http
GET /api/mypage/stats/monthly
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "period": "2024년 08월",
  "orders": {
    "total": 45,
    "completed": 42,
    "weekly": 3
  },
  "ads": {
    "total": 28,
    "published": 25,
    "weekly": 2
  },
  "revenue": {
    "adRevenue": 2847500,
    "orderRevenue": 1200000
  }
}
```

#### 수익 데이터 설명
- **adRevenue**: 광고 매출 (revenue_transactions에서 INCOME 거래 합계)
- **orderRevenue**: 주문 매입 (revenue_transactions에서 EXPENSE 거래 합계)

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/mypage/stats/monthly?memberId=2"
```

### 3. 계정 정보 조회
사용자의 계정 정보를 조회합니다.

```http
GET /api/mypage/account-info
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "membership": "프리미엄",
  "membershipLevel": "PREMIUM",
  "joinDate": "2024-01-01T00:00:00",
  "email": "user@example.com",
  "nextPaymentDate": "2024-09-01T00:00:00",
  "isSubscriptionActive": true,
  "subscriptionEndDate": "2025-01-01T00:00:00"
}
```

---

## 광고 관리 API

### 1. 최근 광고 목록 조회 (마이페이지용)
사용자가 최근에 게시한 광고 목록을 조회합니다.

```http
GET /api/mypage/recent-ads
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |
| limit | Int | No | 조회할 광고 수 (기본값: 5) |

#### Response
```json
[
  {
    "id": 15,
    "roundId": 16,
    "roundTitle": "연말 Q4 라운드 #1",
    "status": "COMPLETED",
    "adContent": "<!DOCTYPE html><html>...</html>",
    "htmlFilePath": "generated_ads/round_16_member_15_1696723800.html",
    "previewUrl": "/ads/generated_ads/round_16_member_15_1696723800.html",
    "createdAt": "2024-10-07T13:30:00",
    "updatedAt": "2024-10-07T13:35:00",
    "completedAt": "2024-10-07T13:35:00",
    "errorMessage": null,
    "retryCount": 0,
    "metadata": {
      "tags": ["헬스케어", "브랜딩"],
      "category": "헬스케어",
      "title": "헬스케어 브랜드 광고",
      "description": "건강한 라이프스타일을 제안하는 브랜드 광고",
      "client": "테스트 회사",
      "previewHeight": "400px"
    }
  }
]
```

#### Status Types
- `PENDING`: 생성 대기
- `IN_PROGRESS`: 생성 중  
- `COMPLETED`: 완료
- `FAILED`: 실패
- `PUBLISHED`: 게시됨

---

## AI 설정 API

### 1. AI 설정 조회
사용자의 AI 자동화 설정을 조회합니다.

```http
GET /api/mypage/ai-settings
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "postWriting": false,
  "orderWriting": false,
  "updatedAt": "2024-08-26T10:30:00"
}
```

### 2. AI 설정 업데이트
사용자의 AI 자동화 설정을 업데이트합니다.

```http
PUT /api/mypage/ai-settings
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Request Body
```json
{
  "postWriting": true,
  "orderWriting": false
}
```

#### Response
```json
{
  "postWriting": true,
  "orderWriting": false,
  "updatedAt": "2024-08-26T10:35:00"
}
```

---

## 알림 설정 API

### 1. 알림 설정 조회
사용자의 알림 설정을 조회합니다.

```http
GET /api/mypage/notification-settings
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "emailNotifications": true,
  "smsNotifications": false,
  "pushNotifications": true,
  "marketingEmails": false,
  "orderUpdates": true,
  "adPerformanceAlerts": true
}
```

### 2. 알림 설정 업데이트
사용자의 알림 설정을 업데이트합니다.

```http
PUT /api/mypage/notification-settings
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Request Body
```json
{
  "emailNotifications": true,
  "smsNotifications": false,
  "pushNotifications": true,
  "marketingEmails": false,
  "orderUpdates": true,
  "adPerformanceAlerts": true
}
```

#### Response
```json
{
  "emailNotifications": true,
  "smsNotifications": false,
  "pushNotifications": true,
  "marketingEmails": false,
  "orderUpdates": true,
  "adPerformanceAlerts": true
}
```

---

## 계정 관리 API

### 1. 비밀번호 변경
사용자의 비밀번호를 변경합니다.

```http
PUT /api/mypage/password
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Request Body
```json
{
  "currentPassword": "현재비밀번호",
  "newPassword": "새비밀번호"
}
```

#### Response
```json
{
  "success": true,
  "message": "비밀번호가 성공적으로 변경되었습니다."
}
```

### 2. 프로필 이미지 업로드
사용자의 프로필 이미지를 업로드합니다.

```http
POST /api/mypage/profile/image
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |
| imageUrl | String | Yes | 이미지 URL |

#### Response
```json
{
  "success": true,
  "imageUrl": "https://example.com/profile.jpg",
  "message": "프로필 이미지가 업데이트되었습니다."
}
```

### 3. 회원 탈퇴
회원 탈퇴를 처리합니다.

```http
DELETE /api/mypage/account
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |
| reason | String | No | 탈퇴 사유 |

#### Response
```json
{
  "success": true,
  "message": "회원 탈퇴가 완료되었습니다.",
  "deletedAt": "2024-08-26T10:30:00"
}
```

---

## 테스트/관리 API

### 1. 마이페이지 데이터 초기화
테스트용 마이페이지 데이터를 초기화합니다.

```http
POST /api/mypage/test/initialize
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "success": true,
  "message": "테스트 마이페이지 데이터가 초기화되었습니다.",
  "memberId": 1
}
```

### 2. 통계 새로고침
통계 캐시를 새로고침합니다.

```http
POST /api/mypage/stats/refresh
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "success": true,
  "message": "통계가 새로고침되었습니다.",
  "refreshedAt": "2024-08-26T10:30:00"
}
```

---

## 활동 내역 API

### 1. 최근 활동 조회
사용자의 최근 활동 내역을 조회합니다.

```http
GET /api/mypage/activities
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |
| limit | Int | No | 조회할 활동 수 (기본값: 10) |

#### Response
```json
{
  "activities": [
    {
      "id": 1,
      "type": "ORDER_COMPLETED",
      "title": "뷰티 패키지 디자인 완료",
      "description": "구매번호 #ORD-2024-002",
      "timestamp": "2024-08-25T14:30:00",
      "status": "완료",
      "metadata": {}
    },
    {
      "id": 2,
      "type": "AD_PUBLISHED",
      "title": "헬스케어 광고 게시",
      "description": "Round #246 · 헬스케어 분야",
      "timestamp": "2024-08-24T11:20:00",
      "status": "게시됨",
      "metadata": {}
    }
  ],
  "totalCount": 15
}
```

### 2. 대시보드 요약 정보 조회
마이페이지에 필요한 모든 정보를 한 번에 조회합니다.

```http
GET /api/mypage/dashboard
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "userProfile": {
    "id": 1,
    "name": "김도현",
    "email": "user@example.com",
    "membership": "PREMIUM",
    "joinDate": "2024-01-01T00:00:00",
    "nextPaymentDate": "2024-09-01T00:00:00",
    "stats": {
      "rounds": 12,
      "totalOrders": 45,
      "totalAds": 28,
      "impressions": 45000,
      "clicks": 28000
    }
  },
  "monthlyStats": {
    "period": "2024년 08월",
    "orders": {
      "total": 45,
      "completed": 42,
      "weekly": 3
    },
    "ads": {
      "total": 28,
      "published": 25,
      "weekly": 2
    },
    "revenue": {
      "adRevenue": 2847500,
      "orderRevenue": 1200000
    }
  },
  "aiSettings": {
    "postWriting": false,
    "orderWriting": false,
    "updatedAt": "2024-08-26T10:30:00"
  },
  "accountInfo": {
    "membership": "프리미엄",
    "membershipLevel": "PREMIUM",
    "joinDate": "2024-01-01T00:00:00",
    "email": "user@example.com",
    "nextPaymentDate": "2024-09-01T00:00:00",
    "isSubscriptionActive": true,
    "subscriptionEndDate": "2025-01-01T00:00:00"
  },
  "recentActivities": [
    {
      "id": 1,
      "type": "ORDER_COMPLETED",
      "title": "뷰티 패키지 디자인 완료",
      "description": "구매번호 #ORD-2024-002",
      "timestamp": "2024-08-25T14:30:00",
      "status": "완료"
    }
  ]
}
```

---

## 구매 관리 API (통합)

### 1. 내가 구매한 광고 목록 조회 (통합)
사용자가 구매한 모든 광고 주문 목록을 조회합니다. 광고 정보, 영수증 정보, 광고 URL을 모두 포함합니다.

```http
GET /api/orders/members/{memberId}/my-purchases
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
[
  {
    "orderId": 15,
    "orderNumber": "ORD-2024-001",
    "roundId": 16,
    "adTaskId": 25,
    "productName": "헬스케어 브랜드 광고",
    "quantity": 1,
    "orderStatus": "COMPLETED",
    "orderDate": "2024-10-07T13:30:00",
    "paymentStatus": "CONFIRMED",
    "paymentAmount": 100000000,
    "deadline": "2024-10-15",
    "requirements": "건강한 라이프스타일을 강조해주세요",
    "totalParticipants": 8,
    "roundTitle": "Round #16",
    "adUrl": "https://example.com/ads/round16/ad25.html",
    "receiptInfo": {
      "receiptNumber": "REC-2024-001",
      "totalAmount": 100000000,
      "vatAmount": 10000000,
      "finalAmount": 110000000,
      "companyName": "CNC"
    }
  },
  {
    "orderId": 14,
    "orderNumber": "ORD-2024-002",
    "roundId": 15,
    "adTaskId": 22,
    "productName": "뷰티 패키지 디자인",
    "quantity": 2,
    "orderStatus": "IN_PROGRESS",
    "orderDate": "2024-10-05T10:15:00",
    "paymentStatus": "WAITING",
    "paymentAmount": 100000000,
    "deadline": "2024-10-20",
    "requirements": "고급스러운 느낌으로 제작해주세요",
    "totalParticipants": 12,
    "roundTitle": "Round #15",
    "adUrl": "https://example.com/ads/round15/ad22.html",
    "receiptInfo": {
      "receiptNumber": "REC-2024-002",
      "totalAmount": 200000000,
      "vatAmount": 20000000,
      "finalAmount": 220000000,
      "companyName": "CNC"
    }
  }
]
```

#### Order Status Types
- `PENDING`: 주문 대기
- `PAYMENT_WAITING`: 입금 대기
- `IN_PROGRESS`: 광고 게시중
- `COMPLETED`: 완료
- `CANCELLED`: 취소됨

#### Payment Status Types
- `WAITING`: 입금 대기
- `CONFIRMED`: 입금 확인
- `CANCELLED`: 결제 취소

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/members/1/my-purchases"
```

### 2. 광고 URL 다운로드
advertisement_assignments 테이블에서 URL을 조회하여 octet-stream으로 다운로드합니다.

```http
GET /api/orders/{orderId}/ad-url/download
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| orderId | Long | Yes | 주문 ID |

#### Response
- **Content-Type**: `application/octet-stream`
- **Content-Disposition**: `attachment; filename=ad-url-{orderId}.html`

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/15/ad-url/download" \
  -o "ad-url-15.html"
```

### 3. 영수증 다운로드
HTML 영수증을 octet-stream으로 다운로드합니다.

```http
GET /api/orders/{orderId}/receipt/download
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| orderId | Long | Yes | 주문 ID |

#### Response
- **Content-Type**: `application/octet-stream`
- **Content-Disposition**: `attachment; filename=receipt-{orderId}.html`

#### 영수증 포함 내용
- 발행업체 정보 (CNC)
- 고객 정보 (회사명, 연락처 등)
- 주문 상세 정보 (상품명, 수량, 단가 등)
- 결제 정보 (결제방법, 상태, 은행 정보)
- 금액 정보 (합계, 부가세, 총액)

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/15/receipt/download" \
  -o "receipt-15.html"
```

## 장점

### 1. 단일 API 호출
- 모든 정보(광고 정보, 영수증 정보, 광고 URL)를 한 번에 조회
- 네트워크 요청 최소화

### 2. 효율적인 데이터 조회
- 4개 테이블(orders, order_payments, ad_tasks, advertisement_assignments) 조인으로 모든 데이터 확보
- 데이터베이스 쿼리 최적화

### 3. 간단한 다운로드
- 모든 다운로드를 octet-stream으로 통일
- 클라이언트에서 일관된 방식으로 처리 가능

### 4. 영수증 통합
- CNC가 서비스 공급자로 자동 설정
- 부가세 10% 자동 계산
- 영수증 번호 자동 생성

#### 금액 계산 방식
- **합계**: 단가 × 수량
- **부가세**: 합계 × 10%
- **총액**: 합계 + 부가세

#### Error Responses
```json
{
  "error": "Order not found",
  "message": "Order not found with id: 999",
  "status": 404
}
```
