# 순환발주 기반 광고 소개 블로그 API 문서

## 📋 개요
라운드 참여자들이 서로의 광고를 소개하는 블로그 시스템의 API 문서입니다. 
순환발주 알고리즘을 통해 정확한 원금보장과 함께 n명 참여 시 n-1개의 소개글을 작성합니다.

## 🎯 핵심 비즈니스 로직
1. **순환발주 시스템**: 수학적 알고리즘으로 정확한 원금보장 (받는 금액 = 투자 금액)
2. **n-1 법칙**: n명 참여 시 각자 자신을 제외한 n-1개의 광고 소개글 작성
3. **라운드 동기화**: 게시 시작/종료일이 라운드 일정과 완전 동일
4. **비용 할당**: 순환발주로 계산된 정확한 작성비 할당
5. **중복 방지**: 동일 광고에 대해 사용자당 1개 소개글만 작성 가능

## 🔧 기본 정보
- Base URL: `http://localhost:8080/api`
- Content-Type: `application/json`
- 인증: JWT Bearer Token (Authorization 헤더)

---

## 🔐 인증 관련 API

### 로그인 상태 확인
```http
GET /auth/me
```

**응답**:
```json
{
  "id": 1,
  "email": "user@example.com",
  "companyName": "회사명",
  "isActive": true
}
```

---

## 📝 게시글 관리 API

### 1. 게시글 생성
특정 광고에 대한 소개 블로그 글을 작성합니다.

```http
POST /api/posts
```

**요청**:
```json
{
  "title": "혁신적인 AI 마케팅 솔루션 소개",
  "content": "# 스마트애드: 차세대 AI 마케팅 플랫폼\n\n같은 라운드에서 함께하는 스마트애드의...",
  "roundId": 1,
  "targetAdTaskId": 2,
  "assignedCost": 2500.00,
  "postType": "BLOG_INTRODUCTION",
  "status": "DRAFT"
}
```

**cURL 예제**:
```bash
curl -X POST "http://localhost:8080/api/posts" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "title": "혁신적인 AI 마케팅 솔루션 소개",
    "content": "# 스마트애드: 차세대 AI 마케팅 플랫폼\n\n같은 라운드에서 함께하는 스마트애드의 혁신적인 솔루션을 소개합니다.",
    "roundId": 1,
    "targetAdTaskId": 2,
    "assignedCost": 2500.00,
    "postType": "BLOG_INTRODUCTION",
    "status": "DRAFT"
  }'
```

**응답**:
```json
{
  "id": 1,
  "title": "혁신적인 AI 마케팅 솔루션 소개",
  "content": "# 스마트애드: 차세대 AI 마케팅 플랫폼...",
  "authorId": 1,
  "authorName": "내 회사",
  "roundId": 1,
  "roundTitle": "Q4 마케팅 캠페인",
  "targetAdTaskId": 2,
  "targetAdContent": "AI 기반 타겟 마케팅 광고",
  "assignedCost": 2500.00,
  "postType": "BLOG_INTRODUCTION",
  "status": "DRAFT",
  "postStartDate": "2024-01-01T00:00:00",
  "postEndDate": "2024-01-31T23:59:59",
  "publishedAt": null,
  "createdAt": "2024-10-09T10:30:00",
  "updatedAt": "2024-10-09T10:30:00",
  "isActive": false
}
```

### 2. 게시글 상세 조회
```http
GET /api/posts/{id}
```

**Query Parameters**:
- `incrementView` (Boolean, default: true): 조회수 증가 여부

**cURL 예제**:
```bash
# 조회수 증가와 함께 조회
curl -X GET "http://localhost:8080/api/posts/1?incrementView=true"

# 조회수 증가 없이 조회
curl -X GET "http://localhost:8080/api/posts/1?incrementView=false"
```

**응답**: 게시글 생성과 동일한 PostResponse 형식

### 3. 게시글 수정
```http
PUT /api/posts/{id}
```

**요청**:
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용",
  "assignedCost": 2600.00,
  "postType": "TUTORIAL",
  "status": "PUBLISHED"
}
```

**cURL 예제**:
```bash
curl -X PUT "http://localhost:8080/api/posts/1" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "title": "수정된 제목",
    "content": "수정된 내용",
    "assignedCost": 2600.00,
    "postType": "TUTORIAL",
    "status": "PUBLISHED"
  }'
```

**응답**: PostResponse 형식

### 4. 게시글 삭제
```http
DELETE /api/posts/{id}
```

**cURL 예제**:
```bash
curl -X DELETE "http://localhost:8080/api/posts/1" \
  -H "X-User-Id: 1"
