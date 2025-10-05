# REST API 문서

## 회원가입 API

### 회원가입
회원가입을 처리하는 API입니다.

**Endpoint:** `POST /api/members/register`

**Content-Type:** `multipart/form-data`

#### Request Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| email | String | Y | 이메일 주소 |
| password | String | Y | 비밀번호 |
| companyName | String | Y | 회사명 |
| businessRegistrationNumber | String | Y | 사업자등록번호 |
| contactNumber | String | Y | 연락처 |
| businessRegistrationFile | File | Y | 사업자등록증 파일 |
| telecommunicationSalesFile | File | Y | 통신판매업신고증 파일 |

#### Response
**Success (200 OK)**
```json
{
  "id": 1,
  "email": "user@example.com",
  "companyName": "테스트 회사",
  "businessRegistrationNumber": "123-45-67890",
  "contactNumber": "010-1234-5678",
  "isPremium": false,
  "rentalStatus": "INACTIVE",
  "currentRentalExpiry": null,
  "message": "회원가입이 완료되었습니다."
}
```

**Error (400 Bad Request)**
```json
{
  "id": 0,
  "email": "user@example.com",
  "companyName": "테스트 회사",
  "businessRegistrationNumber": "123-45-67890",
  "contactNumber": "010-1234-5678",
  "isPremium": false,
  "rentalStatus": "INACTIVE",
  "currentRentalExpiry": null,
  "message": "이미 등록된 이메일입니다."
}
```

**Error (500 Internal Server Error)**
```json
{
  "id": 0,
  "email": "user@example.com",
  "companyName": "테스트 회사",
  "businessRegistrationNumber": "123-45-67890",
  "contactNumber": "010-1234-5678",
  "isPremium": false,
  "rentalStatus": "INACTIVE",
  "currentRentalExpiry": null,
  "message": "서버 오류가 발생했습니다."
}
```

#### cURL 예제
```bash
curl -X POST http://localhost:8080/api/members/register \
  -F "email=user@example.com" \
  -F "password=password123" \
  -F "companyName=테스트 회사" \
  -F "businessRegistrationNumber=123-45-67890" \
  -F "contactNumber=010-1234-5678" \
  -F "businessRegistrationFile=@./business_registration.pdf" \
  -F "telecommunicationSalesFile=@./telecom_sales.pdf"
```

#### 검증 규칙
- 이메일: 중복 불가
- 사업자등록번호: 중복 불가
- 파일: 비어있지 않은 파일만 허용
- 비밀번호: BCrypt로 암호화되어 저장

#### 에러 메시지
- "이미 등록된 이메일입니다."
- "이미 등록된 사업자등록번호입니다."
- "파일이 비어있습니다."
- "파일명이 없습니다."
- "서버 오류가 발생했습니다."

---

## 통합 회원가입 API

### 완전한 회원가입 (회원가입 + 계약 + 임대권 구매)
회원가입, 계약 체결, 임대권 구매를 하나의 트랜잭션으로 처리하는 API입니다.

**Endpoint:** `POST /api/members/complete-registration`

**Content-Type:** `multipart/form-data`

#### Request Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| email | String | Y | 이메일 주소 |
| password | String | Y | 비밀번호 |
| companyName | String | Y | 회사명 |
| businessRegistrationNumber | String | Y | 사업자등록번호 |
| contactNumber | String | Y | 연락처 |
| businessRegistrationFile | File | Y | 사업자등록증 파일 |
| telecommunicationSalesFile | File | Y | 통신판매업신고증 파일 |
| rentalContractAgreed | Boolean | Y | 임대권 구매 계약 동의 |
| serviceContractAgreed | Boolean | Y | 광고 게시 용역 계약 동의 |
| marketingAgreed | Boolean | Y | 마케팅 정보 수신 동의 |
| durationYears | Integer | N | 임대권 기간(년) (기본값: 1년) |

