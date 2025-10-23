# 주문 관리 API 문서

## 주문 생성 및 관리 API

### 주문 생성
회원이 특정 라운드에 주문을 생성합니다.

**Endpoint:** `POST /api/orders/members/{memberId}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |

#### Request Body
```json
{
  "adTaskId": 47,
  "productName": "마케팅 광고 서비스",
  "quantity": 1,
  "requirements": "특별한 요구사항이 있습니다.",
  "deadline": "2025-12-31"
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| adTaskId | Long | Y | 광고 작업 ID (ad_tasks 테이블의 ID) |
| productName | String | Y | 상품명 |
| quantity | Integer | Y | 수량 |
| requirements | String | N | 특별 요구사항 |
| deadline | Date | N | 희망 마감일 |

#### Response
**Success (200 OK)**
```json
{
  "id": 123,
  "orderNumber": "ORD-2025-001",
  "adTaskId": 47,
  "adTaskTitle": "AdTask #47",
  "memberId": 16,
  "memberCompanyName": "테스트 회사",
  "memberEmail": "test@company.com",
  "productName": "마케팅 광고 서비스",
  "quantity": 1,
  "requirements": "특별한 요구사항이 있습니다.",
  "deadline": "2025-12-31",
  "startDate": null,
  "completionDate": null,
  "failureDate": null,
  "progressRate": 0,
  "failureReason": null,
  "status": "PENDING",
  "submittedAt": "2025-10-09T10:30:00",
  "reviewedAt": null,
  "reviewedByName": null,
  "notes": null,
  "createdAt": "2025-10-09T10:30:00",
  "updatedAt": "2025-10-09T10:30:00",
  "paymentInfo": null
}
```

#### cURL 예제
```bash
curl -X POST "http://localhost:8080/api/orders/members/16" \
  -H "Content-Type: application/json" \
  -d '{
    "adTaskId": 47,
    "productName": "마케팅 광고 서비스",
    "quantity": 1,
    "requirements": "특별한 요구사항이 있습니다.",
    "deadline": "2025-12-31"
  }'
curl -X POST "http://localhost:8080/api/orders/members/17" \
  -H "Content-Type: application/json" \
  -d '{
    "adTaskId": 45,
    "productName": "마케팅 광고 서비스",
    "quantity": 1,
    "requirements": "특별한 요구사항이 있습니다.",
    "deadline": "2025-12-31"
  }'
```

---

### 특정 주문 조회
주문 ID로 특정 주문의 상세 정보를 조회합니다.

**Endpoint:** `GET /api/orders/{orderId}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderId | Long | Y | 주문 ID |

#### Response
**Success (200 OK)**
```json
{
  "id": 123,
  "orderNumber": "ORD-2025-001",
  "roundId": 16,
  "roundTitle": "연말 Q4 라운드 #1",
  "memberId": 15,
  "memberCompanyName": "테스트 회사",
  "memberEmail": "test@company.com",
  "productName": "마케팅 광고 서비스",
  "quantity": 1,
  "requirements": "특별한 요구사항이 있습니다.",
  "deadline": "2025-12-31",
  "startDate": "2025-10-10",
  "completionDate": null,
  "failureDate": null,
  "progressRate": 50,
  "failureReason": null,
  "status": "IN_PROGRESS",
  "submittedAt": "2025-10-09T10:30:00",
  "reviewedAt": "2025-10-09T14:00:00",
  "reviewedByName": "관리자 회사",
  "notes": "승인 완료",
  "createdAt": "2025-10-09T10:30:00",
  "updatedAt": "2025-10-09T14:00:00",
  "paymentInfo": {
    "id": 456,
    "applicationNumber": "APP251009001",
    "paymentAmount": 50000.00,
    "depositorName": "테스트 회사",
    "paymentStatus": "CONFIRMED",
    "bankAccountNumber": "123-456-789",
    "bankName": "국민은행",
    "paymentConfirmedAt": "2025-10-09T12:00:00",
    "notes": "입금 확인 완료"
  }
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/123"
```

---

### 주문번호로 주문 조회
주문번호로 주문 정보를 조회합니다.

**Endpoint:** `GET /api/orders/number/{orderNumber}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderNumber | String | Y | 주문번호 |

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/number/ORD-2025-001"
```

---

### 회원별 주문 목록 조회
특정 회원의 모든 주문을 조회합니다.

**Endpoint:** `GET /api/orders/members/{memberId}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |

#### Response
**Success (200 OK)**
```json
{
  "memberId": 15,
  "memberCompanyName": "테스트 회사",
  "memberEmail": "test@company.com",
  "totalOrders": 2,
  "orders": [
    {
      "id": 123,
      "orderNumber": "ORD-2025-001",
      "roundId": 16,
      "roundTitle": "연말 Q4 라운드 #1",
      "memberId": 15,
      "memberCompanyName": "테스트 회사",
      "memberEmail": "test@company.com",
      "productName": "마케팅 광고 서비스",
      "quantity": 1,
      "requirements": null,
      "deadline": "2025-12-31",
      "startDate": null,
      "completionDate": null,
      "failureDate": null,
      "progressRate": 0,
      "failureReason": null,
      "status": "PAYMENT_WAITING",
      "submittedAt": "2025-10-09T10:30:00",
      "reviewedAt": null,
      "reviewedByName": null,
      "notes": null,
      "createdAt": "2025-10-09T10:30:00",
      "updatedAt": "2025-10-09T10:30:00",
      "paymentInfo": {
        "id": 456,
        "applicationNumber": "APP251009001",
        "paymentAmount": 50000.00,
        "depositorName": "테스트 회사",
        "paymentStatus": "WAITING",
        "bankAccountNumber": "123-456-789",
        "bankName": "국민은행",
        "paymentConfirmedAt": null,
        "notes": null
      }
    }
  ]
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/members/15"
```

---

### 주문 정보 수정
주문 정보를 수정합니다. (대기 상태의 주문만 수정 가능)

**Endpoint:** `PUT /api/orders/{orderId}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderId | Long | Y | 주문 ID |

#### Request Body
```json
{
  "deadline": "2025-12-31",
  "requirements": "수정된 요구사항입니다."
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| deadline | Date | N | 희망 마감일 |
| requirements | String | N | 특별 요구사항 |

#### Response
**Success (200 OK)**
```json
{
  "id": 123,
  "orderNumber": "ORD-2025-001",
  "roundId": 16,
  "roundTitle": "연말 Q4 라운드 #1",
  "memberId": 15,
  "memberCompanyName": "테스트 회사",
  "memberEmail": "test@company.com",
  "productName": "마케팅 광고 서비스",
  "quantity": 1,
  "requirements": "수정된 요구사항입니다.",
  "deadline": "2025-12-31",
  "startDate": null,
  "completionDate": null,
  "failureDate": null,
  "progressRate": 0,
  "failureReason": null,
  "status": "PAYMENT_WAITING",
  "submittedAt": "2025-10-09T10:30:00",
  "reviewedAt": null,
  "reviewedByName": null,
  "notes": null,
  "createdAt": "2025-10-09T10:30:00",
  "updatedAt": "2025-10-09T15:00:00",
  "paymentInfo": null
}
```

**Error (400 Bad Request)**
```json
{
  "error": "ORDER_MODIFICATION_NOT_ALLOWED",
  "message": "대기 상태의 주문만 수정할 수 있습니다.",
  "status": "IN_PROGRESS"
}
```

#### cURL 예제
```bash
curl -X PUT "http://localhost:8080/api/orders/123" \
  -H "Content-Type: application/json" \
  -d '{
    "deadline": "2025-12-31",
    "requirements": "수정된 요구사항입니다."
  }'
```

---

## 주문 상태 관리 API

### 주문 상태 업데이트
주문의 상태를 업데이트합니다. (관리자용)

**Endpoint:** `PUT /api/orders/{orderId}/status`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderId | Long | Y | 주문 ID |

#### Request Body
```json
{
  "status": "APPROVED",
  "notes": "검토 완료, 승인합니다.",
  "failureReason": null,
  "progressRate": 10
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| status | String | Y | 주문 상태 (PENDING, PAYMENT_WAITING, PAYMENT_CONFIRMED, APPROVED, IN_PROGRESS, COMPLETED, FAILED, CANCELLED) |
| notes | String | N | 관리자 메모 |
| failureReason | String | N | 실패 사유 (FAILED 상태일 때) |
| progressRate | Integer | N | 진행률 (0-100) |

#### Response
**Success (200 OK)**
```json
{
  "id": 123,
  "orderNumber": "ORD-2025-001",
  "roundId": 16,
  "roundTitle": "연말 Q4 라운드 #1",
  "memberId": 15,
  "memberCompanyName": "테스트 회사",
  "memberEmail": "test@company.com",
  "productName": "마케팅 광고 서비스",
  "quantity": 1,
  "requirements": null,
  "deadline": "2025-12-31",
  "startDate": null,
  "completionDate": null,
  "failureDate": null,
  "progressRate": 10,
  "failureReason": null,
  "status": "APPROVED",
  "submittedAt": "2025-10-09T10:30:00",
  "reviewedAt": "2025-10-09T14:00:00",
  "reviewedByName": null,
  "notes": "검토 완료, 승인합니다.",
  "createdAt": "2025-10-09T10:30:00",
  "updatedAt": "2025-10-09T14:00:00",
  "paymentInfo": null
}
```

#### cURL 예제
```bash
curl -X PUT "http://localhost:8080/api/orders/123/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "APPROVED",
    "notes": "검토 완료, 승인합니다.",
    "progressRate": 10
  }'
```

---

## 결제 관리 API

### 주문 결제 (결제 정보 생성)
주문에 대한 결제 정보를 생성합니다. 고객이 주문 생성 후 결제를 진행할 때 사용하는 API입니다.

**Endpoint:** `POST /api/orders/payments`

> **주문 결제 흐름**
> 1. 고객이 주문 생성 → 주문 상태: `PENDING`
> 2. 고객이 이 API로 결제 정보 생성 → 주문 상태: `PAYMENT_WAITING`, 결제 상태: `WAITING` 
> 3. 관리자가 입금 확인 → 결제 상태: `CONFIRMED`, 주문 상태: `PAYMENT_CONFIRMED`

#### Request Body
```json
{
  "orderId": 123,
  "paymentAmount": 50000.00,
  "depositorName": "테스트 회사",
  "bankAccountNumber": "123-456-789",
  "bankName": "국민은행",
  "notes": "입금 예정"
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| orderId | Long | Y | 주문 ID |
| paymentAmount | BigDecimal | Y | 입금 금액 |
| depositorName | String | N | 입금자명 |
| bankAccountNumber | String | Y | 입금 계좌번호 |
| bankName | String | Y | 은행명 |
| notes | String | N | 메모 |

#### Response
**Success (200 OK)**
```json
{
  "id": 456,
  "applicationNumber": "APP251009001",
  "paymentAmount": 50000.00,
  "depositorName": "테스트 회사",
  "paymentStatus": "WAITING",
  "bankAccountNumber": "123-456-789",
  "bankName": "국민은행",
  "paymentConfirmedAt": null,
  "notes": "입금 예정"
}
```

#### cURL 예제
```bash
curl -X POST "http://localhost:8080/api/orders/payments" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 7,
    "paymentAmount": 50000.00,
    "depositorName": "테스트 회사",
    "bankAccountNumber": "123-456-789",
    "bankName": "국민은행",
    "notes": "입금 예정"
  }'
curl -X POST "http://localhost:8080/api/orders/payments" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 8,
    "paymentAmount": 50000.00,
    "depositorName": "테스트 회사",
    "bankAccountNumber": "123-456-789",
    "bankName": "국민은행",
    "notes": "입금 예정"
  }'
```

---

### 결제 상태 업데이트
결제 상태를 업데이트합니다. (관리자용)

**Endpoint:** `PUT /api/orders/payments/{paymentId}/status`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| paymentId | Long | Y | 결제 ID |

#### Request Body
```json
{
  "paymentStatus": "CONFIRMED",
  "notes": "입금 확인 완료"
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| paymentStatus | String | Y | 결제 상태 (WAITING, CONFIRMED, FAILED) |
| notes | String | N | 메모 |

#### Response
**Success (200 OK)**
```json
{
  "id": 456,
  "applicationNumber": "APP251009001",
  "paymentAmount": 50000.00,
  "depositorName": "테스트 회사",
  "paymentStatus": "CONFIRMED",
  "bankAccountNumber": "123-456-789",
  "bankName": "국민은행",
  "paymentConfirmedAt": "2025-10-09T12:00:00",
  "notes": "입금 확인 완료"
}
```

#### cURL 예제
```bash
curl -X PUT "http://localhost:8080/api/orders/payments/3/status" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentStatus": "CONFIRMED",
    "notes": "입금 확인 완료"
  }'
curl -X PUT "http://localhost:8080/api/orders/payments/4/status" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentStatus": "CONFIRMED",
    "notes": "입금 확인 완료"
  }'
```

---

### 주문별 결제 내역 조회
특정 주문의 모든 결제 내역을 조회합니다.

**Endpoint:** `GET /api/orders/{orderId}/payments`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderId | Long | Y | 주문 ID |

#### Response
**Success (200 OK)**
```json
[
  {
    "id": 456,
    "applicationNumber": "APP251009001",
    "paymentAmount": 50000.00,
    "depositorName": "테스트 회사",
    "paymentStatus": "CONFIRMED",
    "bankAccountNumber": "123-456-789",
    "bankName": "국민은행",
    "paymentConfirmedAt": "2025-10-09T12:00:00",
    "notes": "입금 확인 완료"
  }
]
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/1/payments"
```

---

### 입금 대기중인 결제 목록 조회
입금 대기중인 모든 결제를 조회합니다. (관리자용)

**Endpoint:** `GET /api/orders/payments/pending`

#### Response
**Success (200 OK)**
```json
[
  {
    "id": 789,
    "applicationNumber": "APP251009002",
    "paymentAmount": 75000.00,
    "depositorName": "ABC 회사",
    "paymentStatus": "WAITING",
    "bankAccountNumber": "987-654-321",
    "bankName": "신한은행",
    "paymentConfirmedAt": null,
    "notes": null
  }
]
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/payments/pending"
```

---

## 결과물 파일 관리 API

### 결과물 파일 업로드
주문의 결과물 파일을 업로드합니다. (관리자용)

**Endpoint:** `POST /api/orders/{orderId}/files`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderId | Long | Y | 주문 ID |

#### Request Body (Multipart Form Data)
| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| files | MultipartFile[] | Y | 업로드할 파일들 |
| description | String | N | 파일 설명 |
| submitterName | String | N | 제출자명 |

#### Response
**Success (200 OK)**
```json
{
  "orderId": 123,
  "uploadedFiles": [
    {
      "id": 789,
      "fileName": "헬스케어_브랜딩_가이드.pdf",
      "originalFileName": "healthcare_branding_guide.pdf",
      "fileSize": 2457600,
      "fileType": "application/pdf",
      "filePath": "/uploads/orders/123/healthcare_branding_guide.pdf",
      "description": "헬스케어 브랜딩 가이드 최종본",
      "submitterName": "테크솔루션 주식회사",
      "uploadedAt": "2025-10-09T16:30:00"
    }
  ],
  "totalFiles": 1,
  "totalSize": 2457600
}
```

#### cURL 예제
```bash
curl -X POST "http://localhost:8080/api/orders/123/files" \
  -F "files=@healthcare_branding_guide.pdf" \
  -F "description=헬스케어 브랜딩 가이드 최종본" \
  -F "submitterName=테크솔루션 주식회사"
```

---

### 주문별 결과물 파일 목록 조회
특정 주문의 모든 결과물 파일을 조회합니다.

**Endpoint:** `GET /api/orders/{orderId}/files`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderId | Long | Y | 주문 ID |

#### Response
**Success (200 OK)**
```json
{
  "orderId": 123,
  "orderNumber": "ORD-2025-001",
  "orderTitle": "헬스케어 마케팅 광고 서비스",
  "orderStatus": "COMPLETED",
  "files": [
    {
      "id": 789,
      "fileName": "헬스케어_브랜딩_가이드.pdf",
      "originalFileName": "healthcare_branding_guide.pdf",
      "fileSize": 2457600,
      "fileType": "application/pdf",
      "filePath": "/uploads/orders/123/healthcare_branding_guide.pdf",
      "description": "헬스케어 브랜딩 가이드 최종본",
      "submitterName": "테크솔루션 주식회사",
      "uploadedAt": "2025-10-09T16:30:00",
      "downloadUrl": "/api/orders/123/files/789/download"
    },
    {
      "id": 790,
      "fileName": "광고_콘텐츠_v2.psd",
      "originalFileName": "ad_content_v2.psd",
      "fileSize": 16449280,
      "fileType": "application/octet-stream",
      "filePath": "/uploads/orders/123/ad_content_v2.psd",
      "description": "광고 콘텐츠 최종 디자인 파일",
      "submitterName": "테크솔루션 주식회사",
      "uploadedAt": "2025-10-09T16:35:00",
      "downloadUrl": "/api/orders/123/files/790/download"
    }
  ],
  "totalFiles": 2,
  "totalSize": 18906880
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/123/files"
```

---

### 결과물 파일 다운로드
특정 결과물 파일을 다운로드합니다.

**Endpoint:** `GET /api/orders/{orderId}/files/{fileId}/download`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderId | Long | Y | 주문 ID |
| fileId | Long | Y | 파일 ID |

#### Response
**Success (200 OK)**
- 파일 바이너리 데이터
- Content-Type: 파일의 MIME 타입
- Content-Disposition: attachment; filename="파일명"

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/123/files/789/download" \
  -o healthcare_branding_guide.pdf
```

---

### 결과물 파일 미리보기
파일의 메타데이터와 미리보기 정보를 조회합니다.

**Endpoint:** `GET /api/orders/{orderId}/files/{fileId}/preview`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderId | Long | Y | 주문 ID |
| fileId | Long | Y | 파일 ID |

#### Response
**Success (200 OK)**
```json
{
  "id": 789,
  "fileName": "헬스케어_브랜딩_가이드.pdf",
  "originalFileName": "healthcare_branding_guide.pdf",
  "fileSize": 2457600,
  "fileType": "application/pdf",
  "description": "헬스케어 브랜딩 가이드 최종본",
  "submitterName": "테크솔루션 주식회사",
  "uploadedAt": "2025-10-09T16:30:00",
  "previewAvailable": true,
  "previewUrl": "/api/orders/123/files/789/thumbnail",
  "downloadUrl": "/api/orders/123/files/789/download"
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/123/files/789/preview"
```

---

### 결과물 파일 삭제
결과물 파일을 삭제합니다. (관리자용)

**Endpoint:** `DELETE /api/orders/{orderId}/files/{fileId}`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderId | Long | Y | 주문 ID |
| fileId | Long | Y | 파일 ID |

#### Response
**Success (200 OK)**
```json
{
  "message": "파일이 성공적으로 삭제되었습니다.",
  "deletedFileId": 789,
  "deletedFileName": "헬스케어_브랜딩_가이드.pdf"
}
```

#### cURL 예제
```bash
curl -X DELETE "http://localhost:8080/api/orders/123/files/789"
```

---

### 주문별 결과물 일괄 다운로드
주문의 모든 결과물을 ZIP 파일로 다운로드합니다.

**Endpoint:** `GET /api/orders/{orderId}/files/download-all`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| orderId | Long | Y | 주문 ID |

#### Response
**Success (200 OK)**
- ZIP 파일 바이너리 데이터
- Content-Type: application/zip
- Content-Disposition: attachment; filename="주문번호_결과물.zip"

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/123/files/download-all" \
  -o ORD-2025-001_결과물.zip
```

---

## 회원별 주문 통계 API

### 회원별 주문 통계 조회
특정 회원의 상세 주문 통계를 조회합니다.

**Endpoint:** `GET /api/orders/members/{memberId}/statistics`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| period | String | N | 조회 기간 (thisMonth, last3Months, thisYear, all) 기본값: thisMonth |

#### Response
**Success (200 OK)**
```json
{
  "memberId": 15,
  "memberCompanyName": "테스트 회사",
  "memberEmail": "test@company.com",
  "period": "thisMonth",
  "periodStart": "2025-10-01",
  "periodEnd": "2025-10-31",
  "summary": {
    "totalOrders": 5,
    "pendingOrders": 1,
    "paymentWaitingOrders": 1,
    "paymentConfirmedOrders": 1,
    "approvedOrders": 0,
    "inProgressOrders": 1,
    "completedOrders": 1,
    "failedOrders": 0,
    "cancelledOrders": 0,
    "totalOrderAmount": 250000.00,
    "averageOrderAmount": 50000.00,
    "successRate": 80.0
  },
  "monthlyTrend": [
    {
      "month": "2025-08",
      "totalOrders": 2,
      "completedOrders": 2,
      "totalAmount": 100000.00
    },
    {
      "month": "2025-09",
      "totalOrders": 3,
      "completedOrders": 2,
      "totalAmount": 150000.00
    },
    {
      "month": "2025-10",
      "totalOrders": 5,
      "completedOrders": 1,
      "totalAmount": 250000.00
    }
  ],
  "statusDistribution": [
    {
      "status": "COMPLETED",
      "count": 1,
      "percentage": 20.0
    },
    {
      "status": "IN_PROGRESS",
      "count": 1,
      "percentage": 20.0
    },
    {
      "status": "PAYMENT_WAITING",
      "count": 1,
      "percentage": 20.0
    },
    {
      "status": "PAYMENT_CONFIRMED",
      "count": 1,
      "percentage": 20.0
    },
    {
      "status": "PENDING",
      "count": 1,
      "percentage": 20.0
    }
  ],
  "recentOrders": [
    {
      "id": 123,
      "orderNumber": "ORD-2025-001",
      "productName": "마케팅 광고 서비스",
      "status": "IN_PROGRESS",
      "progressRate": 75,
      "submittedAt": "2025-10-09T10:30:00"
    }
  ]
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/members/15/statistics?period=thisMonth"
```

---

### 전체 주문 통계 조회 (관리자용)
모든 회원의 주문 통계를 조회합니다.

**Endpoint:** `GET /api/orders/statistics/overview`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| period | String | N | 조회 기간 (thisMonth, last3Months, thisYear, all) 기본값: thisMonth |

#### Response
**Success (200 OK)**
```json
{
  "period": "thisMonth",
  "periodStart": "2025-10-01",
  "periodEnd": "2025-10-31",
  "overallSummary": {
    "totalOrders": 50,
    "totalMembers": 25,
    "totalOrderAmount": 2500000.00,
    "averageOrderAmount": 50000.00,
    "successRate": 85.0,
    "completionRate": 70.0
  },
  "statusSummary": {
    "pendingOrders": 5,
    "paymentWaitingOrders": 8,
    "paymentConfirmedOrders": 6,
    "approvedOrders": 3,
    "inProgressOrders": 12,
    "completedOrders": 15,
    "failedOrders": 1,
    "cancelledOrders": 0
  },
  "dailyTrend": [
    {
      "date": "2025-10-01",
      "newOrders": 2,
      "completedOrders": 1
    },
    {
      "date": "2025-10-02",
      "newOrders": 3,
      "completedOrders": 2
    }
  ],
  "topPerformers": [
    {
      "memberId": 15,
      "memberCompanyName": "테스트 회사",
      "totalOrders": 5,
      "completedOrders": 4,
      "totalAmount": 250000.00
    }
  ]
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/statistics/overview?period=thisMonth"
```

---

## 실시간 알림 API

### WebSocket 연결
주문 상태 변경에 대한 실시간 알림을 받기 위한 WebSocket 연결입니다.

**Endpoint:** `WS /api/orders/notifications/ws`

#### 연결 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID (쿼리 파라미터) |
| token | String | Y | 인증 토큰 (쿼리 파라미터) |

#### 수신 메시지 형식
```json
{
  "type": "ORDER_STATUS_CHANGED",
  "orderId": 123,
  "orderNumber": "ORD-2025-001",
  "oldStatus": "PAYMENT_WAITING",
  "newStatus": "PAYMENT_CONFIRMED",
  "message": "입금이 확인되었습니다.",
  "timestamp": "2025-10-09T12:00:00",
  "data": {
    "progressRate": 10,
    "notes": "입금 확인 완료"
  }
}
```

#### 메시지 타입
- `ORDER_STATUS_CHANGED`: 주문 상태 변경
- `PAYMENT_CONFIRMED`: 결제 확인
- `ORDER_APPROVED`: 주문 승인
- `ORDER_STARTED`: 작업 시작
- `ORDER_COMPLETED`: 작업 완료
- `ORDER_FAILED`: 작업 실패
- `FILES_UPLOADED`: 결과물 업로드

#### JavaScript 예제
```javascript
const ws = new WebSocket('ws://localhost:8080/api/orders/notifications/ws?memberId=15&token=your-auth-token');

ws.onmessage = function(event) {
    const notification = JSON.parse(event.data);
    console.log('알림 수신:', notification);
    
    switch(notification.type) {
        case 'ORDER_STATUS_CHANGED':
            showStatusChangeNotification(notification);
            break;
        case 'PAYMENT_CONFIRMED':
            showPaymentConfirmedNotification(notification);
            break;
        // ... 기타 알림 타입 처리
    }
};
```

---

### Server-Sent Events (SSE) 연결
WebSocket 대신 SSE를 사용한 실시간 알림입니다.

**Endpoint:** `GET /api/orders/notifications/sse`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |

#### Response Headers
- Content-Type: text/event-stream
- Cache-Control: no-cache
- Connection: keep-alive

#### 이벤트 형식
```
event: orderStatusChanged
data: {"orderId": 123, "orderNumber": "ORD-2025-001", "oldStatus": "PAYMENT_WAITING", "newStatus": "PAYMENT_CONFIRMED", "message": "입금이 확인되었습니다.", "timestamp": "2025-10-09T12:00:00"}

event: paymentConfirmed
data: {"orderId": 123, "orderNumber": "ORD-2025-001", "paymentAmount": 50000.00, "confirmedAt": "2025-10-09T12:00:00"}
```

#### JavaScript 예제
```javascript
const eventSource = new EventSource('/api/orders/notifications/sse?memberId=15');

eventSource.addEventListener('orderStatusChanged', function(event) {
    const data = JSON.parse(event.data);
    showStatusChangeNotification(data);
});

eventSource.addEventListener('paymentConfirmed', function(event) {
    const data = JSON.parse(event.data);
    showPaymentConfirmedNotification(data);
});
```

---

### 알림 히스토리 조회
회원의 알림 내역을 조회합니다.

**Endpoint:** `GET /api/orders/notifications/history`

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| memberId | Long | Y | 회원 ID |
| page | Integer | N | 페이지 번호 (기본값: 0) |
| size | Integer | N | 페이지 크기 (기본값: 20) |
| type | String | N | 알림 타입 필터 |

#### Response
**Success (200 OK)**
```json
{
  "content": [
    {
      "id": 1001,
      "type": "ORDER_STATUS_CHANGED",
      "orderId": 123,
      "orderNumber": "ORD-2025-001",
      "title": "주문 상태 변경",
      "message": "입금이 확인되었습니다.",
      "isRead": true,
      "createdAt": "2025-10-09T12:00:00"
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/orders/notifications/history?memberId=15&page=0&size=20"
```

---

### 알림 읽음 처리
특정 알림을 읽음으로 표시합니다.

**Endpoint:** `PUT /api/orders/notifications/{notificationId}/read`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| notificationId | Long | Y | 알림 ID |

#### Response
**Success (200 OK)**
```json
{
  "id": 1001,
  "type": "ORDER_STATUS_CHANGED",
  "orderId": 123,
  "orderNumber": "ORD-2025-001",
  "title": "주문 상태 변경",
  "message": "입금이 확인되었습니다.",
  "isRead": true,
  "readAt": "2025-10-09T16:00:00",
  "createdAt": "2025-10-09T12:00:00"
}
```

#### cURL 예제
```bash
curl -X PUT "http://localhost:8080/api/orders/notifications/1001/read"
```

---

## 데이터 모델

### OrderStatus (주문 상태)
- `PENDING`: 대기 - 주문 생성 후 초기 상태
- `PAYMENT_WAITING`: 입금 대기 - 결제 정보 생성 후 입금 대기
- `PAYMENT_CONFIRMED`: 입금 확인 - 관리자가 입금 확인
- `APPROVED`: 승인 - 관리자가 주문 승인
- `IN_PROGRESS`: 진행 중 - 광고 게시 작업 진행
- `COMPLETED`: 완료 - 모든 작업 완료
- `FAILED`: 실패 - 작업 실패
- `CANCELLED`: 취소 - 주문 취소

### PaymentStatus (결제 상태)
- `WAITING`: 입금 대기 - 입금 정보 생성 후 대기
- `CONFIRMED`: 입금 확인 - 관리자가 입금 확인
- `FAILED`: 입금 실패 - 입금 처리 실패

### 비즈니스 플로우

#### 주문 생성 플로우
1. **주문 생성** → `PENDING` 상태로 주문 생성
2. **결제 정보 생성** → `PAYMENT_WAITING` 상태로 변경
3. **입금 확인** → `PAYMENT_CONFIRMED` 상태로 변경
4. **관리자 승인** → `APPROVED` 상태로 변경
5. **작업 시작** → `IN_PROGRESS` 상태로 변경
6. **작업 완료** → `COMPLETED` 상태로 변경

#### 이력 관리
- 모든 상태 변경은 `updated_at` 필드로 추적
- 결제 확인 시점은 `paymentConfirmedAt` 필드로 기록
- 관리자 검토 시점은 `reviewedAt` 필드로 기록
- 작업 시작/완료/실패 일자는 각각 별도 필드로 관리

#### 제약사항
- 한 라운드에서 한 회원당 하나의 주문만 가능
- 주문번호와 신청번호는 자동 생성되며 중복 불가
- 결제 상태가 `CONFIRMED`가 되어야 주문 승인 가능
- 상태 변경은 순차적으로 진행되어야 함

#### 권한 관리
- 주문 생성: 일반 회원
- 상태 업데이트: 관리자만 가능
- 결제 상태 확인: 관리자만 가능
- 입금 대기 목록 조회: 관리자만 가능

#### 모니터링 포인트
- 상태별 주문 수량 및 비율
- 결제 대기 시간 및 확인 처리 시간
- 라운드별 참여율 및 완료율
- 실패 원인 분석 및 개선점 도출

---

## 광고 구매 API (클라이언트용)

### 광고 구매 (원클릭 주문+결제)
로그인한 유저가 특정 광고를 구매하는 통합 API입니다. 주문 생성과 결제 정보 생성을 한 번에 처리합니다.

**Endpoint:** `POST /api/orders/purchase`

#### Request Body
```json
{
  "adTaskId": 47,
  "memberId": 16,
  "quantity": 1,
  "requirements": "특별한 요구사항이 있습니다."
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|------|------|
| adTaskId | Long | Y | 광고 작업 ID (ad_tasks 테이블의 ID) |
| memberId | Long | Y | 회원 ID |
| quantity | Integer | N | 수량 (기본값: 1) |
| requirements | String | N | 특별 요구사항 |

#### 자동 처리되는 필드들
- **productName**: `라운드명 + 광고타입 + 광고번호`로 자동 생성
- **deadline**: Round의 `post_end_date` 값으로 자동 설정
- **depositorName**: Member의 회사명 또는 이메일로 자동 설정
- **paymentAmount**: Round의 `order_amount` 값으로 자동 설정
- **bankAccountNumber**: `110-123-456789` (고정값)
- **bankName**: `신한은행` (고정값)

#### Response
**Success (200 OK)**
```json
{
  "order": {
    "id": 123,
    "orderNumber": "ORD-2025-001",
    "adTaskId": 47,
    "adTaskTitle": "AdTask #47",
    "memberId": 16,
    "memberCompanyName": "테스트 회사",
    "memberEmail": "test@company.com",
    "productName": "Q4 마케팅 캠페인 배너광고 1번",
    "quantity": 1,
    "requirements": "특별한 요구사항이 있습니다.",
    "deadline": "2025-11-15",
    "startDate": null,
    "completionDate": null,
    "failureDate": null,
    "progressRate": 0,
    "failureReason": null,
    "status": "PAYMENT_WAITING",
    "submittedAt": "2025-10-22T10:30:00",
    "reviewedAt": null,
    "reviewedByName": null,
    "notes": null,
    "createdAt": "2025-10-22T10:30:00",
    "updatedAt": "2025-10-22T10:30:00",
    "paymentInfo": {
      "id": 456,
      "applicationNumber": "APP251022001",
      "paymentAmount": 50000.00,
      "depositorName": "테스트 회사",
      "paymentStatus": "WAITING",
      "bankAccountNumber": "110-123-456789",
      "bankName": "신한은행",
      "paymentConfirmedAt": null,
      "notes": "로그인한 유저가 광고 구매 - 입금 대기"
    },
    "roundParticipants": null
  },
  "payment": {
    "id": 456,
    "applicationNumber": "APP251022001",
    "paymentAmount": 50000.00,
    "depositorName": "테스트 회사",
    "paymentStatus": "WAITING",
    "bankAccountNumber": "110-123-456789",
    "bankName": "신한은행",
    "paymentConfirmedAt": null,
    "notes": "로그인한 유저가 광고 구매 - 입금 대기"
  }
}
```

#### Error Responses
**Bad Request (400)**
```json
{
  "error": "MEMBER_NOT_FOUND",
  "message": "Member not found with id: 999"
}
```

```json
{
  "error": "ADTASK_NOT_FOUND", 
  "message": "AdTask not found with id: 999"
}
```

#### cURL 예제
```bash
curl -X POST "http://localhost:8080/api/orders/purchase" \
  -H "Content-Type: application/json" \
  -d '{
    "adTaskId": 41,
    "memberId": 3,
    "quantity": 1,
    "requirements": "특별한 요구사항이 있습니다."
  }'
```

#### 비즈니스 로직
1. **주문 생성**: AdTask와 Member 정보를 바탕으로 주문 생성
2. **상품명 자동 생성**: `"{라운드명} {광고타입} {광고번호}번"` 형식
3. **마감일 자동 설정**: Round의 게시 종료일(`post_end_date`) 사용
4. **결제 정보 생성**: Round의 주문 금액으로 결제 정보 자동 생성
5. **입금자명 자동 설정**: Member의 회사명 또는 이메일 사용
6. **상태 변경**: PENDING → PAYMENT_WAITING 자동 처리

#### 사용 시나리오
1. **클라이언트 앱**: 사용자가 광고를 선택하고 "구매하기" 버튼 클릭
2. **원클릭 주문**: adTaskId와 memberId만으로 즉시 주문+결제 정보 생성
3. **입금 안내**: 응답의 결제 정보를 사용하여 사용자에게 입금 안내
4. **상태 추적**: 이후 주문 상태 API를 통해 결제 확인 및 진행 상황 추적

#### 장점
- **간편함**: 복잡한 정보 입력 없이 최소한의 파라미터로 주문 가능
- **자동화**: 상품명, 마감일, 금액 등 자동 계산으로 오류 방지
- **일관성**: Round 정보 기반으로 일관된 주문 정보 생성
- **효율성**: 주문과 결제 정보를 한 번의 API 호출로 처리