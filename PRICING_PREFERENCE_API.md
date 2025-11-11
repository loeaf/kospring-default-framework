# 게시비 단가 선택 기능 API 문서

## 개요
회원가입 및 임대권 구매 시 게시비 단가 기준을 선택할 수 있는 기능입니다.

## 게시비 단가 선택 옵션

| 값 | 이름 | 설명 | 이모지 |
|---|------|------|--------|
| `highest` | 최고 금액 기준 | 시장에서 가장 높은 단가를 기준으로 게시비를 책정 | 💹 HIGH |
| `lowest` | 최저 금액 기준 | 합리적이고 경쟁력 있는 최저 단가를 기준으로 게시비를 책정 | 💰 ECO |
| `undecided` | 모르겠음 (다수결 따름) | 다른 게시자들의 선택에 따라 자동으로 결정 | ⚖️ AUTO |

## 게시비 단가 결정 방식
- 모든 게시자들의 선택을 취합하여 **다수결 원칙**에 따라 최종 단가 기준을 결정
- `undecided` 선택 시 자동으로 다수 의견에 따라 설정
- 향후 게시비 정산 시 해당 기준에 따라 적용

## API 변경 사항

### 1. 회원가입 API (`/api/members/complete-registration`)

#### 추가된 파라미터
- `pricingPreference`: String (선택) - 게시비 단가 선택 (highest/lowest/undecided)
- `marketingAgreed`: Boolean (선택) - 마케팅 정보 수신 동의

#### 예시
```bash
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "email=user@example.com" \
  -F "password=password123" \
  -F "companyName=테스트 회사" \
  -F "businessRegistrationNumber=123-45-67890" \
  -F "contactNumber=010-1234-5678" \
  -F "businessRegistrationFile=@./business_registration.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=false" \
  -F "pricingPreference=highest" \
  -F "durationYears=1"
```

### 2. 임대권 구매 API (`/api/rental-rights/purchase`)

#### 추가된 파라미터
- `pricingPreference`: String (선택) - 게시비 단가 선택 (highest/lowest/undecided)

#### 예시
```bash
curl -X POST http://localhost:8080/api/rental-rights/purchase \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "durationYears": 1,
    "pricingPreference": "highest"
  }'
```

## 데이터베이스 스키마 변경

### rental_rights 테이블
```sql
ALTER TABLE rental_rights 
ADD COLUMN pricing_preference VARCHAR(20) NULL 
COMMENT '게시비 단가 선택 기준: highest(최고금액), lowest(최저금액), undecided(모르겠음)';
```

## 프론트엔드 UI

### 게시비 단가 선택 섹션
```html
<div class="bg-gray-50 p-5 rounded-xl mb-6 border border-gray-200">
    <h3 class="font-semibold text-gray-800 mb-4 flex items-center">
        <span class="w-6 h-6 bg-orange-500 text-white rounded-full flex items-center justify-center text-sm mr-2">💰</span>
        게시비 단가 희망 기준 선택
    </h3>
    
    <div class="space-y-3">
        <!-- 최고 금액 기준 -->
        <div class="flex items-center space-x-3 p-4 border-2 rounded-xl cursor-pointer">
            <input type="radio" name="pricingPreference" value="highest">
            <div class="flex-1">
                <label class="font-medium text-gray-800">최고 금액 기준</label>
                <p class="text-sm text-gray-600 mt-1">시장에서 가장 높은 단가를 기준으로 게시비를 책정합니다</p>
            </div>
            <div class="text-green-600 font-semibold">💹 HIGH</div>
        </div>
        
        <!-- 최저 금액 기준 -->
        <div class="flex items-center space-x-3 p-4 border-2 rounded-xl cursor-pointer">
            <input type="radio" name="pricingPreference" value="lowest">
            <div class="flex-1">
                <label class="font-medium text-gray-800">최저 금액 기준</label>
                <p class="text-sm text-gray-600 mt-1">합리적이고 경쟁력 있는 최저 단가를 기준으로 게시비를 책정합니다</p>
            </div>
            <div class="text-blue-600 font-semibold">💰 ECO</div>
        </div>
        
        <!-- 모르겠음 (다수결 따름) -->
        <div class="flex items-center space-x-3 p-4 border-2 rounded-xl cursor-pointer">
            <input type="radio" name="pricingPreference" value="undecided">
            <div class="flex-1">
                <label class="font-medium text-gray-800">모르겠음 (다수결 따름)</label>
                <p class="text-sm text-gray-600 mt-1">다른 게시자들의 선택에 따라 자동으로 결정됩니다</p>
            </div>
            <div class="text-gray-600 font-semibold">⚖️ AUTO</div>
        </div>
    </div>
</div>
```

## 테스트 스크립트 예시

### 다양한 게시비 단가 선택으로 테스트 멤버 생성
```bash
# 멤버 1: 최고 금액 기준
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "pricingPreference=highest" \
  # ... 기타 파라미터

# 멤버 2: 최저 금액 기준  
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "pricingPreference=lowest" \
  # ... 기타 파라미터

# 멤버 3: 모르겠음
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "pricingPreference=undecided" \
  # ... 기타 파라미터
```

## 비즈니스 로직

1. **회원가입 시**: 선택한 게시비 단가 기준이 임대권에 저장됨
2. **임대권 갱신 시**: 새로운 게시비 단가 기준 선택 가능
3. **게시비 정산 시**: 저장된 기준에 따라 다수결 원칙으로 최종 단가 결정
4. **UI 표시**: 계약 체결 시 필수 선택 항목으로 처리

## 주의사항

- `pricingPreference`는 선택사항이지만, UI에서는 필수 선택으로 처리
- 빈 값("")으로 저장되면 시스템에서 `undecided`로 간주
- 기존 데이터는 `NULL` 값을 가지며, 필요시 `undecided`로 업데이트 가능