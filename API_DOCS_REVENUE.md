# 수익관리 API 문서

## 개요
수익관리 API는 사용자의 광고 수익, 주문 수익, 정산 내역, 세금계산서 등을 관리하는 기능을 제공합니다.

## Base URL
```
http://localhost:8080/api/revenue
```

---

## 수익 관리 API

### 1. 수익 목록 조회
사용자의 수익 목록을 조회합니다. 타입과 기간별 필터링을 지원합니다.

```http
GET /api/revenue/list
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |
| transactionType | String | No | 거래 타입 (INCOME, EXPENSE) |
| period | String | No | 조회 기간 (WEEK, MONTH, QUARTER, YEAR) |

#### Response
```json
{
  "items": [
    {
      "id": 123,
      "transactionType": "INCOME",
      "title": "헬스케어 광고 수익",
      "description": "Round #246 · 헬스케어 분야",
      "transactionDate": "2024-08-26",
      "amount": 125000,
      "memberCompanyName": "테스트 회사",
      "roundNumber": "246",
      "roundCategory": "헬스케어",
      "ctrRate": 5.2,
      "taxInvoiceIssued": false,
      "taxInvoiceNumber": null,
      "createdAt": "2024-08-26T10:30:00",
      "updatedAt": "2024-08-26T10:30:00"
    }
  ],
  "totalCount": 10,
  "summary": {
    "totalIncome": 2847500,
    "totalExpense": 400000,
    "netRevenue": 2447500,
    "monthlyGrowth": 12.5,
    "incomeGrowth": 8.3,
    "expenseGrowth": 25.0,
    "period": "MONTH"
  }
}
```

#### cURL 예제
```bash
# 전체 수익 목록 조회
curl -X GET "http://localhost:8080/api/revenue/list?memberId=2&period=MONTH"

# 매출만 조회
curl -X GET "http://localhost:8080/api/revenue/list?memberId=2&transactionType=INCOME&period=MONTH"

# 매입만 조회
curl -X GET "http://localhost:8080/api/revenue/list?memberId=2&transactionType=EXPENSE&period=MONTH"
```

### 2. 수익 요약 정보 조회
사용자의 수익 요약 정보만 조회합니다.

```http
GET /api/revenue/summary
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |
| period | String | No | 조회 기간 (WEEK, MONTH, QUARTER, YEAR) |

#### Response
```json
{
  "totalIncome": 2847500,
  "totalExpense": 400000,
  "netRevenue": 2447500,
  "monthlyGrowth": 12.5,
  "incomeGrowth": 8.3,
  "expenseGrowth": 25.0,
  "period": "MONTH"
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/revenue/summary?memberId=2&period=MONTH"
```

### 3. 수익 데이터 내보내기
수익 데이터를 Excel 파일로 내보냅니다.

```http
GET /api/revenue/export
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |
| transactionType | String | No | 거래 타입 (INCOME, EXPENSE) |
| period | String | No | 조회 기간 (WEEK, MONTH, QUARTER, YEAR) |

#### Response
Excel 파일 다운로드

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/revenue/export?memberId=2&transactionType=INCOME&period=MONTH" \
  --output "revenue-data.xlsx"
```

---

## 정산 관리 API

### 1. 정산 정보 조회
다음 정산 예정일과 예상 금액 등의 정산 정보를 조회합니다.

```http
GET /api/revenue/settlement/info
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "nextSettlementDate": "2024-09-01T00:00:00",
  "expectedAmount": 3120100,
  "totalSettled": 24567800,
  "accountInfo": {
    "bankName": "신한은행",
    "accountNumber": "110-123-****47",
    "accountHolder": "김도현"
  }
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/revenue/settlement/info?memberId=2"
```

### 2. 정산 내역 조회
지난 정산 내역을 조회합니다.

```http
GET /api/revenue/settlement/history
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "settlements": [
    {
      "id": 1,
      "period": "2024년 7월",
      "amount": 2847500,
      "settlementDate": "2024-08-01T00:00:00",
      "status": "COMPLETED"
    }
  ],
  "totalCount": 3
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/revenue/settlement/history?memberId=2"
```

### 3. 계좌 정보 수정
정산받을 계좌 정보를 수정합니다.

```http
PUT /api/revenue/account
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Request Body
```json
{
  "bankName": "신한은행",
  "accountNumber": "110-123-456789",
  "accountHolder": "김도현"
}
```

#### Response
```json
{
  "success": true,
  "message": "계좌 정보가 업데이트되었습니다. 1원 인증을 진행해주세요."
}
```

#### cURL 예제
```bash
curl -X PUT "http://localhost:8080/api/revenue/account?memberId=2" \
  -H "Content-Type: application/json" \
  -d '{
    "bankName": "신한은행",
    "accountNumber": "110-123-456789",
    "accountHolder": "김도현"
  }'