#### Response
**Success (200 OK)**
```json
{
  "success": true,
  "message": "가입 및 계약이 완료되었습니다.",
  "memberId": 1,
  "email": "user@example.com",
  "companyName": "테스트 회사",
  "businessRegistrationNumber": "123-45-67890",
  "contactNumber": "010-1234-5678",
  "isPremium": false,
  "rentalStatus": "ACTIVE",
  "contractId": 1,
  "contractDate": "2024-01-01T10:00:00",
  "contractVersion": "1.0",
  "rentalRightsId": 1,
  "purchaseDate": "2024-01-01",
  "expiryDate": "2025-01-01",
  "rentalAmount": 100000000,
  "hasValidContract": true,
  "hasValidRentalRights": true
}
```

**Error (400 Bad Request)**
```json
{
  "success": false,
  "message": "이미 등록된 이메일입니다.",
  "memberId": 0,
  "email": "user@example.com",
  "companyName": "테스트 회사",
  "businessRegistrationNumber": "123-45-67890",
  "contactNumber": "010-1234-5678",
  "isPremium": false,
  "rentalStatus": "INACTIVE",
  "contractId": 0,
  "rentalRightsId": 0,
  "rentalAmount": 0,
  "hasValidContract": false,
  "hasValidRentalRights": false
}
```

#### cURL 예제
```bash
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "email=user@example.com" \
  -F "password=password123" \
  -F "companyName=테스트 회사" \
  -F "businessRegistrationNumber=123-45-67890" \
  -F "contactNumber=010-1234-5678" \
  -F "businessRegistrationFile=@./business_registration.pdf" \
  -F "telecommunicationSalesFile=@./telecom_sales.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=false" \
  -F "durationYears=1"
```

#### 특징
- **원자적 처리**: 모든 단계가 성공하거나 모든 단계가 롤백됩니다
- **트랜잭션 보장**: 중간 단계에서 실패 시 이전 단계들이 자동 롤백됩니다
- **완전한 서비스 준비**: 응답 성공 시 바로 서비스 이용 가능합니다

#### 처리 단계
1. 이메일 및 사업자등록번호 중복 확인
2. 파일 업로드 및 회원 정보 저장
3. 서비스 계약 체결
4. 임대권 구매
5. 회원 상태를 ACTIVE로 업데이트

#### 에러 메시지
- "이미 등록된 이메일입니다."
- "이미 등록된 사업자등록번호입니다."
- "파일이 비어있습니다."
- "계약 체결에 실패했습니다."
- "임대권 구매에 실패했습니다."

---

## 이메일 중복 체크 API

### 이메일 중복 확인
회원가입 전 이메일 중복 여부를 확인하는 API입니다.

**Endpoint:** `GET /api/members/check-email`

#### Request Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| email | String | Y | 확인할 이메일 주소 |

#### Response
**Success (200 OK)**
```json
{
  "available": true,
  "message": "사용 가능한 이메일입니다."
}
```

**Duplicate Email (200 OK)**
```json
{
  "available": false,
  "message": "이미 등록된 이메일입니다."
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/members/check-email?email=user@example.com"
```

---

## 사업자등록번호 중복 체크 API

### 사업자등록번호 중복 확인
회원가입 전 사업자등록번호 중복 여부를 확인하는 API입니다.

**Endpoint:** `GET /api/members/check-business-number`

#### Request Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| businessRegistrationNumber | String | Y | 확인할 사업자등록번호 |

#### Response
**Success (200 OK)**
```json
{
  "available": true,
  "message": "사용 가능한 사업자등록번호입니다."
}
```

**Duplicate Business Number (200 OK)**
```json
{
  "available": false,
  "message": "이미 등록된 사업자등록번호입니다."
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/members/check-business-number?businessRegistrationNumber=123-45-67890"
```

---

## 로그인 API

### 회원 로그인
회원 로그인을 처리하는 API입니다.

