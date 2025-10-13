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
| type | String | No | 수익 타입 (SALES, PURCHASES) |
| period | String | No | 조회 기간 (WEEK, MONTH, QUARTER, YEAR) |

#### Response
```json
{
  "items": [
    {
      "id": "ad_123",
      "type": "SALES",
      "title": "헬스케어 광고 수익",
      "description": "Round #246 · 헬스케어 분야",
      "date": "2024-08-26T10:30:00",
      "amount": 125000,
      "status": "완료",
      "fee": 12500,
      "metrics": [
        {
          "label": "예상 수익",
          "value": "₩12,500"
        },
        {
          "label": "수익률",
          "value": "10.0%"
        }
      ],
      "category": "광고"
    }
  ],
  "totalCount": 10,
  "summary": {
    "totalRevenue": 3247500,
    "monthlyGrowth": 12.5,
    "adRevenue": 2847500,
    "adRevenueGrowth": 8.3,
    "orderRevenue": 400000,
    "orderRevenueGrowth": 25.0,
    "period": "MONTH"
  }
}
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
  "totalRevenue": 3247500,
  "monthlyGrowth": 12.5,
  "adRevenue": 2847500,
  "adRevenueGrowth": 8.3,
  "orderRevenue": 400000,
  "orderRevenueGrowth": 25.0,
  "period": "MONTH"
}
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
| type | String | No | 수익 타입 (SALES, PURCHASES) |
| period | String | No | 조회 기간 (WEEK, MONTH, QUARTER, YEAR) |

#### Response
Excel 파일 다운로드

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
  "invoiceNumber": "202408230001",
  "type": "SALES",
  "issueDate": "2024-08-23T10:30:00",
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
    "name": "교육 플랫폼 UI 제작 용역",
    "specification": "",
    "quantity": 1,
    "unitPrice": null,
    "amount": 3200000
  },
  "amounts": {
    "supply": 3200000,
    "tax": 320000,
    "total": 3520000
  },
  "approvalNo": "2024082300000123"
}
```

### 2. 세금계산서 목록 조회
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

### 3. 세금계산서 상세 조회
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

### 4. 세금계산서 이메일 발송
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

### 5. 세금계산서 PDF 다운로드
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

---

## 데이터 타입 설명

### RevenueType
- `SALES`: 매출 (광고 수익)
- `PURCHASES`: 매입 (주문 비용)

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
    memberId: 1,
    type: 'SALES',
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
  params: { memberId: 1 }
});
```

### cURL
```bash
# 수익 목록 조회
curl -X GET "http://localhost:8080/api/revenue/list?memberId=1&type=SALES&period=MONTH"

# 세금계산서 PDF 다운로드
curl -X GET "http://localhost:8080/api/revenue/tax-invoice/202408230001/pdf" \
  -H "Accept: application/pdf" \
  --output "tax-invoice.pdf"

# 계좌 정보 수정
curl -X PUT "http://localhost:8080/api/revenue/account?memberId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "bankName": "신한은행",
    "accountNumber": "110-123-456789",
    "accountHolder": "김도현"
  }'
```