```

**응답**: `204 No Content`

### 5. 게시글 목록 조회
```http
GET /api/posts
```

**Query Parameters**:
- `page` (Integer, default: 0): 페이지 번호
- `size` (Integer, default: 20): 페이지 크기

**cURL 예제**:
```bash
# 기본 목록 조회
curl -X GET "http://localhost:8080/api/posts"

# 페이지네이션과 함께
curl -X GET "http://localhost:8080/api/posts?page=0&size=10"
```

**응답**:
```json
{
  "content": [
    {
      "id": 1,
      "title": "혁신적인 AI 마케팅 솔루션 소개",
      "authorId": 1,
      "authorName": "내 회사",
      "roundId": 1,
      "roundTitle": "Q4 마케팅 캠페인",
      "targetAdTaskId": 2,
      "assignedCost": 2500.00,
      "postType": "BLOG_INTRODUCTION",
      "status": "PUBLISHED",
      "postStartDate": "2024-01-01T00:00:00",
      "postEndDate": "2024-01-31T23:59:59",
      "publishedAt": "2024-01-01T09:00:00",
      "createdAt": "2024-10-09T10:30:00",
      "updatedAt": "2024-10-09T10:30:00",
      "isActive": true
    }
  ],
  "pageable": {
    "page": 0,
    "size": 20
  },
  "totalElements": 5,
  "totalPages": 1
}
```

### 6. 게시글 검색
```http
GET /api/posts/search
```

**Query Parameters**:
- `keyword` (String, optional): 검색 키워드
- `status` (String, optional): 상태 필터
- `roundId` (Long, optional): 라운드 필터
- `targetAdTaskId` (Long, optional): 대상 광고 필터
- `postType` (String, optional): 게시글 유형 필터
- `minCost` (BigDecimal, optional): 최소 비용
- `maxCost` (BigDecimal, optional): 최대 비용
- `page` (Integer, default: 0): 페이지 번호
- `size` (Integer, default: 20): 페이지 크기

**cURL 예제**:
```bash
# 키워드로 검색
curl -X GET "http://localhost:8080/api/posts/search?keyword=AI"

# 상태로 필터링
curl -X GET "http://localhost:8080/api/posts/search?status=PUBLISHED"

# 복합 검색
curl -X GET "http://localhost:8080/api/posts/search?keyword=마케팅&status=PUBLISHED&page=0&size=5"
```

**응답**: 게시글 목록과 동일한 Page 형식

### 7. 작성자별 게시글 조회
```http
GET /api/posts/author/{authorId}
```

**cURL 예제**:
```bash
curl -X GET "http://localhost:8080/api/posts/author/1?page=0&size=10"
```

**응답**: 게시글 목록과 동일한 Page 형식

### 8. 추천 게시글 조회
```http
GET /api/posts/featured
```

**cURL 예제**:
```bash
curl -X GET "http://localhost:8080/api/posts/featured?page=0&size=5"
```

**응답**: 게시글 목록과 동일한 Page 형식

### 9. 인기 게시글 조회
```http
GET /api/posts/popular
```

**cURL 예제**:
```bash
curl -X GET "http://localhost:8080/api/posts/popular?page=0&size=5"
```

**응답**: 게시글 목록과 동일한 Page 형식

### 10. 게시글 발행
```http
POST /api/posts/{id}/publish
```

**cURL 예제**:
```bash
curl -X POST "http://localhost:8080/api/posts/1/publish" \
  -H "X-User-Id: 1"
```

**응답**: PostResponse 형식

### 11. 게시글 숨김
```http
POST /api/posts/{id}/hide
```

**cURL 예제**:
```bash
curl -X POST "http://localhost:8080/api/posts/1/hide" \
  -H "X-User-Id: 1"
```

**응답**: PostResponse 형식

---

## 🔄 순환발주 관리 API

### 1. 라운드별 순환발주 할당 생성 및 DRAFT 게시글 자동 생성
```http
POST /api/posts/rounds/{roundId}/circular-assignments
```

**기능**: 
- Python 순환발주 알고리즘을 사용하여 정확한 원금보장 계산
- 각 참여자별로 n-1개의 DRAFT 상태 게시글 자동 생성
- 중복 방지: 이미 존재하는 (작성자 + 대상광고) 조합은 건너뜀

**cURL 예제**:
```bash
# 라운드 1에 대한 순환발주 할당 생성
curl -X POST "http://localhost:8080/api/posts/rounds/31/circular-assignments" \
  -H "Content-Type: application/json"