```

---

## 세금계산서 API

### 1. 세금계산서 생성
수익 항목에 대한 세금계산서를 생성합니다.

```http
POST /api/revenue/tax-invoice
```

#### Request Body
```json
{
  "revenueItemId": "ad_123",
  "type": "SALES",
  "supplierInfo": {
    "businessNo": "234-56-78901",
    "company": "IT 개발회사",
    "ceo": "대표자명",
    "address": "서울시 강남구",
    "businessType": "서비스업",
    "businessItem": "소프트웨어 개발"
  },
  "buyerInfo": {
    "businessNo": "117-81-49125",
    "company": "CNC 네트워크",
    "ceo": "대표자명",
    "address": "서울시 서초구",
    "businessType": "서비스업",
    "businessItem": "플랫폼 운영"
  }
}
```

#### Response
```json
{
  "invoiceNumber": "202510218846",
  "approvalNo": "2025102176112236",
  "type": "SALES",
  "issueDate": "2025-10-21T17:21:05.876166000",
  "supplier": {
    "businessNo": "234-56-78901",
    "company": "IT 개발회사",
    "ceo": "대표자명",
    "address": "서울시 강남구",
    "businessType": "서비스업",
    "businessItem": "소프트웨어 개발"
  },
  "buyer": {
    "businessNo": "117-81-49125",
    "company": "CNC 네트워크",
    "ceo": "대표자명",
    "address": "서울시 서초구",
    "businessType": "서비스업",
    "businessItem": "플랫폼 운영"
  },
  "item": {
    "name": "기타 수익",
    "specification": "",
    "quantity": 1,
    "unitPrice": 100000,
    "amount": 100000
  },
  "amounts": {
    "supply": 100000,
    "tax": 10000,
    "total": 110000
  },
  "remarks": "기타 수익",
  "paymentMethod": {
    "cash": 110000,
    "check": 0,
    "promissoryNote": 0,
    "credit": 0
  }
}
```

#### cURL 예제
```bash
curl -X POST "http://localhost:8080/api/revenue/tax-invoice" \
  -H "Content-Type: application/json" \
  -d '{
    "revenueItemId": "ad_123",
    "type": "SALES",
    "supplierInfo": {
      "businessNo": "234-56-78901",
      "company": "IT 개발회사",
      "ceo": "대표자명",
      "address": "서울시 강남구",
      "businessType": "서비스업",
      "businessItem": "소프트웨어 개발"
    },
    "buyerInfo": {
      "businessNo": "117-81-49125",
      "company": "CNC 네트워크",
      "ceo": "대표자명",
      "address": "서울시 서초구",
      "businessType": "서비스업",
      "businessItem": "플랫폼 운영"
    }
  }'
```

### 2. Revenue Transaction 기반 세금계산서 생성
수익 거래(revenue_transactions) 데이터를 기반으로 자동으로 세금계산서를 생성합니다.
거래의 member, order, assignment 정보를 기반으로 공급자/구매자 정보와 항목을 자동으로 채웁니다.

```http
GET /api/revenue/tax-invoice/from-transaction/{revenueTransactionId}
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| revenueTransactionId | Long | Yes | 수익 거래 ID |

#### 자동 처리 로직

**매입 거래(EXPENSE)의 경우:**
- 공급자: CNC 네트워크 (플랫폼 운영사)
- 구매자: 해당 revenue_transaction의 member 정보 
- 항목: "{order.productName} 광고 게시 서비스"
- 세금계산서 유형: PURCHASES (매입)

**매출 거래(INCOME)의 경우:**
- 공급자: 해당 revenue_transaction의 member 정보
- 구매자: CNC 네트워크 (플랫폼 운영사)
- 항목: "광고 콘텐츠 제작 및 게시"
- 세금계산서 유형: SALES (매출)

**공통 처리:**
- 금액: revenue_transaction의 amount를 공급가액으로 사용
- 부가세: 공급가액의 10% 자동 계산
- 회원 정보: member 테이블의 사업자등록번호, 회사명, 주소 등 활용

#### Response
Revenue Transaction의 실제 데이터를 기반으로 세금계산서가 생성됩니다.
```json
{
  "invoiceNumber": "202510218847",
  "approvalNo": "2025102176112237",
  "type": "SALES",
  "issueDate": "2025-10-21T17:21:05.876166000",
  "supplier": {
    "businessNo": "234-56-78901",
    "company": "IT 개발회사",
    "ceo": "대표자명",
    "address": "서울시 강남구",
    "businessType": "서비스업",
    "businessItem": "소프트웨어 개발"
  },
  "buyer": {
    "businessNo": "117-81-49125",
    "company": "CNC 네트워크",
    "ceo": "대표자명",
    "address": "서울시 서초구",
    "businessType": "서비스업",
    "businessItem": "플랫폼 운영"
  },
  "item": {
    "name": "광고 콘텐츠 제작 및 게시",
    "specification": "Round #1 - 헬스케어 (SOCIAL_MEDIA)",
    "quantity": 1,
    "unitPrice": 125000,
    "amount": 125000
  },
  "amounts": {
    "supply": 125000,
    "tax": 12500,
    "total": 137500
  },
  "remarks": "광고 콘텐츠 제작 및 게시 - INCOME (Round #1)",
  "paymentMethod": {
    "cash": 137500,
    "check": 0,
    "promissoryNote": 0,
    "credit": 0
  }
}
```