**Endpoint:** `POST /api/members/login`

**Content-Type:** `application/json`

#### Request Body
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| email | String | Y | 로그인 이메일 |
| password | String | Y | 비밀번호 |

#### Response
**Success (200 OK)**
```json
{
  "id": 1,
  "email": "user@example.com",
  "companyName": "테스트 회사",
  "businessRegistrationNumber": "123-45-67890",
  "contactNumber": "010-1234-5678",
  "isPremium": false,
  "rentalStatus": "ACTIVE",
  "currentRentalExpiry": "2025-01-01",
  "hasValidContract": true,
  "message": "로그인 성공",
  "success": true
}
```

**Error (401 Unauthorized)**
```json
{
  "id": 0,
  "email": "user@example.com",
  "companyName": "",
  "businessRegistrationNumber": "",
  "contactNumber": "",
  "isPremium": false,
  "rentalStatus": "INACTIVE",
  "currentRentalExpiry": null,
  "hasValidContract": false,
  "message": "등록되지 않은 이메일입니다.",
  "success": false
}
```

#### cURL 예제
```bash
curl -X POST http://localhost:8080/api/members/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@cnc.com",
    "password": "test123!@#"
  }'
```

#### 응답 필드 설명
- `rentalStatus`: 임대권 상태 (ACTIVE, EXPIRED, INACTIVE)
- `currentRentalExpiry`: 현재 임대권 만료일
- `hasValidContract`: 유효한 서비스 계약 보유 여부

---

## 계약 관리 API

### 서비스 계약 체결
서비스 이용 계약을 체결하는 API입니다.

**Endpoint:** `POST /api/contracts/create`

**Content-Type:** `application/json`

#### Request Body
```json
{
  "memberId": 1,
  "rentalContractAgreed": true,
  "serviceContractAgreed": true,
  "marketingAgreed": false
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| memberId | Long | Y | 회원 ID |
| rentalContractAgreed | Boolean | Y | 임대권 구매 계약 동의 |
| serviceContractAgreed | Boolean | Y | 광고 게시 용역 계약 동의 |
| marketingAgreed | Boolean | Y | 마케팅 정보 수신 동의 |

#### Response
**Success (200 OK)**
```json
{
  "id": 1,
  "memberId": 1,
  "rentalContractAgreed": true,
  "serviceContractAgreed": true,
  "marketingAgreed": false,
  "contractDate": "2024-01-01T10:00:00",
  "contractVersion": "1.0",
  "isActive": true,
  "message": "계약이 성공적으로 체결되었습니다.",
  "success": true
}
```

#### cURL 예제
```bash
curl -X POST http://localhost:8080/api/contracts/create \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "rentalContractAgreed": true,
    "serviceContractAgreed": true,
    "marketingAgreed": false
  }'