```

**응답**:
```json
[
  {
    "id": 1,
    "title": "[초안] AI 마케팅 솔루션 소개글",
    "content": "# AI 마케팅 솔루션 소개\n\n이 글은 스마트애드의 'AI 마케팅 솔루션''에 대한 소개글입니다.\n\n[여기에 내용을 작성해주세요]",
    "authorId": 1,
    "authorName": "A 회사",
    "roundId": 1,
    "roundTitle": "Q4 마케팅 캠페인",
    "targetAdTaskId": 2,
    "targetAdContent": "AI 기반 타겟 마케팅 광고",
    "assignedCost": 2500.00,
    "postType": "BLOG_INTRODUCTION",
    "status": "DRAFT",
    "postStartDate": "2024-01-01T00:00:00",
    "postEndDate": "2024-01-31T23:59:59",
    "publishedAt": null,
    "createdAt": "2024-10-09T10:30:00",
    "updatedAt": "2024-10-09T10:30:00",
    "isActive": false
  }
]
```

### 2. 순환발주 할당 현황 조회
```http
GET /api/posts/rounds/{roundId}/circular-assignments
```

**cURL 예제**:
```bash
curl -X GET "http://localhost:8080/api/posts/rounds/1/circular-assignments"
```

**응답**:
```json
{
  "roundId": 1,
  "message": "순환발주 할당 현황 조회 기능은 추후 구현 예정"
}
```

**참고**: 현재는 기본 응답만 제공하며, 상세 할당 현황은 추후 구현 예정

---

## 📊 통계 및 관리 API

### 1. 게시글 통계
```http
GET /api/posts/stats
```

**cURL 예제**:
```bash
curl -X GET "http://localhost:8080/api/posts/stats"
```

**응답**:
```json
{
  "totalPosts": 25,
  "publishedPosts": 18,
  "draftPosts": 5,
  "hiddenPosts": 2
}
```

### 2. 광고별 게시글 통계
```http
GET /api/posts/ads/{adTaskId}/post-stats
```

**cURL 예제**:
```bash
curl -X GET "http://localhost:8080/api/posts/ads/2/post-stats"
```

**응답**:
```json
{
  "adTaskId": 2,
  "message": "광고별 게시글 통계 기능은 추후 구현 예정"
}
```

### 3. 라운드별 게시글 통계
```http
GET /api/posts/rounds/{roundId}/post-stats
```

**cURL 예제**:
```bash
curl -X GET "http://localhost:8080/api/posts/rounds/1/post-stats"
```

**응답**:
```json
{
  "roundId": 1,
  "message": "라운드별 게시글 통계 기능은 추후 구현 예정"
}
```

### 4. 순환발주 밸런스 체크
```http
GET /api/posts/rounds/{roundId}/balance-check
```

**cURL 예제**:
```bash
curl -X GET "http://localhost:8080/api/posts/rounds/1/balance-check"
```

**응답**:
```json
{
  "roundId": 1,
  "message": "라운드 밸런스 체크 기능은 추후 구현 예정"
}
```

**참고**: 실제 밸런스 체크는 데이터베이스 뷰 `circular_balance_check`를 통해 가능하며, 향후 API로 연동 예정

### 5. 라운드 완성도 체크
```http
GET /api/posts/rounds/{roundId}/completion-status
```

**cURL 예제**:
```bash
curl -X GET "http://localhost:8080/api/posts/rounds/1/completion-status"
```

**응답**:
```json
{
  "roundId": 1,
  "message": "라운드 완성도 체크 기능은 추후 구현 예정"
}
```

**참고**: 실제 완성도 체크는 데이터베이스 뷰 `round_completion_status`를 통해 가능하며, 향후 API로 연동 예정

---

## 🏷️ 데이터 타입 정의

### PostType (게시글 유형)
- `BLOG_INTRODUCTION`: 블로그 소개글
- `REVIEW`: 후기
- `TUTORIAL`: 튜토리얼  
- `SHOWCASE`: 쇼케이스

### PostStatus (게시글 상태)
- `DRAFT`: 임시저장
- `PUBLISHED`: 공개
- `HIDDEN`: 숨김

---

## ⚠️ 에러 응답

### 공통 에러 형식
```json
{
  "error": "Invalid request",
  "message": "순환발주 계산 중 오류가 발생했습니다",
  "timestamp": "2024-10-09T10:30:00"
}
```

### 주요 에러 코드
- `400 Bad Request`: 잘못된 요청 (필수 파라미터 누락 등)
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: 권한 없음 (다른 사용자의 게시글 수정 시도 등)
- `404 Not Found`: 리소스를 찾을 수 없음
- `409 Conflict`: 중복 생성 시도 (동일 광고에 대한 중복 소개글)
- `422 Unprocessable Entity`: 순환발주 계산 실패
- `500 Internal Server Error`: 서버 내부 오류

---

## 🔄 비즈니스 플로우

### 1. 순환발주 기반 게시글 작성 플로우 (개선된 버전)
```bash
# 1. 라운드별 순환발주 할당 생성 및 DRAFT 게시글 자동 생성
POST /api/posts/rounds/1/circular-assignments
# → 모든 참여자의 n-1개 DRAFT 게시글이 자동 생성됨
# → 순환발주 알고리즘으로 정확한 assignedCost 계산됨