#### cURL 예제
```bash
# 수익 거래 ID 123번을 기반으로 세금계산서 생성
curl -X GET "http://localhost:8080/api/revenue/tax-invoice/from-transaction/123"

# 실제 존재하는 revenue transaction ID 사용 예시
curl -X GET "http://localhost:8080/api/revenue/tax-invoice/from-transaction/1"
```

### 3. 세금계산서 목록 조회
사용자의 세금계산서 목록을 조회합니다.

```http
GET /api/revenue/tax-invoice/list
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "invoices": [
    {
      "invoiceNumber": "202408230001",
      "type": "SALES",
      "issueDate": "2024-08-23T10:30:00",
      "supplierCompany": "김도현",
      "buyerCompany": "CNC 네트워크",
      "totalAmount": 3520000,
      "status": "ISSUED"
    }
  ],
  "totalCount": 2
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/revenue/tax-invoice/list?memberId=2"
```

### 4. 세금계산서 상세 조회
특정 세금계산서의 상세 정보를 조회합니다.

```http
GET /api/revenue/tax-invoice/{invoiceNumber}
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| invoiceNumber | String | Yes | 세금계산서 번호 |

#### Response
세금계산서 생성 API와 동일한 형식

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/revenue/tax-invoice/202408230001"
```

### 5. 세금계산서 이메일 발송
세금계산서를 이메일로 발송합니다.

```http
POST /api/revenue/tax-invoice/{invoiceNumber}/email
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| invoiceNumber | String | Yes | 세금계산서 번호 |
| email | String | Yes | 받을 이메일 주소 |

#### Response
```json
{
  "success": true,
  "message": "세금계산서가 이메일로 발송되었습니다."
}
```

#### cURL 예제
```bash
curl -X POST "http://localhost:8080/api/revenue/tax-invoice/202408230001/email?email=test@example.com"
```

### 6. 세금계산서 PDF 다운로드
세금계산서를 PDF 파일로 다운로드합니다.

```http
GET /api/revenue/tax-invoice/{invoiceNumber}/pdf
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| invoiceNumber | String | Yes | 세금계산서 번호 |

#### Response
PDF 파일 다운로드

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/revenue/tax-invoice/202408230001/pdf" \
  -H "Accept: application/pdf" \
  --output "tax-invoice-202408230001.pdf"
```

---

## 테스트 API

### 1. 테스트 수익 데이터 생성
테스트용 수익 데이터를 생성합니다.

```http
POST /api/revenue/test/generate
```

#### Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |

#### Response
```json
{
  "success": true,
  "message": "테스트 수익 데이터가 생성되었습니다.",
  "generatedCount": 10
}
```

#### cURL 예제
```bash
curl -X POST "http://localhost:8080/api/revenue/test/generate?memberId=2"
```

---

## 데이터 타입 설명

### TransactionType
- `INCOME`: 수입 (광고 게시로 받은 수익)
- `EXPENSE`: 지출 (광고 주문 비용)

### RevenuePeriod
- `WEEK`: 주간
- `MONTH`: 월간
- `QUARTER`: 분기
- `YEAR`: 연간

### SettlementStatus
- `PENDING`: 대기
- `COMPLETED`: 완료
- `FAILED`: 실패

### TaxInvoiceType
- `SALES`: 매출 세금계산서
- `PURCHASES`: 매입 세금계산서

### TaxInvoiceStatus
- `DRAFT`: 임시저장
- `ISSUED`: 발행완료
- `SENT`: 전송완료
- `FAILED`: 발행실패

---

## 에러 코드

| HTTP Status | Description |
|-------------|-------------|
| 200 | 성공 |
| 400 | 잘못된 요청 |
| 401 | 인증 실패 |
| 403 | 권한 없음 |
| 404 | 리소스를 찾을 수 없음 |
| 500 | 서버 내부 오류 |


## 사용 예시

### JavaScript (Axios)
```javascript
// 수익 목록 조회
const revenueList = await axios.get('/api/revenue/list', {
  params: {
    memberId: 2,
    transactionType: 'INCOME',
    period: 'MONTH'
  }
});

// 세금계산서 생성
const taxInvoice = await axios.post('/api/revenue/tax-invoice', {
  revenueItemId: 'ad_123',
  type: 'SALES',
  supplierInfo: {
    businessNo: '234-56-78901',
    company: 'IT 개발회사',
    ceo: '대표자명'
  },
  buyerInfo: {
    businessNo: '117-81-49125',
    company: 'CNC 네트워크',
    ceo: '대표자명'
  }
});

// 정산 내역 조회
const settlementHistory = await axios.get('/api/revenue/settlement/history', {
  params: { memberId: 2 }
});
```