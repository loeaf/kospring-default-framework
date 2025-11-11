# 라운드 업데이트 API 문서

## 개요
기존 라운드에서 광고를 제작하지 않은 신규 회원들을 위한 광고를 자동으로 생성하는 API입니다.

## API 엔드포인트

### 신규 회원 광고 생성
기존 라운드에서 광고를 만들지 않은 신규 회원들을 위한 광고를 생성합니다.

**Endpoint:** `POST /api/ad-tasks/rounds/{roundId}/create-missing-ads`

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| roundId | Long | Y | 라운드 ID |

#### Response
**Success (200 OK)**
```json
{
  "message": "Missing ads created successfully",
  "roundId": 1,
  "roundTitle": "도현이 부자만들기 찬성 라운드 #2",
  "newMembersCount": 3,
  "totalAdTasksCreated": 9,
  "memberTasks": [
    {
      "memberId": 7,
      "memberEmail": "user7@example.com",
      "companyName": "신규회사1",
      "adTaskIds": [25, 26, 27],
      "adTypes": ["scratch_1", "carousel_2", "interactive_3"]
    },
    {
      "memberId": 8,
      "memberEmail": "user8@example.com", 
      "companyName": "신규회사2",
      "adTaskIds": [28, 29, 30],
      "adTypes": ["scratch_1", "carousel_2", "interactive_3"]
    }
  ]
}
```

**Error (404 Not Found)**
```json
{
  "error": "Round not found"
}
```

**Error (500 Internal Server Error)**
```json
{
  "error": "Failed to create missing ads: [error message]"
}
```

#### cURL 예제
```bash
curl -X POST http://localhost:8080/api/ad-tasks/rounds/1/create-missing-ads \
  -H "Content-Type: application/json"
```

## 스크립트 사용법

### 1. 신규 회원 광고 생성 스크립트
```bash
# 기본 사용 (라운드 ID 1)
./sh/update_round_ads.sh

# 특정 라운드 지정
./sh/update_round_ads.sh 2
```

### 2. 라운드 현황 확인 스크립트
```bash
# 기본 사용 (라운드 ID 1) 
./sh/check_round_members.sh

# 특정 라운드 확인
./sh/check_round_members.sh 2
```

## 동작 과정

### 1. 신규 회원 식별
- 해당 라운드에서 이미 광고를 만든 회원들 조회
- 유효한 임대권을 가진 모든 활성 회원 조회
- 광고를 만들지 않은 신규 회원들 필터링

### 2. 광고 생성
각 신규 회원에 대해 3개의 광고 타입별로 생성:
- `scratch_1`: 스크래치 광고 (인덱스 1)
- `carousel_2`: 캐러셀 광고 (인덱스 2) 
- `interactive_3`: 인터랙티브 광고 (인덱스 3)

### 3. 상태 관리
- 새로 생성되는 광고는 `PENDING` 상태로 시작
- 자동으로 처리 큐에 추가되어 Python 워커가 처리

## 사용 시나리오

### 시나리오 1: 라운드 생성 후 신규 회원 가입
1. 라운드 생성 및 기존 회원들 광고 생성 완료
2. 새로운 회원이 가입하여 임대권 구매
3. `update_round_ads.sh` 실행으로 신규 회원 광고 자동 생성

### 시나리오 2: 정기적 신규 회원 확인
1. 크론잡으로 주기적으로 `update_round_ads.sh` 실행
2. 신규 회원이 있으면 자동으로 광고 생성
3. 없으면 "신규 회원이 없습니다" 메시지

## 로그 예시

### 성공적인 광고 생성
```
=== 라운드 1 신규 회원 광고 생성 ===
신규 회원 수: 2
생성된 광고 수: 6
신규 회원 2명에 대해 6개의 광고가 생성되었고,
PENDING 상태의 광고들이 처리 큐에 추가되었습니다.
```

### 신규 회원이 없는 경우
```
=== 라운드 1 신규 회원 광고 생성 ===
신규 회원 수: 0
생성된 광고 수: 0
신규 회원이 없어서 추가로 생성할 광고가 없습니다.
모든 활성 회원들이 이미 이 라운드의 광고를 가지고 있습니다.
```

## 관련 API

### 기존 API들과의 연계
- `GET /api/members/active` - 활성 회원 조회
- `GET /api/ad-tasks/rounds/{roundId}` - 라운드별 광고 조회
- `GET /api/ad-tasks/rounds/{roundId}/statistics` - 라운드 광고 통계
- `POST /api/ad-tasks/rounds/{roundId}/enqueue-pending` - PENDING 광고 큐 추가

### 워크플로우
1. **라운드 생성** → `POST /api/rounds`
2. **기존 회원 광고 생성** → `POST /api/ad-tasks/test-data`
3. **신규 회원 가입** → `POST /api/members/complete-registration`
4. **신규 회원 광고 생성** → `POST /api/ad-tasks/rounds/{roundId}/create-missing-ads`
5. **광고 처리 시작** → `POST /api/ad-tasks/rounds/{roundId}/enqueue-pending`

## 주의사항

- 이미 광고를 가진 회원에 대해서는 추가 광고가 생성되지 않음
- 임대권이 만료된 회원은 제외됨
- 생성된 광고는 Python 워커에 의해 비동기적으로 처리됨
- 라운드가 종료된 경우에도 신규 회원 광고 생성 가능 (관리자 판단)