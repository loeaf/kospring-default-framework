# Advertisement Posts API Documentation

## 개요
사용자가 할당받은 광고 포스트를 개별적으로 관리할 수 있는 API입니다.
할당(`advertisement_assignments`) 완료 후, 각 publisher가 포스트 내용을 작성하고 상태를 관리할 수 있습니다.

## 기본 정보
- **Base URL**: `http://localhost:8080/api/advertisement-posts`
- **Content-Type**: `application/json`
- **Authentication**: `userId` 쿼리 파라미터 사용 (Path Variable로 리소스 식별)

---

## 1. 멤버의 할당된 포스트 목록 조회

**Endpoint**: `GET /members/{memberId}/posts`

**설명**: 특정 멤버(publisher)에게 할당된 모든 포스트를 조회합니다.

**Path Parameters**:
- `memberId` (Long, required): Publisher Member ID

**Response Fields**:
- `roundStartDate`: 게시 시작일 (라운드 종료일 기준)
- `roundEndDate`: 게시 종료일 (게시 시작일 + 설정된 기간, 기본 7일)

**Request**:
```bash
curl -X GET "http://localhost:8080/api/advertisement-posts/members/23/posts"
```

**Response**:
```json
{
  "posts": [
    {
      "assignmentId": 1,
      "postId": 10,
      "roundId": 32,
      "roundTitle": "연말 마케팅 라운드",
      "roundStartDate": "2024-01-31T23:59:59",
      "roundEndDate": "2024-02-07T23:59:59",
      "advertiserMemberId": 5,
      "advertiserEmail": "advertiser@company.com",
      "advertiserCompanyName": "광고주 회사",
      "adTaskId": 47,
      "revenuePerPost": 50000.00,
      "assignmentStatus": "ASSIGNED",
      "postContent": "초안 내용...",
      "postStatus": "PENDING",
      "ctrRate": null,
      "finalRevenue": null,
      "submittedAt": "2024-01-01T09:00:00",
      "approvedAt": null,
      "publishedAt": null,
      "notes": "초안 작성 중",
      "rejectionReason": null,
      "createdAt": "2024-01-01T08:00:00"
    }
  ],
  "totalCount": 5,
  "pendingCount": 2,
  "approvedCount": 1,
  "publishedCount": 1,
  "rejectedCount": 1
}
```

---

## 2. 특정 라운드의 멤버 할당 포스트 조회

**Endpoint**: `GET /rounds/{roundId}/members/{memberId}/posts`

**설명**: 특정 라운드에서 특정 멤버가 할당받은 포스트들만 조회합니다.

**Path Parameters**:
- `roundId` (Long, required): Round ID
- `memberId` (Long, required): Publisher Member ID

**Request**:
```bash
curl -X GET "http://localhost:8080/api/advertisement-posts/rounds/32/members/1/posts"
```

**Response**: 위와 동일한 형식

---

## 3. 포스트 상세 조회

**Endpoint**: `GET /posts/{postId}`

**설명**: 특정 포스트의 상세 정보를 조회합니다.

**Path Parameters**:
- `postId` (Long, required): Post ID

**Request**:
```bash
curl -X GET "http://localhost:8080/api/advertisement-posts/posts/123"
```

**Response**:
```json
{
  "id": 123,
  "assignmentId": 456,
  "content": "완성된 광고 포스트 내용...",
  "ctrRate": 2.5,
  "finalRevenue": 75000.00,
  "postStatus": "PUBLISHED",
  "submittedAt": "2024-01-01T09:00:00",
  "approvedAt": "2024-01-01T10:00:00",
  "publishedAt": "2024-01-01T11:00:00",
  "failedAt": null,
  "failureReason": null,
  "rejectionReason": null,
  "reviewedBy": 10,
  "reviewedByName": "관리자",
  "notes": "성공적으로 게시됨",
  "assignment": {
    "id": 456,
    "roundId": 32,
    "roundTitle": "연말 마케팅 라운드",
    "roundStartDate": "2024-01-31T23:59:59",
    "roundEndDate": "2024-02-07T23:59:59",
    "advertiserMemberId": 5,
    "advertiserEmail": "advertiser@company.com",
    "advertiserCompanyName": "광고주 회사",
    "publisherMemberId": 1,
    "publisherEmail": "publisher@media.com",
    "publisherCompanyName": "미디어 회사",
    "adTaskId": 47,
    "revenuePerPost": 50000.00,
    "assignmentStatus": "COMPLETED"
  },
  "createdAt": "2024-01-01T08:00:00",
  "updatedAt": "2024-01-01T11:00:00"
}
```

---

## 4. 할당 ID로 포스트 조회

**Endpoint**: `GET /assignments/{assignmentId}/post`

**설명**: 특정 할당(assignment)에 대한 포스트를 조회합니다.

**Path Parameters**:
- `assignmentId` (Long, required): Assignment ID

**Request**:
```bash
curl -X GET "http://localhost:8080/api/advertisement-posts/assignments/456/post"
```

**Response**: 위 포스트 상세 조회와 동일한 형식

---

## 5. 포스트 내용 작성/수정

**Endpoint**: `PUT /posts/{postId}/content`

**설명**: 포스트의 내용을 작성하거나 수정합니다. (Publisher만 가능)

**Path Parameters**:
- `postId` (Long, required): Post ID