# 2. 할당된 작업 확인 (선택사항)
GET /api/posts/rounds/1/circular-assignments

# 3. 각 사용자는 자신의 DRAFT 게시글을 수정
PUT /api/posts/{postId}
{
  "title": "수정된 제목",
  "content": "실제 소개 내용 작성",
  "status": "DRAFT"
}

# 4. 게시글 발행
POST /api/posts/{postId}/publish

# 5. 완성도 체크 (선택사항)
GET /api/posts/rounds/1/completion-status

# 6. 밸런스 검증 (선택사항)
GET /api/posts/rounds/1/balance-check
```

### 2. 개선된 게시글 관리 플로우
```bash
# 1. 자동 생성된 DRAFT 게시글 확인
GET /api/posts/author/{authorId}
# → status: "DRAFT"인 게시글들 조회

# 2. DRAFT 게시글 내용 수정
PUT /api/posts/{postId}
{
  "title": "완성된 제목",
  "content": "완성된 내용",
  "status": "DRAFT"
}

# 3. 게시글 발행
POST /api/posts/{postId}/publish

# 4. 활성 상태 확인 (라운드 기간 중)
GET /api/posts/{postId}
# → isActive: true/false (postStartDate와 postEndDate 기준)

# 5. 필요시 게시글 숨김
POST /api/posts/{postId}/hide
```

---

## 🎯 비즈니스 규칙

### 순환발주 시스템 규칙
1. **원금보장**: 각 참여자가 받는 총 금액 = 투자한 금액 (Python 알고리즘으로 정확히 계산)
2. **n-1 법칙**: n명 참여 시 각자 n-1개 게시글 작성 (자신 제외, 자동 생성됨)
3. **라운드 동기화**: 게시 기간이 라운드 일정과 완전 일치 (postStartDate = round.startDate)
4. **중복 방지**: 동일 광고에 대해 사용자당 1개 게시글만 (자동 생성 시 체크)
5. **비용 정확성**: 수학적 알고리즘으로 정확한 비용 할당 (Round.orderAmount 기반)
6. **자동화**: 라운드 시작 시 모든 DRAFT 게시글이 자동 생성되어 사용자는 내용만 작성

### 게시글 작성 규칙
1. **자동 생성**: 라운드 시작 시 시스템이 모든 DRAFT 게시글 자동 생성
2. **대상 제한**: 자신의 광고는 소개할 수 없음 (타인 광고만, 자동 생성 시 적용)
3. **기간 제한**: 라운드 기간 중에만 게시글 활성화 (isActive 계산)
4. **상태 관리**: 자동 생성된 DRAFT → 사용자 수정 → PUBLISHED → (HIDDEN)
5. **수정 권한**: 작성자만 자신의 게시글 수정 가능
6. **할당 비용**: 순환발주 알고리즘으로 계산된 정확한 assignedCost 자동 설정

### 활성화 조건
- 게시글이 `PUBLISHED` 상태
- 현재 시각이 `postStartDate`와 `postEndDate` 사이
- 라운드가 진행 중인 상태

---

## 📋 구현 현황 및 데이터베이스 스키마

### 🚀 구현 완료 기능
1. **CircularAssignmentService**: Python 순환발주 알고리즘을 Kotlin으로 완전 구현
   - 2명, 3명, 4명 이상 케이스별 정확한 해법
   - Round.orderAmount 기반 실제 투자금액 연동
2. **자동 DRAFT 생성**: PostService.generateDraftPostsForRound() 구현
   - 라운드 시작 시 참여자별 n-1개 게시글 자동 생성
   - 순환발주 계산된 assignedCost 자동 할당
3. **API 엔드포인트**: 모든 순환발주 관련 엔드포인트 추가 완료
4. **데이터베이스 뷰**: 밸런스 체크 및 통계 뷰 구현 완료

### 📊 데이터베이스 스키마
이 API는 다음 DDL을 기반으로 구현됩니다:
- `/db/posts_final.sql` - 순환발주 기반 게시글 시스템 완전판

주요 뷰 (구현 완료):
- `circular_balance_check` - 순환발주 밸런스 검증
- `round_completion_status` - 라운드별 완성도
- `active_posts` - 현재 활성 게시글
- `round_post_stats` - 라운드별 통계

### 📋 추후 개선 예정
- 밸런스 체크 API와 데이터베이스 뷰 연동
- 완성도 체크 API와 데이터베이스 뷰 연동
- 광고별/라운드별 통계 API 상세 구현

---

*🎉 이 시스템은 수학적으로 정확한 순환발주 알고리즘을 통해 완벽한 원금보장을 제공하며, 자동화된 게시글 생성으로 사용자 편의성을 극대화했습니다.*