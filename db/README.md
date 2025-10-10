# 데이터베이스 스키마 관리

## 📁 파일 구조

### 🏗️ 메인 DDL 파일
- `posts_final.sql` - **순환발주 기반 광고 소개 블로그 시스템** (최종 버전)
- `init.sql` - 기본 테이블 초기화 스크립트
- `create_ad_tasks_table.sql` - 광고 작업 테이블
- `create_company_essentials_table.sql` - 회사 필수 정보 테이블

### 🔧 스키마 수정 파일
- `add_ad_type_and_index_columns.sql` - 광고 타입 및 인덱스 컬럼 추가
- `add_audit_columns.sql` - 감사 컬럼 추가  
- `add_round_cost_columns.sql` - 라운드 비용 컬럼 추가
- `add_web_url_to_ad_tasks.sql` - 광고 작업에 웹 URL 추가

### 🧪 테스트 및 검증 파일  
- `check_contract_status.sql` - 계약 상태 확인
- `test_expired_rental.sql` - 만료된 렌탈 테스트

### 📦 백업 파일 (bak/)
- `posts_*.sql` - 게시글 시스템 개발 과정에서 생성된 이전 버전들

## 🎯 핵심 시스템: 순환발주 기반 광고 소개 블로그

### 📋 시스템 개요
- **목적**: 라운드 참여자들이 서로의 광고를 소개하는 블로그 시스템
- **원리**: n명 참여 시 각자 n-1개의 게시글 작성 (자신 제외)
- **비용**: 순환발주 알고리즘으로 정확한 원금보장 계산

### 🔄 순환발주 로직
```
참여자: A(100만), B(200만), C(150만), D(50만)
결과: 각자 투자금만큼 게시글 작성비로 받음
```

### 📊 주요 특징
1. **라운드 기반 일정**: 게시 시작/종료일이 라운드와 동일
2. **중복 방지**: 동일 광고에 대해 사용자당 1개 게시글만
3. **실시간 모니터링**: 완성도, 밸런스 체크 뷰 제공
4. **비용 추적**: 순환발주로 계산된 정확한 할당 비용

## 🗃️ 주요 테이블

### posts
```sql
- id: 게시글 ID
- title: 제목
- content: 내용  
- author_id: 작성자 (members 참조)
- round_id: 라운드 (rounds 참조)
- target_ad_task_id: 대상 광고 (ad_tasks 참조)
- assigned_cost: 할당된 비용 (DECIMAL)
- post_type: 게시글 유형 (ENUM)
- status: 상태 (DRAFT/PUBLISHED/HIDDEN)
- post_start_date: 게시 시작일시
- post_end_date: 게시 종료일시
```

## 📈 관리 뷰

### circular_balance_check
순환발주 밸런스 검증 (받을 금액 = 지불 금액)

### round_completion_status  
라운드별 완성도 추적 (n-1개 게시글 작성 여부)

### active_posts
현재 게시 기간 중인 활성 게시글

### round_post_stats
라운드별 게시글 통계 및 기간 현황

## 🚀 사용법

1. **초기 설정**
   ```sql
   SOURCE init.sql;
   SOURCE posts_final.sql;
   ```

2. **라운드별 게시글 현황 확인**
   ```sql
   SELECT * FROM round_completion_status;
   ```

3. **순환발주 밸런스 체크**
   ```sql
   SELECT * FROM circular_balance_check WHERE ABS(balance) > 0.01;
   ```

4. **현재 활성 게시글 조회**
   ```sql
   SELECT * FROM active_posts ORDER BY assigned_cost DESC;
   ```

---
*이 시스템은 수학적으로 정확한 순환발주 알고리즘을 기반으로 구현되었습니다.*