```

---

### 활성 계약 조회
회원의 활성화된 계약을 조회하는 API입니다.

**Endpoint:** `GET /api/contracts/member/{memberId}/active`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |

#### Response
**Success (200 OK)**
```json
{
  "id": 1,
  "memberId": 1,
  "rentalContractAgreed": true,
  "serviceContractAgreed": true,
  "marketingAgreed": false,
  "contractDate": "2024-01-01T10:00:00",
  "contractVersion": "1.0",
  "isActive": true,
  "message": "활성화된 계약이 존재합니다.",
  "success": true
}
```

---

### 계약 유효성 체크
회원의 유효한 계약 보유 여부를 체크하는 API입니다.

**Endpoint:** `GET /api/contracts/member/{memberId}/check`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |

#### Response
```json
{
  "hasValidContract": true
}
```

---

## 임대권 관리 API

### 임대권 구매
새로운 임대권을 구매하는 API입니다.

**Endpoint:** `POST /api/rental-rights/purchase`

**Content-Type:** `application/json`

#### Request Body
```json
{
  "memberId": 1,
  "purchaseDate": "2024-01-01",
  "durationYears": 1
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| memberId | Long | Y | 회원 ID |
| purchaseDate | LocalDate | N | 구매일 (기본값: 오늘) |
| durationYears | Integer | N | 기간(년) (기본값: 1년) |

#### Response
**Success (200 OK)**
```json
{
  "id": 1,
  "memberId": 1,
  "purchaseDate": "2024-01-01",
  "expiryDate": "2025-01-01",
  "rentalAmount": 100000000,
  "status": "ACTIVE",
  "autoRenewal": false,
  "renewalNoticeSent": false,
  "message": "임대권이 성공적으로 구매되었습니다.",
  "success": true
}
```

#### cURL 예제
```bash
curl -X POST http://localhost:8080/api/rental-rights/purchase \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "purchaseDate": "2024-01-01",
    "durationYears": 1
  }'
```

---

### 활성 임대권 조회
회원의 활성화된 임대권 목록을 조회하는 API입니다.

**Endpoint:** `GET /api/rental-rights/member/{memberId}/active`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |

#### Response
**Success (200 OK)**
```json
[
  {
    "id": 1,
    "memberId": 1,
    "purchaseDate": "2024-01-01",
    "expiryDate": "2025-01-01",
    "rentalAmount": 100000000,
    "status": "ACTIVE",
    "autoRenewal": false,
    "renewalNoticeSent": false,
    "message": "활성화된 임대권입니다.",
    "success": true
  }
]
```

---

### 임대권 유효성 체크
회원의 유효한 임대권 보유 여부를 체크하는 API입니다.

**Endpoint:** `GET /api/rental-rights/member/{memberId}/check`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |

#### Response
```json
{
  "hasValidRentalRights": true
}
```

---

### 임대권 갱신
기존 임대권을 갱신하는 API입니다.

**Endpoint:** `POST /api/rental-rights/{rentalRightsId}/renew`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| rentalRightsId | Long | Y | 임대권 ID |

#### Request Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| durationYears | Integer | N | 갱신 기간(년) (기본값: 1년) |

#### Response
**Success (200 OK)**
```json
{
  "id": 2,
  "memberId": 1,
  "purchaseDate": "2024-01-01",
  "expiryDate": "2025-01-01",
  "rentalAmount": 100000000,
  "status": "ACTIVE",
  "autoRenewal": false,
  "renewalNoticeSent": false,
  "message": "임대권이 성공적으로 갱신되었습니다.",
  "success": true
}
```

#### cURL 예제
```bash
curl -X POST "http://localhost:8080/api/rental-rights/1/renew?durationYears=1"
```

---

### 만료 예정 임대권 조회
만료 예정인 임대권 목록을 조회하는 API입니다.

**Endpoint:** `GET /api/rental-rights/expiring`

#### Request Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| days | Integer | N | 만료까지 남은 일수 (기본값: 30일) |

#### Response
**Success (200 OK)**
```json
[
  {
    "id": 1,
    "memberId": 1,
    "purchaseDate": "2024-01-01",
    "expiryDate": "2025-01-01",
    "rentalAmount": 100000000,
    "status": "ACTIVE",
    "autoRenewal": false,
    "renewalNoticeSent": false,
    "message": "만료 예정인 임대권입니다.",
    "success": true
  }
]
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/rental-rights/expiring?days=30"
```

---

## 라운드 관리 API

### 라운드 생성
새로운 라운드를 생성하는 API입니다.

**Endpoint:** `POST /api/rounds`

**Content-Type:** `application/json`

#### Request Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| createdById | Long | Y | 라운드 생성자의 회원 ID (query parameter) |

#### Request Body
```json
{
  "title": "헬스케어 라운드 #1",
  "description": "헬스케어 분야 광고 라운드입니다.",
  "category": "헬스케어",
  "orderAmount": 100000000.00,
  "templateCost": 10000000.00,
  "aiGenerationCost": 20000000.00,
  "targetingPostingCost": 30000000.00,
  "serverRentalCost": 25000000.00,
  "otherCosts": 15000000.00,
  "startDate": "2024-01-01T09:00:00",
  "endDate": "2024-01-31T18:00:00",
  "maxParticipants": 50
}
```

| 필드 | 타입 | 필수 | 설명 | 제약사항 |
|-----|------|------|------|----------|
| title | String | Y | 라운드 제목 | 최대 255자 |
| description | String | N | 라운드 설명 | 최대 1000자 |
| category | String | N | 라운드 카테고리 | 최대 100자 |
| orderAmount | BigDecimal | Y | 총 발주 금액 | 0.01 이상 |
| templateCost | BigDecimal | N | 템플릿 비용 | 기본값: 0 |
| aiGenerationCost | BigDecimal | N | AI 기반 광고 생성비 | 기본값: 0 |
| targetingPostingCost | BigDecimal | N | 타게팅 게시비 | 기본값: 0 |
| serverRentalCost | BigDecimal | N | 서버 임대비 | 기본값: 0 |
| otherCosts | BigDecimal | N | 기타 비용 | 기본값: 0 |
| startDate | LocalDateTime | Y | 라운드 시작일 | 현재 시간 이후 |
| endDate | LocalDateTime | Y | 라운드 종료일 | 시작일 이후 |
| maxParticipants | Integer | N | 최대 참여자 수 | 1 이상 |

#### Response
**Success (201 Created)**
```json
{
  "id": 1,
  "roundNumber": "Round #1",
  "title": "헬스케어 라운드 #1",
  "description": "헬스케어 분야 광고 라운드입니다.",
  "category": "헬스케어",
  "orderAmount": 100000000.00,
  "templateCost": 10000000.00,
  "aiGenerationCost": 20000000.00,
  "targetingPostingCost": 30000000.00,
  "serverRentalCost": 25000000.00,
  "otherCosts": 15000000.00,
  "startDate": "2024-01-01T09:00:00",
  "endDate": "2024-01-31T18:00:00",
  "status": "ACTIVE",
  "maxParticipants": 50,
  "createdById": 1,
  "createdAt": "2024-01-01T08:00:00"
}
```

#### cURL 예제
```bash
curl -X POST http://localhost:8080/api/rounds?createdById=1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "헬스케어 라운드 #1",
    "description": "헬스케어 분야 광고 라운드입니다.",
    "category": "헬스케어",
    "orderAmount": 100000.00,
    "startDate": "2024-01-01T09:00:00",
    "endDate": "2024-01-31T18:00:00",
    "maxParticipants": 50
  }'
```

---

### 라운드 목록 조회
라운드 목록을 조회하는 API입니다. 상태별 필터링과 페이징을 지원합니다.

**Endpoint:** `GET /api/rounds`

#### Request Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| status | String | N | 라운드 상태 (ACTIVE, CLOSED, PENDING) |
| page | Integer | N | 페이지 번호 (0부터 시작, 기본값: 0) |
| size | Integer | N | 페이지 크기 (기본값: 20) |
| sort | String | N | 정렬 조건 (예: createdAt,desc) |

#### Response
**Success (200 OK)**
```json
{
  "content": [
    {
      "id": 1,
      "roundNumber": "Round #1",
      "title": "헬스케어 라운드 #1",
      "description": "헬스케어 분야 광고 라운드입니다.",
      "category": "헬스케어",
      "orderAmount": 100000.00,
      "startDate": "2024-01-01T09:00:00",
      "endDate": "2024-01-31T18:00:00",
      "status": "ACTIVE",
      "maxParticipants": 50,
      "createdById": 1,
      "createdAt": "2024-01-01T08:00:00"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1
}
```

#### cURL 예제
```bash
# 전체 라운드 조회
curl -X GET "http://localhost:8080/api/rounds"

# 활성 라운드만 조회
curl -X GET "http://localhost:8080/api/rounds?status=ACTIVE"

# 페이징 적용
curl -X GET "http://localhost:8080/api/rounds?page=0&size=10&sort=createdAt,desc"
```

---

### 라운드 상세 조회
특정 라운드의 상세 정보를 조회하는 API입니다.

**Endpoint:** `GET /api/rounds/{id}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| id | Long | Y | 라운드 ID |

#### Response
**Success (200 OK)**
```json
{
  "id": 1,
  "roundNumber": "Round #1",
  "title": "헬스케어 라운드 #1",
  "description": "헬스케어 분야 광고 라운드입니다.",
  "category": "헬스케어",
  "orderAmount": 100000.00,
  "startDate": "2024-01-01T09:00:00",
  "endDate": "2024-01-31T18:00:00",
  "status": "ACTIVE",
  "maxParticipants": 50,
  "createdById": 1,
  "createdAt": "2024-01-01T08:00:00"
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
curl -X GET "http://localhost:8080/api/rounds/1"
```

---

### 회원별 라운드 조회
특정 회원이 생성한 라운드 목록을 조회하는 API입니다.

**Endpoint:** `GET /api/rounds/member/{memberId}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |

#### Request Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| page | Integer | N | 페이지 번호 (0부터 시작, 기본값: 0) |
| size | Integer | N | 페이지 크기 (기본값: 20) |

#### Response
**Success (200 OK)**
```json
{
  "content": [
    {
      "id": 1,
      "roundNumber": "Round #1",
      "title": "헬스케어 라운드 #1",
      "description": "헬스케어 분야 광고 라운드입니다.",
      "category": "헬스케어",
      "orderAmount": 100000.00,
      "startDate": "2024-01-01T09:00:00",
      "endDate": "2024-01-31T18:00:00",
      "status": "ACTIVE",
      "maxParticipants": 50,
      "createdById": 1,
      "createdAt": "2024-01-01T08:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/rounds/member/1?page=0&size=10"
```

#### 라운드 상태 (RoundStatus)
- `ACTIVE`: 활성 상태 (참여 가능)
- `CLOSED`: 종료된 상태
- `PENDING`: 대기 상태

#### 검증 규칙
- 라운드 제목: 필수, 최대 255자
- 총 발주 금액: 필수, 0.01 이상
- 비용 세부항목: 선택사항, 0 이상
- 시작일: 필수, 현재 시간 이후
- 종료일: 필수, 시작일 이후
- 최대 참여자 수: 1 이상 (선택사항)

#### 새로 추가된 필드
- `rentalStatus`: 회원의 임대권 상태 (ACTIVE, EXPIRED, INACTIVE)
- `currentRentalExpiry`: 현재 임대권 만료일
- `templateCost`: 템플릿 비용
- `aiGenerationCost`: AI 기반 광고 생성비
- `targetingPostingCost`: 타게팅 게시비
- `serverRentalCost`: 서버 임대비
- `otherCosts`: 기타 비용

#### 비즈니스 로직 플로우
1. **회원가입** → 임대권 상태는 INACTIVE로 시작
2. **계약 체결** → 필수 계약(임대권 구매 + 용역 계약) 동의 필요
3. **임대권 구매** → 1억원 고정, 1년 유효, 상태가 ACTIVE로 변경
4. **서비스 이용** → 유효한 계약 + 활성 임대권 보유 시 가능
5. **임대권 갱신** → 만료 전 갱신 또는 만료 후 재구매

#### 에러 메시지
- "Member not found with id: {id}" - 생성자 회원을 찾을 수 없음
- "Round not found with id: {id}" - 라운드를 찾을 수 없음
- "라운드 제목은 필수입니다" - 제목 누락
- "발주 금액은 0보다 커야 합니다" - 잘못된 금액
- "종료일은 시작일 이후여야 합니다" - 잘못된 날짜 범위