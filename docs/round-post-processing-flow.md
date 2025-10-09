# POST Round 처리 순서

## 개요
라운드 생성(POST /rounds) 엔드포인트의 전체 처리 흐름을 설명합니다.

## 처리 순서

### 1. 파라미터 검증 (RoundCreateRequest)
- **title**: 라운드 제목 (필수, 최대 255자)
- **description**: 라운드 설명 (선택, 최대 1000자)
- **category**: 카테고리 (선택, 최대 100자)
- **orderAmount**: 발주 금액 (필수, 0.01 이상)
- **templateCost**: 템플릿 비용 (선택, 0 이상)
- **aiGenerationCost**: AI 생성비 (선택, 0 이상)
- **targetingPostingCost**: 타게팅 게시비 (선택, 0 이상)
- **serverRentalCost**: 서버 임대비 (선택, 0 이상)
- **otherCosts**: 기타 비용 (선택, 0 이상)
- **startDate**: 시작일 (필수, 현재 시간 이후)
- **endDate**: 종료일 (필수, 시작일 이후)
- **maxParticipants**: 최대 참여자 수 (선택, 1 이상)

### 2. 회원 검증
- `MemberRepository.findByIdOrNull(createdById)`를 통해 생성자 회원 조회
- 회원이 존재하지 않으면 `IllegalArgumentException` 발생

### 3. 데이터베이스 저장

#### 3.1 Round 엔티티 생성 및 저장
**테이블**: `rounds`

**저장되는 필드**:
- title, description, category
- orderAmount, templateCost, aiGenerationCost, targetingPostingCost, serverRentalCost, otherCosts
- startDate, endDate, maxParticipants
- status: ACTIVE (기본값)
- createdBy: 회원 엔티티 참조
- createdAt, updatedAt: 자동 생성

#### 3.2 라운드 번호 생성
- `generate_round_number()` DB 함수 호출하여 고유 라운드 번호 생성
- `Round.roundNumber` 필드에 할당

### 4. 광고 생성 작업 큐 처리

#### 4.1 활성 회원 조회
- `MemberRepository.findActiveMembers()`로 모든 활성 회원 조회

#### 4.2 AdTask 엔티티 생성 및 저장
**테이블**: `ad_tasks`

각 활성 회원에 대해 AdTask 생성:
- round: 생성된 Round 엔티티 참조
- member: 각 활성 회원 엔티티 참조
- status: PENDING (기본값)
- retryCount: 0 (기본값)
- createdAt: 자동 생성

### 5. Redis 큐 처리

#### 5.1 큐 이름
- `ad_generation_queue`: 메인 광고 생성 작업 큐

#### 5.2 메시지 구조 (AdTaskMessage)
각 AdTask에 대해 다음 구조의 메시지를 Redis 리스트에 추가:

```json
{
  "taskId": "AdTask의 ID",
  "roundId": "라운드 ID",
  "memberId": "회원 ID",
  "roundInfo": {
    "id": "라운드 ID",
    "title": "라운드 제목",
    "description": "라운드 설명",
    "category": "카테고리",
    "orderAmount": "발주 금액",
    "templateCost": "템플릿 비용",
    "aiGenerationCost": "AI 생성비",
    "targetingPostingCost": "타게팅 게시비",
    "serverRentalCost": "서버 임대비",
    "otherCosts": "기타 비용",
    "startDate": "시작일",
    "endDate": "종료일",
    "maxParticipants": "최대 참여자 수"
  },
  "memberInfo": {
    "id": "회원 ID",
    "email": "이메일",
    "companyName": "회사명",
    "businessRegistrationNumber": "사업자등록번호",
    "contactNumber": "연락처"
  }
}
```

#### 5.3 Redis 저장 방식
- `RedisTemplate.opsForList().leftPush(AD_GENERATION_QUEUE, message)`
- Jackson JSON 직렬화를 통해 메시지 저장

### 6. 응답 반환
- 생성된 Round 엔티티를 RoundResponse DTO로 변환하여 반환

## API 호출 예시

### cURL 명령어
```bash
curl -X POST http://localhost:8080/api/rounds \
  -H "Content-Type: application/json" \
  -d '{
    "title": "2024년 Q4 마케팅 캠페인",
    "description": "연말 기업소개를 위한 디지털 마케팅 라운드",
    "category": "MARKETING",
    "orderAmount": 1000000.00,
    "templateCost": 50000.00,
    "aiGenerationCost": 30000.00,
    "targetingPostingCost": 100000.00,
    "serverRentalCost": 20000.00,
    "otherCosts": 10000.00,
    "startDate": "2024-12-01T09:00:00",
    "endDate": "2024-12-31T23:59:59",
    "maxParticipants": 100
  }'
```

### 응답 예시
```json
{
  "id": 1,
  "roundNumber": "R-2024-001",
  "title": "2024년 Q4 마케팅 캠페인",
  "description": "연말 프로모션을 위한 디지털 마케팅 라운드",
  "category": "MARKETING",
  "orderAmount": 1000000.00,
  "templateCost": 50000.00,
  "aiGenerationCost": 30000.00,
  "targetingPostingCost": 100000.00,
  "serverRentalCost": 20000.00,
  "otherCosts": 10000.00,
  "startDate": "2024-12-01T09:00:00",
  "endDate": "2024-12-31T23:59:59",
  "status": "ACTIVE",
  "maxParticipants": 100,
  "createdBy": {
    "id": 1,
    "email": "admin@example.com",
    "companyName": "테스트 회사"
  },
  "createdAt": "2024-10-07T10:30:00",
  "updatedAt": "2024-10-07T10:30:00"
}
```

## 주요 고려사항

### 트랜잭션 처리
- `@Transactional` 어노테이션으로 전체 메서드가 하나의 트랜잭션으로 처리
- 광고 생성 큐 추가 실패 시에도 라운드 생성은 성공하도록 예외 처리

### 오류 처리
- 회원 검증 실패: `IllegalArgumentException`
- 광고 큐 추가 실패: 로그 기록 후 계속 진행 (라운드 생성 실패하지 않음)

### 로깅
- 라운드 생성 완료 및 광고 생성 작업 수량 로깅
- 각 단계별 상세 로깅 (DEBUG 레벨)