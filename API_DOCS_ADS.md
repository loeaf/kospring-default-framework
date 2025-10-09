# 광고 관리 API 문서

## 광고 작업 관리 API

### 라운드별 모든 광고 조회
라운드에 속한 모든 광고 작업을 조회하며 상태별 통계 정보를 포함합니다.

**Endpoint:** `GET /api/ad-tasks/rounds/{roundId}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roundId | Long | Y | 라운드 ID |

#### Response
**Success (200 OK)**
```json
{
  "roundId": 16,
  "roundTitle": "연말 Q4 라운드 #1",
  "totalAds": 5,
  "completedAds": 3,
  "pendingAds": 1,
  "failedAds": 1,
  "ads": [
    {
      "id": 15,
      "roundId": 16,
      "roundTitle": "연말 Q4 라운드 #1",
      "memberId": 15,
      "memberCompanyName": "테스트 회사",
      "memberEmail": "test@company.com",
      "status": "COMPLETED",
      "adContent": "<!DOCTYPE html><html>...</html>",
      "htmlFilePath": "generated_ads/round_16_member_15_1696723800.html",
      "createdAt": "2025-10-07T13:30:00",
      "updatedAt": "2025-10-07T13:35:00",
      "completedAt": "2025-10-07T13:35:00",
      "errorMessage": null,
      "retryCount": 0
    },
    {
      "id": 16,
      "roundId": 16,
      "roundTitle": "연말 Q4 라운드 #1",
      "memberId": 17,
      "memberCompanyName": "ABC 주식회사",
      "memberEmail": "abc@company.com",
      "status": "PENDING",
      "adContent": null,
      "htmlFilePath": null,
      "createdAt": "2025-10-07T13:30:00",
      "updatedAt": "2025-10-07T13:30:00",
      "completedAt": null,
      "errorMessage": null,
      "retryCount": 0
    }
  ]
}
```

**Error (404 Not Found)**
```json
{
  "message": "Round not found with id: 999"
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/ad-tasks/rounds/16"
```

---

### 라운드별 완료된 광고만 조회
라운드에서 생성이 완료된 광고만 조회합니다.

**Endpoint:** `GET /api/ad-tasks/rounds/{roundId}/completed`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roundId | Long | Y | 라운드 ID |

#### Response
**Success (200 OK)**
```json
[
  {
    "id": 15,
    "roundId": 16,
    "roundTitle": "연말 Q4 라운드 #1",
    "memberId": 15,
    "memberCompanyName": "테스트 회사",
    "memberEmail": "test@company.com",
    "status": "COMPLETED",
    "adContent": "<!DOCTYPE html><html lang=\"ko\"><head><meta charset=\"UTF-8\">...</html>",
    "htmlFilePath": "generated_ads/round_16_member_15_1696723800.html",
    "createdAt": "2025-10-07T13:30:00",
    "updatedAt": "2025-10-07T13:35:00",
    "completedAt": "2025-10-07T13:35:00",
    "errorMessage": null,
    "retryCount": 0
  },
  {
    "id": 18,
    "roundId": 16,
    "roundTitle": "연말 Q4 라운드 #1",
    "memberId": 19,
    "memberCompanyName": "XYZ 기업",
    "memberEmail": "xyz@company.com",
    "status": "COMPLETED",
    "adContent": "<!DOCTYPE html><html lang=\"ko\"><head><meta charset=\"UTF-8\">...</html>",
    "htmlFilePath": "generated_ads/round_16_member_19_1696724100.html",
    "createdAt": "2025-10-07T13:30:00",
    "updatedAt": "2025-10-07T13:40:00",
    "completedAt": "2025-10-07T13:40:00",
    "errorMessage": null,
    "retryCount": 0
  }
]
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/ad-tasks/rounds/16/completed"
```

---

### 특정 광고 작업 상세 조회
특정 광고 작업의 상세 정보를 조회합니다.

**Endpoint:** `GET /api/ad-tasks/{taskId}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| taskId | Long | Y | 광고 작업 ID |

#### Response
**Success (200 OK)**
```json
{
  "id": 15,
  "roundId": 16,
  "roundTitle": "연말 Q4 라운드 #1",
  "memberId": 15,
  "memberCompanyName": "테스트 회사",
  "memberEmail": "test@company.com",
  "status": "COMPLETED",
  "adContent": "<!DOCTYPE html><html lang=\"ko\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>테스트 회사 - 스크래치 광고</title><style>/* CSS 스타일 */</style></head><body><!-- 광고 HTML 내용 --></body></html>",
  "htmlFilePath": "generated_ads/round_16_member_15_1696723800.html",
  "createdAt": "2025-10-07T13:30:00",
  "updatedAt": "2025-10-07T13:35:00",
  "completedAt": "2025-10-07T13:35:00",
  "errorMessage": null,
  "retryCount": 0
}
```

**Error (404 Not Found)**
```json
{
  "message": "AdTask not found with id: 999"
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/ad-tasks/15"
```

---

### 특정 라운드에서 특정 회원의 완료된 광고 조회 (라운드 정보 포함)
특정 라운드에서 특정 회원의 완료된 광고들과 라운드 상세 정보를 함께 조회합니다. 라운드의 참여자 수, 남은 시간, 광고 가격 등의 정보가 포함됩니다.

**Endpoint:** `GET /api/ad-tasks/rounds/{roundId}/members/{memberId}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roundId | Long | Y | 라운드 ID |
| memberId | Long | Y | 회원 ID |

#### Response
**Success (200 OK)**
```json
{
  "roundId": 16,
  "roundTitle": "연말 Q4 라운드 #1",
  "roundDescription": "연말 마케팅을 위한 특별 광고 라운드입니다.",
  "category": "MARKETING",
  "orderAmount": 50000.00,
  "templateCost": 5000.00,
  "aiGenerationCost": 3000.00,
  "targetingPostingCost": 2000.00,
  "serverRentalCost": 1000.00,
  "otherCosts": 500.00,
  "startDate": "2025-10-01T09:00:00",
  "endDate": "2025-10-31T23:59:59",
  "status": "ACTIVE",
  "maxParticipants": 100,
  "currentParticipants": 45,
  "remainingTimeMs": 1814400000,
  "ads": [
    {
      "id": 15,
      "roundId": 16,
      "roundTitle": "연말 Q4 라운드 #1",
      "memberId": 15,
      "memberCompanyName": "테스트 회사",
      "memberEmail": "test@company.com",
      "status": "COMPLETED",
      "createdAt": "2025-10-07T13:30:00",
      "updatedAt": "2025-10-07T13:35:00",
      "completedAt": "2025-10-07T13:35:00",
      "errorMessage": null,
      "retryCount": 0,
      "webUrl": "https://example.com/ad-preview/round_16_member_15_scratch"
    },
    {
      "id": 16,
      "roundId": 16,
      "roundTitle": "연말 Q4 라운드 #1",
      "memberId": 15,
      "memberCompanyName": "테스트 회사",
      "memberEmail": "test@company.com",
      "status": "COMPLETED",
      "createdAt": "2025-10-07T13:30:00",
      "updatedAt": "2025-10-07T13:35:00",
      "completedAt": "2025-10-07T13:35:00",
      "errorMessage": null,
      "retryCount": 0,
      "webUrl": "https://example.com/ad-preview/round_16_member_15_carousel"
    },
    {
      "id": 17,
      "roundId": 16,
      "roundTitle": "연말 Q4 라운드 #1",
      "memberId": 15,
      "memberCompanyName": "테스트 회사",
      "memberEmail": "test@company.com",
      "status": "COMPLETED",
      "createdAt": "2025-10-07T13:30:00",
      "updatedAt": "2025-10-07T13:35:00",
      "completedAt": "2025-10-07T13:35:00",
      "errorMessage": null,
      "retryCount": 0,
      "webUrl": "https://example.com/ad-preview/round_16_member_15_interactive"
    }
  ]
}
```

#### Response 필드 설명

**라운드 정보**
- `roundId`: 라운드 ID
- `roundTitle`: 라운드 제목
- `roundDescription`: 라운드 설명
- `category`: 라운드 카테고리
- `orderAmount`: 주문 금액
- `templateCost`: 템플릿 비용
- `aiGenerationCost`: AI 생성 비용
- `targetingPostingCost`: 타겟팅 포스팅 비용
- `serverRentalCost`: 서버 렌탈 비용
- `otherCosts`: 기타 비용
- `startDate`: 라운드 시작일
- `endDate`: 라운드 종료일
- `status`: 라운드 상태 (ACTIVE, CLOSED, PENDING)
- `maxParticipants`: 최대 참여자 수
- `currentParticipants`: 현재 참여자 수
- `remainingTimeMs`: 남은 시간 (밀리초)

**광고 정보**
- `ads`: 해당 회원의 완료된 광고 목록 (최대 3개)

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/ad-tasks/rounds/26/members/17"
```

---

### 특정 회원의 모든 광고 조회
특정 회원의 모든 광고 작업을 조회합니다.

**Endpoint:** `GET /api/ad-tasks/members/{memberId}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |

#### Response
**Success (200 OK)**
```json
[
  {
    "id": 15,
    "roundId": 16,
    "roundTitle": "연말 Q4 라운드 #1",
    "memberId": 15,
    "memberCompanyName": "테스트 회사",
    "memberEmail": "test@company.com",
    "status": "COMPLETED",
    "adContent": "<!DOCTYPE html><html>...</html>",
    "htmlFilePath": "generated_ads/round_16_member_15_1696723800.html",
    "createdAt": "2025-10-07T13:30:00",
    "updatedAt": "2025-10-07T13:35:00",
    "completedAt": "2025-10-07T13:35:00",
    "errorMessage": null,
    "retryCount": 0,
    "webUrl": "https://example.com/ad-preview/round_16_member_15"
  }
]
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/ad-tasks/members/15"
```

---

### 특정 회원의 완료된 광고만 조회
특정 회원의 완료된 광고만 조회합니다.

**Endpoint:** `GET /api/ad-tasks/members/{memberId}/completed`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |

#### Response
**Success (200 OK)**
```json
[
  {
    "id": 15,
    "roundId": 16,
    "roundTitle": "연말 Q4 라운드 #1",
    "memberId": 15,
    "memberCompanyName": "테스트 회사",
    "memberEmail": "test@company.com",
    "status": "COMPLETED",
    "adContent": "<!DOCTYPE html><html>...</html>",
    "htmlFilePath": "generated_ads/round_16_member_15_1696723800.html",
    "createdAt": "2025-10-07T13:30:00",
    "updatedAt": "2025-10-07T13:35:00",
    "completedAt": "2025-10-07T13:35:00",
    "errorMessage": null,
    "retryCount": 0,
    "webUrl": "https://example.com/ad-preview/round_16_member_15"
  }
]
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/ad-tasks/members/15/completed"
```

---

### 라운드별 광고 생성 작업 통계 조회
라운드의 광고 생성 작업 통계를 조회합니다.

**Endpoint:** `GET /api/ad-tasks/rounds/{roundId}/statistics`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roundId | Long | Y | 라운드 ID |

#### Response
**Success (200 OK)**
```json
{
  "roundId": 16,
  "roundTitle": "연말 Q4 라운드 #1",
  "statistics": {
    "PENDING": 1,
    "PROCESSING": 0,
    "COMPLETED": 3,
    "FAILED": 1,
    "RETRY": 0
  },
  "totalTasks": 5
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/ad-tasks/rounds/16/statistics"
```

---

## 광고 작업 관리 API (관리자용)

### 작업 상태 업데이트
광고 생성 작업의 상태를 업데이트합니다. (Python 워커에서 호출)

**Endpoint:** `PUT /api/ad-tasks/{taskId}/status`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| taskId | Long | Y | 광고 작업 ID |

#### Request Body
```json
{
  "status": "COMPLETED",
  "htmlFilePath": "generated_ads/round_16_member_15_1696723800.html",
  "errorMessage": null
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| status | String | Y | 작업 상태 (PENDING, PROCESSING, COMPLETED, FAILED, RETRY) |
| htmlFilePath | String | N | 생성된 HTML 파일 경로 |
| errorMessage | String | N | 오류 메시지 (실패 시) |

#### Response
**Success (200 OK)**
```json
{
  "message": "Task status updated successfully"
}
```

**Error (400 Bad Request)**
```json
{
  "error": "Invalid status: INVALID_STATUS"
}
```

#### cURL 예제
```bash
curl -X PUT "http://localhost:8080/api/ad-tasks/15/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED",
    "htmlFilePath": "generated_ads/round_16_member_15_1696723800.html"
  }'
```

---

### 실패한 작업 재시도
실패한 광고 생성 작업을 다시 큐에 추가합니다.

**Endpoint:** `POST /api/ad-tasks/{taskId}/retry`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| taskId | Long | Y | 광고 작업 ID |

#### Request Body
```json
{
  "errorMessage": "Manual retry requested"
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| errorMessage | String | N | 재시도 사유 |

#### Response
**Success (200 OK)**
```json
{
  "message": "Task queued for retry"
}
```

#### cURL 예제
```bash
curl -X POST "http://localhost:8080/api/ad-tasks/15/retry" \
  -H "Content-Type: application/json" \
  -d '{
    "errorMessage": "Network timeout - retry needed"
  }'
```

---

### 라운드의 대기중인 작업 수동 큐잉
라운드의 모든 PENDING 상태 작업을 수동으로 큐에 추가합니다.

**Endpoint:** `POST /api/ad-tasks/rounds/{roundId}/enqueue-pending`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roundId | Long | Y | 라운드 ID |

#### Response
**Success (200 OK)**
```json
{
  "message": "Pending tasks enqueued successfully",
  "enqueuedCount": 3
}
```

**Error (500 Internal Server Error)**
```json
{
  "error": "Failed to enqueue tasks: Redis connection failed"
}
```

#### cURL 예제
```bash
curl -X POST "http://localhost:8080/api/ad-tasks/rounds23/enqueue-pending"
```

---

## 데이터 모델

### AdTaskStatus (광고 작업 상태)
- `PENDING`: 대기 중 - 큐에 추가되기 전 또는 대기 중
- `PROCESSING`: 처리 중 - AI가 광고를 생성하는 중
- `COMPLETED`: 완료 - 광고 생성 완료, HTML 파일 생성됨
- `FAILED`: 실패 - 광고 생성 실패, 오류 메시지 포함
- `RETRY`: 재시도 대기 - 실패 후 재시도 큐에 추가된 상태

### AdTask 필드 설명
- `id`: 광고 작업 고유 ID
- `roundId`: 소속 라운드 ID
- `roundTitle`: 라운드 제목
- `memberId`: 광고 대상 회원 ID
- `memberCompanyName`: 회원의 회사명
- `memberEmail`: 회원 이메일
- `status`: 작업 진행 상태
- `webUrl`: 광고 미리보기 웹 URL
- `createdAt`: 작업 생성 시간
- `updatedAt`: 마지막 업데이트 시간
- `completedAt`: 작업 완료 시간
- `errorMessage`: 오류 발생 시 오류 메시지
- `retryCount`: 재시도 횟수 (최대 10회)

### RoundWithAdsResponse 필드 설명
- `roundId`: 라운드 ID
- `roundTitle`: 라운드 제목
- `roundDescription`: 라운드 설명
- `category`: 라운드 카테고리
- `orderAmount`: 주문 금액
- `templateCost`: 템플릿 비용
- `aiGenerationCost`: AI 생성 비용
- `targetingPostingCost`: 타겟팅 포스팅 비용
- `serverRentalCost`: 서버 렌탈 비용
- `otherCosts`: 기타 비용
- `startDate`: 라운드 시작일
- `endDate`: 라운드 종료일
- `status`: 라운드 상태 (ACTIVE, CLOSED, PENDING)
- `maxParticipants`: 최대 참여자 수
- `currentParticipants`: 현재 참여자 수
- `remainingTimeMs`: 남은 시간 (밀리초)
- `ads`: 해당 회원의 완료된 광고 목록 (최대 3개)

### 비즈니스 로직

#### 광고 생성 플로우
1. **라운드 생성** → 자동으로 모든 프리미엄 회원에 대해 AdTask 생성 (PENDING 상태)
2. **큐 처리** → Python ad_generator가 Redis 큐에서 작업을 가져와 처리
3. **AI 생성** → Claude API를 사용해 회원별 맞춤 HTML 광고 3개 생성 (스크래치, 캐러셀, 인터랙티브)
4. **데이터베이스 저장** → 생성된 광고를 DB에 저장하고 웹 URL 기록
5. **상태 업데이트** → 작업 상태를 COMPLETED로 변경

#### 에러 처리
- **네트워크 오류**: RETRY 상태로 변경 후 재큐잉
- **API 한도 초과**: FAILED 상태로 변경, 수동 재시도 가능
- **잘못된 데이터**: FAILED 상태로 변경, 오류 메시지 기록
- **최대 재시도 횟수 초과**: FAILED 상태로 고정

#### 광고 타입별 특징
- **스크래치 (scratch)**: 기존 스크래치 카드 템플릿 사용
- **캐러셀 (carousel)**: 슬라이드 형태의 인터랙티브 광고
- **인터랙티브 (interactive)**: 메모리 매칭 게임 형태의 광고

#### 제약사항
- 한 라운드에서 한 회원당 3개의 광고 생성 (각기 다른 타입)
- 재시도 횟수는 최대 10회로 제한
- 프리미엄 회원만 광고 생성 대상
- 광고는 웹 URL을 통해 제공되며, 파일 경로는 API에서 제외

#### 권한 및 보안
- 모든 조회 API는 인증 없이 접근 가능 (추후 인증 추가 예정)
- 상태 업데이트 API는 시스템 내부에서만 호출
- HTML 콘텐츠는 XSS 방지를 위해 별도 도메인에서 서빙 권장

#### 모니터링 포인트
- 전체 작업 수 대비 완료율 (회원당 3개 광고 기준)
- 평균 광고 생성 시간
- 실패율 및 주요 실패 원인
- Redis 큐 대기 시간
- Claude API 사용량 및 비용
- 라운드별 참여자 현황 및 완료율

#### API 변경 사항 (v2.0)
- `adContent` 및 `htmlFilePath` 필드 제거
- `webUrl` 필드 추가
- 회원당 3개 광고 생성 지원
- 라운드 상세 정보 포함 응답 형태 추가
- 참여자 수 및 남은 시간 정보 제공