**Query Parameters**:
- `userId` (Long, required): Publisher Member ID

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "content": "훌륭한 광고 콘텐츠입니다. 이 제품은 정말 혁신적이며...",
  "notes": "초안 작성 완료, 검토 요청"
}
```

**Request**:
```bash
curl -X PUT "http://localhost:8080/api/advertisement-posts/posts/123/content?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "훌륭한 광고 콘텐츠입니다...",
    "notes": "초안 작성 완료"
  }'
```

**Response**: 포스트 상세 정보 (위 3번과 동일한 형식)

---

## 6. 포스트 상태 변경

**Endpoint**: `PUT /posts/{postId}/status`

**설명**: 포스트의 상태를 변경합니다.

**Path Parameters**:
- `postId` (Long, required): Post ID

**Query Parameters**:
- `userId` (Long, required): User ID

**Headers**:
```
Content-Type: application/json
```

**상태 변경 권한**:
- `PENDING` → `APPROVED`: 관리자만
- `PENDING` → `REJECTED`: 관리자만
- `APPROVED` → `PUBLISHED`: Publisher 또는 관리자
- `PUBLISHED` → `FAILED`: 관리자만

**Request Body**:
```json
{
  "postStatus": "PUBLISHED",
  "notes": "성공적으로 게시됨",
  "ctrRate": 2.5,
  "finalRevenue": 75000.00
}
```

**상태별 추가 필드**:
- `REJECTED`: `rejectionReason` 필수
- `FAILED`: `failureReason` 필수
- `PUBLISHED`: `ctrRate`, `finalRevenue` 선택적

**Request**:
```bash
curl -X PUT "http://localhost:8080/api/advertisement-posts/posts/123/status?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "postStatus": "PUBLISHED",
    "ctrRate": 2.5,
    "finalRevenue": 75000.00,
    "notes": "성공적으로 게시됨"
  }'
```

**Response**: 포스트 상세 정보 (위 3번과 동일한 형식)

---

## 포스트 상태(Post Status) 정보

| 상태 | 설명 | 다음 가능한 상태 |
|------|------|------------------|
| `PENDING` | 작성 완료, 승인 대기 | `APPROVED`, `REJECTED` |
| `APPROVED` | 승인됨, 게시 대기 | `PUBLISHED` |
| `REJECTED` | 거부됨, 수정 필요 | `PENDING` (수정 후) |
| `PUBLISHED` | 게시 완료 | `FAILED` (문제 발생시) |
| `FAILED` | 게시 실패 | `PENDING` (재작성) |

---

## 할당 상태(Assignment Status) 정보

| 상태 | 설명 |
|------|------|
| `ASSIGNED` | 할당 완료 |
| `IN_PROGRESS` | 작업 진행 중 |
| `COMPLETED` | 완료 |
| `FAILED` | 실패 |

---

## 사용 워크플로우

### Publisher 워크플로우:
1. **할당 확인**: `GET /members/{memberId}/posts`로 할당받은 포스트 확인
2. **내용 작성**: `PUT /posts/{postId}/content`로 포스트 내용 작성
3. **승인 대기**: 관리자의 승인 대기
4. **게시**: `PUT /posts/{postId}/status`로 `PUBLISHED` 상태로 변경
5. **실적 입력**: CTR, 최종 수익 등 업데이트

### Admin 워크플로우:
1. **승인/거부**: `PUT /posts/{postId}/status`로 `APPROVED` 또는 `REJECTED`
2. **모니터링**: 게시된 포스트의 성과 추적
3. **문제 처리**: 필요시 `FAILED` 상태로 변경

---

## 오류 응답

**400 Bad Request**:
```json
{
  "error": "Invalid post status transition",
  "message": "Cannot change from PUBLISHED to PENDING"
}
```

**403 Forbidden**:
```json
{
  "error": "Access denied",
  "message": "You are not the publisher of this post"
}
```

**404 Not Found**:
```json
{
  "error": "Post not found",
  "message": "Post not found with id: 123"
}
```

---

## 추가 예시

### 포스트 거부 (관리자)
```bash
curl -X PUT "http://localhost:8080/api/advertisement-posts/posts/123/status?userId=10" \
  -H "Content-Type: application/json" \
  -d '{
    "postStatus": "REJECTED",
    "rejectionReason": "광고 가이드라인에 맞지 않습니다. 톤앤매너를 수정해주세요.",
    "notes": "재작성 후 재제출 요청"
  }'
```

### 게시 실패 처리 (관리자)
```bash
curl -X PUT "http://localhost:8080/api/advertisement-posts/posts/123/status?userId=10" \
  -H "Content-Type: application/json" \
  -d '{
    "postStatus": "FAILED",
    "failureReason": "플랫폼 정책 위반으로 게시 중단",
    "notes": "대체 플랫폼 검토 필요"
  }'
```

---

## 테스트 API

### 테스트용 포스트 일괄 생성
```bash
curl -X POST "http://localhost:8080/api/advertisement-posts/test/rounds/32/create-all"
```

### 테스트용 상태 일괄 변경
```bash
curl -X PUT "http://localhost:8080/api/advertisement-posts/test/rounds/32/status/APPROVED?adminUserId=1"
```

### 테스트용 전체 워크플로우 실행
```bash
curl -X POST "http://localhost:8080/api/advertisement-posts/test/rounds/32/full-workflow?adminUserId=1"
```

### 테스트용 라운드 포스트 조회
```bash
curl -X GET "http://localhost:8080/api/advertisement-posts/test/rounds/32/all-posts"
```