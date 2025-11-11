# 라운드 업데이트 기능

## 개요
기존 라운드에서 광고를 제작하지 않은 신규 회원들을 위한 광고를 자동으로 생성하는 기능입니다.

## 주요 기능

### 1. 신규 회원 광고 자동 생성
- 기존 라운드에 참여하지 않은 신규 가입 회원 식별
- 자동으로 3개 타입 광고(scratch, carousel, interactive) 생성
- PENDING 상태로 생성하여 Python 워커가 처리하도록 큐에 추가

### 2. 중복 방지
- 이미 해당 라운드에 광고가 있는 회원은 제외
- 임대권이 만료된 회원은 자동 제외

## 스크립트 사용법

### 기본 사용
```bash
# 라운드 1의 신규 회원 광고 생성
./sh/update_round_ads.sh 1

# 라운드 2의 신규 회원 광고 생성
./sh/update_round_ads.sh 2
```

### 라운드 현황 확인
```bash
# 라운드 1의 회원 및 광고 현황 확인
./sh/check_round_members.sh 1
```

### 전체 워크플로우
```bash
# 라운드 생성부터 광고 생성까지 전체 과정
./sh/round_workflow.sh
```

## API 사용법

### 신규 회원 광고 생성
```bash
curl -X POST http://localhost:8080/api/ad-tasks/rounds/1/create-missing-ads \
  -H "Content-Type: application/json"
```

### 응답 예시
```json
{
  "message": "Missing ads created successfully",
  "roundId": 1,
  "roundTitle": "도현이 부자만들기 찬성 라운드 #2",
  "newMembersCount": 2,
  "totalAdTasksCreated": 6,
  "memberTasks": [
    {
      "memberId": 7,
      "memberEmail": "new1@example.com",
      "companyName": "신규회사1",
      "adTaskIds": [25, 26, 27],
      "adTypes": ["scratch_1", "carousel_2", "interactive_3"]
    }
  ]
}
```

## 사용 시나리오

### 시나리오 1: 라운드 진행 중 신규 회원 가입
1. 라운드가 이미 진행 중
2. 새로운 회원이 가입하여 임대권 구매
3. `update_round_ads.sh` 실행하여 신규 회원 광고 생성
4. Python 워커가 자동으로 광고 처리

### 시나리오 2: 정기적 신규 회원 확인
```bash
# 크론잡 설정 예시 (매일 오전 9시)
0 9 * * * cd /path/to/project && ./sh/update_round_ads.sh 1
```

## 파일 구조

```
sh/
├── update_round_ads.sh       # 신규 회원 광고 생성
├── check_round_members.sh    # 라운드 현황 확인
├── round_workflow.sh         # 전체 워크플로우
├── 1_create-round.sh         # 기존: 라운드 생성
├── 2_create_ad_tasks.sh      # 기존: 기존 회원 광고 생성
├── 3_create-order.sh         # 기존: 주문 생성
└── 4_create-post.sh          # 기존: 포스트 생성
```

## 로그 및 모니터링

### 성공 로그
```
=== 라운드 1 신규 회원 광고 생성 ===
라운드 ID: 1
신규 회원 수: 2
생성된 광고 수: 6
신규 회원 2명에 대해 6개의 광고가 생성되었고,
PENDING 상태의 광고들이 처리 큐에 추가되었습니다.
```

### 신규 회원이 없는 경우
```
=== 라운드 1 신규 회원 광고 생성 ===
신규 회원이 없어서 추가로 생성할 광고가 없습니다.
모든 활성 회원들이 이미 이 라운드의 광고를 가지고 있습니다.
```

## 주의사항

1. **타이밍**: 라운드 종료 후에도 신규 회원 광고 생성 가능
2. **중복 방지**: 동일한 스크립트를 여러 번 실행해도 안전
3. **임대권 확인**: 유효한 임대권을 가진 회원만 대상
4. **비동기 처리**: 생성된 광고는 Python 워커가 별도로 처리

## 트러블슈팅

### 문제: 신규 회원이 있는데 광고가 생성되지 않음
```bash
# 활성 회원 확인
curl -X GET http://localhost:8080/api/members/active

# 임대권 상태 확인
# 회원별로 유효한 임대권이 있는지 확인
```

### 문제: 광고가 생성됐지만 처리되지 않음
```bash
# PENDING 광고 큐 추가
curl -X POST http://localhost:8080/api/ad-tasks/rounds/1/enqueue-pending

# Python 워커 상태 확인 필요
```

## 확장 가능성

1. **자동화**: 크론잡으로 정기적 신규 회원 확인
2. **알림**: 신규 회원 광고 생성 시 슬랙/이메일 알림
3. **대시보드**: 라운드별 신규 회원 현황 대시보드
4. **배치 처리**: 여러 라운드 동시 처리