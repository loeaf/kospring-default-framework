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
  "isAvailable": true,
  "message": "사용 가능한 이메일입니다."
}
```

**Duplicate Email (200 OK)**
```json
{
  "isAvailable": false,
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
  "isAvailable": true,
  "message": "사용 가능한 사업자등록번호입니다."
}
```

**Duplicate Business Number (200 OK)**
```json
{
  "isAvailable": false,
  "message": "이미 등록된 사업자등록번호입니다."
}
```

#### cURL 예제
```bash
curl -X GET "http://localhost:8080/api/members/check-business-number?businessRegistrationNumber=123-45-67890"
```