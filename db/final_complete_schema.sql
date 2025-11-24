-- ==========================================
-- 최종 완전 데이터베이스 스키마
-- 모든 마이그레이션이 적용된 최종 버전
-- 생성일: 2024-11-10
-- ==========================================

-- ==========================================
-- 1. 회원 테이블 (members)
-- ==========================================
CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '이메일 주소',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    company_name VARCHAR(255) NOT NULL COMMENT '회사명',
    business_registration_number VARCHAR(20) NOT NULL UNIQUE COMMENT '사업자등록번호',
    contact_number VARCHAR(20) NOT NULL COMMENT '연락처',
    business_registration_file VARCHAR(500) NOT NULL COMMENT '사업자등록증 파일 경로',
    telecommunication_sales_file VARCHAR(500) NOT NULL COMMENT '통신판매업신고증 파일 경로',
    
    -- 회원 상태 및 권한
    is_premium BOOLEAN DEFAULT FALSE COMMENT '프리미엄 회원 여부',
    rental_status VARCHAR(20) DEFAULT 'INACTIVE' CHECK (rental_status IN ('ACTIVE', 'EXPIRED', 'INACTIVE')) COMMENT '임대권 상태',
    current_rental_expiry DATE COMMENT '현재 임대권 만료일',
    
    -- 비즈니스 정보 (추가됨)
    business_field VARCHAR(255) COMMENT '사업분야',
    product_description VARCHAR(1000) COMMENT '상품/제품 소개',
    company_description VARCHAR(1000) COMMENT '회사 소개',
    
    -- 감사 필드
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- ==========================================
-- 2. 라운드 테이블 (rounds)
-- ==========================================
CREATE TABLE rounds (
    id BIGSERIAL PRIMARY KEY,
    round_number VARCHAR(20) UNIQUE COMMENT '라운드 번호 (Round #246)',
    title VARCHAR(255) NOT NULL COMMENT '라운드 제목',
    description TEXT COMMENT '라운드 설명',
    category VARCHAR(100) COMMENT '라운드 카테고리 (헬스케어 분야)',
    
    -- 비용 정보 (세분화됨)
    order_amount DECIMAL(12,2) NOT NULL COMMENT '라운드별 정해진 발주 금액 (총 비용)',
    template_cost DECIMAL(12,2) DEFAULT 0 COMMENT '템플릿 비용',
    ai_generation_cost DECIMAL(12,2) DEFAULT 0 COMMENT 'AI 기반 광고 생성비',
    targeting_posting_cost DECIMAL(12,2) DEFAULT 0 COMMENT '타게팅 게시비',
    server_rental_cost DECIMAL(12,2) DEFAULT 0 COMMENT '서버 임대비',
    other_costs DECIMAL(12,2) DEFAULT 0 COMMENT '기타 비용',
    
    -- 라운드 일정
    start_date TIMESTAMP WITH TIME ZONE NOT NULL COMMENT '라운드 시작일',
    end_date TIMESTAMP WITH TIME ZONE NOT NULL COMMENT '라운드 종료일',
    
    -- 게시 일정 (추가됨)
    post_start_date TIMESTAMP WITH TIME ZONE COMMENT '게시 시작일',
    post_end_date TIMESTAMP WITH TIME ZONE COMMENT '게시 종료일',
    post_duration_days INTEGER DEFAULT 7 CHECK (post_duration_days BETWEEN 1 AND 90) COMMENT '게시 기간 (일)',
    
    -- 라운드 설정
    status VARCHAR(20) DEFAULT 'PREPARING' CHECK (status IN ('PREPARING', 'ACTIVE', 'CLOSED', 'PENDING')) COMMENT '라운드 상태',
    max_participants INTEGER COMMENT '최대 참여자 수',
    created_by BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '라운드 생성자(관리자)',
    
    -- 감사 필드
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- ==========================================
-- 3. 발주(참여 신청) 테이블 (orders)
-- ==========================================
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(20) UNIQUE COMMENT '주문번호 (ORD-2024-001)',
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE COMMENT '참여한 라운드 ID',
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '발주 신청 회원 ID',
    
    -- 상품 정보
    product_name VARCHAR(255) NOT NULL COMMENT '상품명',
    quantity INTEGER NOT NULL CHECK (quantity > 0) COMMENT '수량',
    requirements TEXT COMMENT '특별 요구사항',
    
    -- 일정 관리
    deadline DATE COMMENT '마감일',
    start_date DATE COMMENT '시작일',
    completion_date DATE COMMENT '완료일',
    failure_date DATE COMMENT '실패일',
    
    -- 진행 상태
    progress_rate INTEGER DEFAULT 0 CHECK (progress_rate >= 0 AND progress_rate <= 100) COMMENT '진행률',
    failure_reason TEXT COMMENT '실패 사유',
    status VARCHAR(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAYMENT_WAITING', 'PAYMENT_CONFIRMED', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'CANCELLED')) COMMENT '발주 상태',
    
    -- 검토 정보
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '신청일시',
    reviewed_at TIMESTAMP WITH TIME ZONE COMMENT '검토일시',
    reviewed_by BIGINT REFERENCES members(id) COMMENT '검토자',
    notes TEXT COMMENT '관리자 메모',
    
    -- 감사 필드
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',

    UNIQUE(round_id, member_id) -- 한 라운드에 회원당 하나의 발주만 가능
);

-- ==========================================
-- 4. 광고 작업 테이블 (ad_tasks) - 새로 추가
-- ==========================================
CREATE TABLE ad_tasks (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE COMMENT '라운드 ID',
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '광고주 회원 ID',
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE COMMENT '발주 ID',
    
    -- 광고 타입 및 인덱스 (다중 광고 지원)
    ad_type VARCHAR(50) DEFAULT 'scratch' CHECK (ad_type IN ('scratch', 'carousel', 'interactive')) COMMENT '광고 타입',
    ad_index INTEGER DEFAULT 1 CHECK (ad_index > 0) COMMENT '광고 인덱스 (같은 타입 내에서의 순번)',
    
    -- 작업 상태
    task_status VARCHAR(20) DEFAULT 'PENDING' CHECK (task_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'RETRY')) COMMENT '작업 상태',
    
    -- 광고 내용
    ad_content TEXT COMMENT '광고 내용',
    description VARCHAR(20000) COMMENT '광고 작업 상세 설명',
    
    -- 파일 및 URL 정보
    html_file_path VARCHAR(500) COMMENT 'HTML 파일 경로',
    web_url VARCHAR(1000) COMMENT '웹 URL (iframe용)',
    
    -- 일정 추적
    task_started_at TIMESTAMP WITH TIME ZONE COMMENT '작업 시작일시',
    task_completed_at TIMESTAMP WITH TIME ZONE COMMENT '작업 완료일시',
    
    -- 오류 처리
    error_message TEXT COMMENT '오류 메시지',
    retry_count INTEGER DEFAULT 0 COMMENT '재시도 횟수',
    
    -- 감사 필드
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',
    
    UNIQUE(round_id, member_id, ad_type, ad_index) -- 라운드, 회원, 광고타입, 인덱스별 유니크 제약
);

-- ==========================================
-- 5. 시스템 설정 테이블 (system_settings)
-- ==========================================
CREATE TABLE system_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE COMMENT '설정 키',
    setting_value TEXT NOT NULL COMMENT '설정 값',
    description TEXT COMMENT '설정 설명',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- ==========================================
-- 6. 발주 입금 테이블 (order_payments)
-- ==========================================
CREATE TABLE order_payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE COMMENT '발주 ID',
    application_number VARCHAR(20) NOT NULL UNIQUE COMMENT '신청번호 (APP430928)',
    payment_amount DECIMAL(12,2) NOT NULL COMMENT '입금 금액',
    depositor_name VARCHAR(100) COMMENT '입금자명',
    payment_status VARCHAR(20) DEFAULT 'WAITING' CHECK (payment_status IN ('WAITING', 'CONFIRMED', 'FAILED')) COMMENT '입금 상태',
    payment_confirmed_at TIMESTAMP WITH TIME ZONE COMMENT '입금 확인일시',
    payment_confirmed_by BIGINT REFERENCES members(id) COMMENT '입금 확인한 관리자',
    bank_account_number VARCHAR(50) NOT NULL COMMENT '입금 계좌번호',
    bank_name VARCHAR(100) NOT NULL COMMENT '은행명',
    notes TEXT COMMENT '메모',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- ==========================================
-- 7. AI 생성 광고 컨텐츠 테이블 (ai_advertisements) - 기존 호환성 유지
-- ==========================================
CREATE TABLE ai_advertisements (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE COMMENT '라운드 ID',
    advertiser_member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '광고주 (광고 소유자)',
    title VARCHAR(255) NOT NULL COMMENT '광고 제목',
    advertisement_cost DECIMAL(12,2) NOT NULL COMMENT '광고비',
    html_file_path VARCHAR(500) COMMENT 'AI가 생성한 HTML 파일 경로',
    content_status VARCHAR(20) DEFAULT 'CONTENT_NEEDED' CHECK (
        content_status IN ('CONTENT_NEEDED', 'CONTENT_COMPLETED', 'WORK_COMPLETED')
    ) COMMENT '컨텐츠 상태',
    content_written_at TIMESTAMP WITH TIME ZONE COMMENT '컨텐츠 작성 완료일',
    work_completed_at TIMESTAMP WITH TIME ZONE COMMENT '작업 완료일',
    deadline DATE NOT NULL COMMENT '마감일',
    created_by BIGINT NOT NULL REFERENCES members(id) COMMENT '생성한 관리자',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',

    UNIQUE(round_id, advertiser_member_id) -- 한 라운드에서 한 회원당 하나의 광고만
);

-- ==========================================
-- 8. 광고 게시 할당 테이블 (advertisement_assignments)
-- ==========================================
CREATE TABLE advertisement_assignments (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE COMMENT '라운드 ID',
    advertiser_member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '광고주 (광고 소유자)',
    publisher_member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '게시자 (광고 게시하는 사람)',
    ad_task_id BIGINT REFERENCES ad_tasks(id) ON DELETE CASCADE COMMENT '광고 작업 ID',
    ai_advertisement_id BIGINT REFERENCES ai_advertisements(id) ON DELETE CASCADE COMMENT 'AI 광고 ID (호환성)',
    revenue_per_post DECIMAL(12,2) NOT NULL COMMENT '게시당 수익 (발주금액 ÷ (참여자수-1))',
    assignment_status VARCHAR(20) DEFAULT 'ASSIGNED' CHECK (
        assignment_status IN ('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'FAILED')
    ) COMMENT '할당 상태',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',

    UNIQUE(round_id, advertiser_member_id, publisher_member_id),
    CHECK (advertiser_member_id != publisher_member_id) -- 자기 광고는 게시 안함
);

-- ==========================================
-- 9. 광고 게시 테이블 (advertisement_posts)
-- ==========================================
CREATE TABLE advertisement_posts (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES advertisement_assignments(id) ON DELETE CASCADE COMMENT '광고 할당 ID',
    content TEXT COMMENT '게시자가 작성한 광고 컨텐츠',
    ctr_rate DECIMAL(5,2) COMMENT 'CTR 비율',
    final_revenue DECIMAL(12,2) COMMENT '최종 수익',
    post_status VARCHAR(20) DEFAULT 'PENDING' CHECK (
        post_status IN ('PENDING', 'APPROVED', 'REJECTED', 'PUBLISHED', 'FAILED')
    ) COMMENT '게시 상태',
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '컨텐츠 제출일',
    approved_at TIMESTAMP WITH TIME ZONE COMMENT '승인일',
    published_at TIMESTAMP WITH TIME ZONE COMMENT '게시일',
    failed_at TIMESTAMP WITH TIME ZONE COMMENT '게시 실패일',
    failure_reason TEXT COMMENT '실패 사유',
    rejection_reason TEXT COMMENT '거절 사유',
    reviewed_by BIGINT REFERENCES members(id) COMMENT '검토한 관리자',
    notes TEXT COMMENT '관리자 메모',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- ==========================================
-- 10. 수익 거래 테이블 (revenue_transactions)
-- ==========================================
CREATE TABLE revenue_transactions (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '회원 ID',
    transaction_type VARCHAR(10) NOT NULL CHECK (transaction_type IN ('INCOME', 'EXPENSE')) COMMENT '거래 유형 (매출/매입)',

    -- 매출 관련 (광고 게시 수익)
    assignment_id BIGINT REFERENCES advertisement_assignments(id) ON DELETE SET NULL COMMENT '광고 할당 ID',
    round_number VARCHAR(20) COMMENT '라운드 번호',
    round_category VARCHAR(100) COMMENT '라운드 카테고리',
    ctr_rate DECIMAL(5,2) COMMENT 'CTR 비율',

    -- 매입 관련 (발주 비용)
    order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL COMMENT '발주 ID',
    order_number VARCHAR(20) COMMENT '발주 번호',
    order_status VARCHAR(20) COMMENT '발주 상태',

    -- 공통 필드
    title VARCHAR(255) NOT NULL COMMENT '거래 제목',
    amount DECIMAL(12,2) NOT NULL COMMENT '금액',
    transaction_date DATE NOT NULL COMMENT '거래일',
    tax_invoice_issued BOOLEAN DEFAULT FALSE COMMENT '전자세금계산서 발행 여부',
    tax_invoice_number VARCHAR(50) COMMENT '세금계산서 번호',
    description TEXT COMMENT '설명',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- ==========================================
-- 11. 정산 계좌 정보 테이블 (settlement_accounts)
-- ==========================================
CREATE TABLE settlement_accounts (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '회원 ID',
    bank_code VARCHAR(20) NOT NULL COMMENT '은행 코드',
    bank_name VARCHAR(100) NOT NULL COMMENT '은행명',
    account_number VARCHAR(50) NOT NULL COMMENT '계좌번호',
    account_holder VARCHAR(100) NOT NULL COMMENT '예금주',
    is_default BOOLEAN DEFAULT FALSE COMMENT '기본 계좌 여부',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',

    UNIQUE(member_id, account_number) -- 같은 회원이 같은 계좌번호 중복 등록 방지
);

-- ==========================================
-- 12. 정산 배치 테이블 (settlement_batches)
-- ==========================================
CREATE TABLE settlement_batches (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE COMMENT '라운드 ID',
    settlement_date DATE NOT NULL COMMENT '정산일',
    total_participants INTEGER NOT NULL COMMENT '참여자 수',
    total_settlement_amount DECIMAL(15,2) NOT NULL COMMENT '총 정산 금액',
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')) COMMENT '정산 상태',
    processed_at TIMESTAMP WITH TIME ZONE COMMENT '처리일시',
    processed_by BIGINT REFERENCES members(id) COMMENT '처리한 관리자',
    notes TEXT COMMENT '메모',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- ==========================================
-- 13. 개별 정산 내역 테이블 (member_settlements)
-- ==========================================
CREATE TABLE member_settlements (
    id BIGSERIAL PRIMARY KEY,
    settlement_batch_id BIGINT NOT NULL REFERENCES settlement_batches(id) ON DELETE CASCADE COMMENT '정산 배치 ID',
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '회원 ID',
    settlement_account_id BIGINT NOT NULL REFERENCES settlement_accounts(id) COMMENT '정산 계좌 ID',

    -- 정산 금액 계산
    total_income DECIMAL(12,2) DEFAULT 0 COMMENT '총 매출 (광고 게시 수익)',
    total_expense DECIMAL(12,2) DEFAULT 0 COMMENT '총 매입 (발주 비용)',
    net_amount DECIMAL(12,2) NOT NULL COMMENT '순 정산 금액',

    -- 정산 처리
    settlement_status VARCHAR(20) DEFAULT 'PENDING' CHECK (
        settlement_status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')
    ) COMMENT '정산 상태',
    settlement_date DATE NOT NULL COMMENT '정산일',
    processed_at TIMESTAMP WITH TIME ZONE COMMENT '처리일시',
    failure_reason TEXT COMMENT '실패 사유',

    -- 은행 정보 (정산 시점 스냅샷)
    bank_name VARCHAR(100) NOT NULL COMMENT '은행명',
    account_number VARCHAR(50) NOT NULL COMMENT '계좌번호',
    account_holder VARCHAR(100) NOT NULL COMMENT '예금주',

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',

    UNIQUE(settlement_batch_id, member_id) -- 한 배치에서 회원당 하나의 정산만
);

-- ==========================================
-- 14. 회원별 정산 요약 테이블 (member_settlement_summary)
-- ==========================================
CREATE TABLE member_settlement_summary (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE UNIQUE COMMENT '회원 ID',

    -- 누적 정산 정보
    total_accumulated_amount DECIMAL(15,2) DEFAULT 0 COMMENT '누적 정산 금액',
    last_settlement_date DATE COMMENT '마지막 정산일',
    next_settlement_date DATE COMMENT '다음 정산일',
    pending_settlement_amount DECIMAL(12,2) DEFAULT 0 COMMENT '정산 예정 금액',

    -- 기본 정산 계좌
    default_settlement_account_id BIGINT REFERENCES settlement_accounts(id) COMMENT '기본 정산 계좌 ID',

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- ==========================================
-- 15. 임대권 테이블 (rental_rights)
-- ==========================================
CREATE TABLE rental_rights (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '회원 ID',
    purchase_date DATE NOT NULL COMMENT '임대권 구매일',
    expiry_date DATE NOT NULL COMMENT '임대권 만료일',
    rental_amount DECIMAL(12,2) NOT NULL DEFAULT 100000000 COMMENT '임대권 금액 (1억원 고정)',
    
    -- 가격 선호도 (추가됨)
    pricing_preference VARCHAR(20) DEFAULT 'undecided' CHECK (pricing_preference IN ('highest', 'lowest', 'undecided')) COMMENT '가격 선호도',
    
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED')) COMMENT '임대권 상태',
    auto_renewal BOOLEAN DEFAULT FALSE COMMENT '자동 갱신 여부',
    renewal_notice_sent BOOLEAN DEFAULT FALSE COMMENT '갱신 안내 발송 여부',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- ==========================================
-- 16. 서비스 계약 테이블 (service_contracts)
-- ==========================================
CREATE TABLE service_contracts (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '회원 ID',
    rental_contract_agreed BOOLEAN DEFAULT FALSE COMMENT '임대권 구매 계약 동의',
    service_contract_agreed BOOLEAN DEFAULT FALSE COMMENT '광고 게시 용역 계약 동의',
    marketing_agreed BOOLEAN DEFAULT FALSE COMMENT '마케팅 정보 수신 동의',
    contract_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '계약 체결일',
    contract_version VARCHAR(10) DEFAULT '1.0' COMMENT '계약서 버전',
    ip_address VARCHAR(45) COMMENT '계약 체결 시 IP 주소',
    user_agent TEXT COMMENT '계약 체결 시 사용자 환경',
    is_active BOOLEAN DEFAULT TRUE COMMENT '계약 활성화 여부',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- ==========================================
-- 17. 회사 필수 정보 테이블 (company_essentials) - 새로 추가
-- ==========================================
CREATE TABLE company_essentials (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL COMMENT '회사명',
    search_keyword VARCHAR(255) NOT NULL COMMENT '검색 키워드',
    
    -- XML 파일 정보
    xml_file_path VARCHAR(500) COMMENT 'XML 파일 저장 경로',
    xml_file_size BIGINT COMMENT 'XML 파일 크기 (bytes)',
    xml_updated_at TIMESTAMP WITH TIME ZONE COMMENT 'XML 파일 마지막 업데이트',
    
    -- 수집 상태
    collection_status VARCHAR(20) DEFAULT 'PENDING' CHECK (
        collection_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'SCHEDULED')
    ) COMMENT '수집 상태',
    
    -- 자동 업데이트 설정
    auto_update_enabled BOOLEAN DEFAULT TRUE COMMENT '자동 업데이트 활성화',
    update_frequency_days INTEGER DEFAULT 30 CHECK (update_frequency_days > 0) COMMENT '업데이트 주기 (일)',
    next_update_date DATE COMMENT '다음 업데이트 예정일',
    
    -- 오류 처리
    error_count INTEGER DEFAULT 0 COMMENT '연속 실패 횟수',
    last_error_message TEXT COMMENT '마지막 오류 메시지',
    max_retry_count INTEGER DEFAULT 3 COMMENT '최대 재시도 횟수',
    
    -- 메타데이터
    data_quality_score DECIMAL(3,2) COMMENT '데이터 품질 점수 (0.00-1.00)',
    record_count INTEGER COMMENT '수집된 레코드 수',
    
    -- 감사 필드
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',
    
    UNIQUE(company_name, search_keyword) -- 회사명-키워드 조합 유니크
);

-- ==========================================
-- 18. 회사 필수 정보 로그 테이블 (company_essential_logs) - 새로 추가
-- ==========================================
CREATE TABLE company_essential_logs (
    id BIGSERIAL PRIMARY KEY,
    company_essential_id BIGINT NOT NULL REFERENCES company_essentials(id) ON DELETE CASCADE COMMENT '회사 필수 정보 ID',
    
    -- 실행 정보
    execution_type VARCHAR(20) NOT NULL CHECK (execution_type IN ('MANUAL', 'SCHEDULED', 'RETRY')) COMMENT '실행 유형',
    execution_status VARCHAR(20) NOT NULL CHECK (execution_status IN ('STARTED', 'SUCCESS', 'FAILED', 'CANCELLED')) COMMENT '실행 상태',
    
    -- 시간 정보
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '시작 시간',
    completed_at TIMESTAMP WITH TIME ZONE COMMENT '완료 시간',
    execution_duration_seconds INTEGER COMMENT '실행 소요 시간 (초)',
    
    -- 결과 정보
    records_processed INTEGER DEFAULT 0 COMMENT '처리된 레코드 수',
    records_added INTEGER DEFAULT 0 COMMENT '추가된 레코드 수',
    records_updated INTEGER DEFAULT 0 COMMENT '업데이트된 레코드 수',
    records_deleted INTEGER DEFAULT 0 COMMENT '삭제된 레코드 수',
    
    -- 오류 정보
    error_message TEXT COMMENT '오류 메시지',
    error_code VARCHAR(50) COMMENT '오류 코드',
    
    -- 메타데이터
    notes TEXT COMMENT '실행 노트',
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시'
);

-- ==========================================
-- 인덱스 생성
-- ==========================================

-- members 테이블
CREATE INDEX idx_members_email ON members(email);
CREATE INDEX idx_members_business_number ON members(business_registration_number);
CREATE INDEX idx_members_premium ON members(is_premium);
CREATE INDEX idx_members_rental_status ON members(rental_status);

-- rounds 테이블
CREATE INDEX idx_rounds_status ON rounds(status);
CREATE INDEX idx_rounds_dates ON rounds(start_date, end_date);
CREATE INDEX idx_rounds_post_dates ON rounds(post_start_date, post_end_date);
CREATE INDEX idx_rounds_created_by ON rounds(created_by);
CREATE INDEX idx_rounds_round_number ON rounds(round_number);

-- orders 테이블
CREATE INDEX idx_orders_round_id ON orders(round_id);
CREATE INDEX idx_orders_member_id ON orders(member_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_submitted_at ON orders(submitted_at);
CREATE INDEX idx_orders_order_number ON orders(order_number);

-- ad_tasks 테이블 (새로 추가)
CREATE INDEX idx_ad_tasks_round_id ON ad_tasks(round_id);
CREATE INDEX idx_ad_tasks_member_id ON ad_tasks(member_id);
CREATE INDEX idx_ad_tasks_order_id ON ad_tasks(order_id);
CREATE INDEX idx_ad_tasks_status ON ad_tasks(task_status);
CREATE INDEX idx_ad_tasks_type_index ON ad_tasks(ad_type, ad_index);

-- system_settings 테이블
CREATE INDEX idx_system_settings_key ON system_settings(setting_key);

-- order_payments 테이블
CREATE INDEX idx_order_payments_order_id ON order_payments(order_id);
CREATE INDEX idx_order_payments_status ON order_payments(payment_status);
CREATE INDEX idx_order_payments_application_number ON order_payments(application_number);

-- ai_advertisements 테이블
CREATE INDEX idx_ai_advertisements_round_id ON ai_advertisements(round_id);
CREATE INDEX idx_ai_advertisements_advertiser_member ON ai_advertisements(advertiser_member_id);
CREATE INDEX idx_ai_advertisements_content_status ON ai_advertisements(content_status);
CREATE INDEX idx_ai_advertisements_deadline ON ai_advertisements(deadline);

-- advertisement_assignments 테이블
CREATE INDEX idx_advertisement_assignments_round_id ON advertisement_assignments(round_id);
CREATE INDEX idx_advertisement_assignments_advertiser ON advertisement_assignments(advertiser_member_id);
CREATE INDEX idx_advertisement_assignments_publisher ON advertisement_assignments(publisher_member_id);
CREATE INDEX idx_advertisement_assignments_ad_task_id ON advertisement_assignments(ad_task_id);
CREATE INDEX idx_advertisement_assignments_ai_ad_id ON advertisement_assignments(ai_advertisement_id);
CREATE INDEX idx_advertisement_assignments_status ON advertisement_assignments(assignment_status);

-- advertisement_posts 테이블
CREATE INDEX idx_advertisement_posts_assignment_id ON advertisement_posts(assignment_id);
CREATE INDEX idx_advertisement_posts_status ON advertisement_posts(post_status);
CREATE INDEX idx_advertisement_posts_submitted_at ON advertisement_posts(submitted_at);

-- revenue_transactions 테이블
CREATE INDEX idx_revenue_transactions_member_id ON revenue_transactions(member_id);
CREATE INDEX idx_revenue_transactions_type ON revenue_transactions(transaction_type);
CREATE INDEX idx_revenue_transactions_date ON revenue_transactions(transaction_date);
CREATE INDEX idx_revenue_transactions_order_id ON revenue_transactions(order_id);
CREATE INDEX idx_revenue_transactions_assignment_id ON revenue_transactions(assignment_id);

-- settlement_accounts 테이블
CREATE INDEX idx_settlement_accounts_member_id ON settlement_accounts(member_id);
CREATE INDEX idx_settlement_accounts_is_default ON settlement_accounts(is_default);

-- settlement_batches 테이블
CREATE INDEX idx_settlement_batches_round_id ON settlement_batches(round_id);
CREATE INDEX idx_settlement_batches_settlement_date ON settlement_batches(settlement_date);
CREATE INDEX idx_settlement_batches_status ON settlement_batches(status);

-- member_settlements 테이블
CREATE INDEX idx_member_settlements_batch_id ON member_settlements(settlement_batch_id);
CREATE INDEX idx_member_settlements_member_id ON member_settlements(member_id);
CREATE INDEX idx_member_settlements_settlement_date ON member_settlements(settlement_date);
CREATE INDEX idx_member_settlements_status ON member_settlements(settlement_status);

-- rental_rights 테이블
CREATE INDEX idx_rental_rights_member_id ON rental_rights(member_id);
CREATE INDEX idx_rental_rights_status ON rental_rights(status);
CREATE INDEX idx_rental_rights_expiry_date ON rental_rights(expiry_date);
CREATE INDEX idx_rental_rights_purchase_date ON rental_rights(purchase_date);
CREATE INDEX idx_rental_rights_pricing_preference ON rental_rights(pricing_preference);

-- service_contracts 테이블
CREATE INDEX idx_service_contracts_member_id ON service_contracts(member_id);
CREATE INDEX idx_service_contracts_is_active ON service_contracts(is_active);
CREATE INDEX idx_service_contracts_contract_date ON service_contracts(contract_date);

-- company_essentials 테이블 (새로 추가)
CREATE INDEX idx_company_essentials_company_name ON company_essentials(company_name);
CREATE INDEX idx_company_essentials_collection_status ON company_essentials(collection_status);
CREATE INDEX idx_company_essentials_next_update ON company_essentials(next_update_date);
CREATE INDEX idx_company_essentials_auto_update ON company_essentials(auto_update_enabled);

-- company_essential_logs 테이블 (새로 추가)
CREATE INDEX idx_company_essential_logs_company_id ON company_essential_logs(company_essential_id);
CREATE INDEX idx_company_essential_logs_execution_status ON company_essential_logs(execution_status);
CREATE INDEX idx_company_essential_logs_started_at ON company_essential_logs(started_at);

-- ==========================================
-- 함수 생성
-- ==========================================

-- updated_at 자동 업데이트 함수
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 신청번호 생성 함수
CREATE OR REPLACE FUNCTION generate_application_number()
RETURNS VARCHAR(20) AS $$
DECLARE
    app_number VARCHAR(20);
    counter INTEGER;
BEGIN
    -- APP + YYMMDD + 순번 형식
    SELECT COUNT(*) + 1 INTO counter
    FROM order_payments
    WHERE DATE(created_at) = CURRENT_DATE;

    app_number := 'APP' || TO_CHAR(CURRENT_DATE, 'YYMMDD') || LPAD(counter::TEXT, 2, '0');

    RETURN app_number;
END;
$$ LANGUAGE plpgsql;

-- 주문번호 생성 함수
CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS VARCHAR(20) AS $$
DECLARE
    order_num VARCHAR(20);
    counter INTEGER;
BEGIN
    -- ORD-YYYY-XXX 형식
    SELECT COUNT(*) + 1 INTO counter
    FROM orders
    WHERE EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_DATE);

    order_num := 'ORD-' || EXTRACT(YEAR FROM CURRENT_DATE) || '-' || LPAD(counter::TEXT, 3, '0');

    RETURN order_num;
END;
$$ LANGUAGE plpgsql;

-- 라운드 번호 생성 함수
CREATE OR REPLACE FUNCTION generate_round_number()
RETURNS VARCHAR(20) AS $$
DECLARE
    round_num VARCHAR(20);
    counter INTEGER;
BEGIN
    SELECT COALESCE(MAX(CAST(SUBSTRING(round_number FROM 8) AS INTEGER)), 0) + 1
    INTO counter
    FROM rounds
    WHERE round_number IS NOT NULL;

    round_num := 'Round #' || counter;

    RETURN round_num;
END;
$$ LANGUAGE plpgsql;

-- 라운드 광고 할당 생성 함수 (업데이트됨)
CREATE OR REPLACE FUNCTION create_advertisement_assignments(p_round_id BIGINT)
RETURNS VOID AS $$
DECLARE
    participant_count INTEGER;
    revenue_per_post DECIMAL(12,2);
    round_order_amount DECIMAL(12,2);
BEGIN
    -- 라운드 참여자 수와 발주 금액 조회
    SELECT COUNT(*), r.order_amount
    INTO participant_count, round_order_amount
    FROM orders o
    JOIN rounds r ON o.round_id = r.id
    WHERE o.round_id = p_round_id AND o.status = 'COMPLETED'
    GROUP BY r.order_amount;

    -- 게시당 수익 계산 (발주금액 ÷ (참여자수-1))
    revenue_per_post := FLOOR(round_order_amount / (participant_count - 1));

    -- ad_tasks 기반으로 광고 할당 생성
    INSERT INTO advertisement_assignments (
        round_id, advertiser_member_id, publisher_member_id,
        ad_task_id, revenue_per_post
    )
    SELECT
        p_round_id,
        advertiser.member_id as advertiser_member_id,
        publisher.member_id as publisher_member_id,
        at.id as ad_task_id,
        revenue_per_post
    FROM orders advertiser
    CROSS JOIN orders publisher
    JOIN ad_tasks at ON at.round_id = p_round_id
        AND at.member_id = advertiser.member_id
        AND at.task_status = 'COMPLETED'
    WHERE advertiser.round_id = p_round_id
        AND publisher.round_id = p_round_id
        AND advertiser.member_id != publisher.member_id  -- 자기 광고는 게시 안함
        AND advertiser.status = 'COMPLETED'
        AND publisher.status = 'COMPLETED';
END;
$$ LANGUAGE plpgsql;

-- 회사 필수정보 자동 업데이트 스케줄링 함수 (새로 추가)
CREATE OR REPLACE FUNCTION schedule_company_essentials_update()
RETURNS VOID AS $$
BEGIN
    UPDATE company_essentials 
    SET collection_status = 'SCHEDULED',
        next_update_date = CURRENT_DATE + INTERVAL '1 day' * update_frequency_days
    WHERE auto_update_enabled = TRUE 
        AND (next_update_date IS NULL OR next_update_date <= CURRENT_DATE)
        AND collection_status NOT IN ('PROCESSING', 'SCHEDULED');
END;
$$ LANGUAGE plpgsql;

-- ==========================================
-- 트리거 생성
-- ==========================================

-- updated_at 자동 업데이트 트리거들
CREATE TRIGGER trigger_members_updated_at BEFORE UPDATE ON members FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_rounds_updated_at BEFORE UPDATE ON rounds FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_orders_updated_at BEFORE UPDATE ON orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_ad_tasks_updated_at BEFORE UPDATE ON ad_tasks FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_system_settings_updated_at BEFORE UPDATE ON system_settings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_order_payments_updated_at BEFORE UPDATE ON order_payments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_ai_advertisements_updated_at BEFORE UPDATE ON ai_advertisements FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_advertisement_assignments_updated_at BEFORE UPDATE ON advertisement_assignments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_advertisement_posts_updated_at BEFORE UPDATE ON advertisement_posts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_revenue_transactions_updated_at BEFORE UPDATE ON revenue_transactions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_settlement_accounts_updated_at BEFORE UPDATE ON settlement_accounts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_settlement_batches_updated_at BEFORE UPDATE ON settlement_batches FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_member_settlements_updated_at BEFORE UPDATE ON member_settlements FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_member_settlement_summary_updated_at BEFORE UPDATE ON member_settlement_summary FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_rental_rights_updated_at BEFORE UPDATE ON rental_rights FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_service_contracts_updated_at BEFORE UPDATE ON service_contracts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_company_essentials_updated_at BEFORE UPDATE ON company_essentials FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ==========================================
-- 기본 데이터 삽입
-- ==========================================

-- 시스템 설정 기본값
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('maintenance_mode', 'false', '시스템 점검 모드'),
('max_participants_per_round', '100', '라운드 당 최대 참여자 수'),
('default_post_duration_days', '7', '기본 게시 기간 (일)'),
('auto_settlement_enabled', 'true', '자동 정산 활성화 여부'),
('company_data_update_frequency', '30', '회사 데이터 업데이트 주기 (일)')
ON CONFLICT (setting_key) DO NOTHING;

-- ==========================================
-- 뷰 생성
-- ==========================================

-- 라운드별 참여 현황 뷰
CREATE OR REPLACE VIEW round_participation_status AS
SELECT 
    r.id as round_id,
    r.round_number,
    r.title as round_title,
    r.status as round_status,
    r.max_participants,
    COUNT(o.id) as current_participants,
    COUNT(CASE WHEN o.status = 'COMPLETED' THEN 1 END) as completed_participants,
    r.order_amount,
    r.start_date,
    r.end_date,
    r.post_start_date,
    r.post_end_date
FROM rounds r
LEFT JOIN orders o ON r.id = o.round_id
GROUP BY r.id, r.round_number, r.title, r.status, r.max_participants, r.order_amount, r.start_date, r.end_date, r.post_start_date, r.post_end_date;

-- 광고 작업 현황 뷰
CREATE OR REPLACE VIEW ad_task_status_summary AS
SELECT 
    r.id as round_id,
    r.round_number,
    r.title as round_title,
    COUNT(at.id) as total_tasks,
    COUNT(CASE WHEN at.task_status = 'PENDING' THEN 1 END) as pending_tasks,
    COUNT(CASE WHEN at.task_status = 'PROCESSING' THEN 1 END) as processing_tasks,
    COUNT(CASE WHEN at.task_status = 'COMPLETED' THEN 1 END) as completed_tasks,
    COUNT(CASE WHEN at.task_status = 'FAILED' THEN 1 END) as failed_tasks
FROM rounds r
LEFT JOIN ad_tasks at ON r.id = at.round_id
GROUP BY r.id, r.round_number, r.title;

-- 회사 데이터 수집 현황 뷰
CREATE OR REPLACE VIEW company_data_collection_status AS
SELECT 
    ce.id,
    ce.company_name,
    ce.search_keyword,
    ce.collection_status,
    ce.next_update_date,
    ce.error_count,
    cel.execution_status as last_execution_status,
    cel.started_at as last_execution_time,
    cel.execution_duration_seconds as last_execution_duration
FROM company_essentials ce
LEFT JOIN LATERAL (
    SELECT * FROM company_essential_logs cel2 
    WHERE cel2.company_essential_id = ce.id 
    ORDER BY cel2.started_at DESC 
    LIMIT 1
) cel ON true;

-- ==========================================
-- 스키마 정보 요약
-- ==========================================

/*
최종 스키마 요약:

주요 테이블:
1. members - 회원 정보 (비즈니스 필드 추가)
2. rounds - 라운드 정보 (준비 상태, 게시 일정, 세분화된 비용 추가)
3. orders - 발주 정보
4. ad_tasks - 광고 작업 관리 (새로 추가, 다중 광고 지원)
5. system_settings - 시스템 설정
6. order_payments - 입금 관리
7. ai_advertisements - AI 광고 (호환성 유지)
8. advertisement_assignments - 광고 할당
9. advertisement_posts - 광고 게시
10. revenue_transactions - 수익 거래
11. settlement_accounts - 정산 계좌
12. settlement_batches - 정산 배치
13. member_settlements - 개별 정산
14. member_settlement_summary - 정산 요약
15. rental_rights - 임대권 (가격 선호도 추가)
16. service_contracts - 서비스 계약
17. company_essentials - 회사 필수 정보 (새로 추가)
18. company_essential_logs - 회사 정보 수집 로그 (새로 추가)

주요 기능:
- 라운드 준비 단계 지원
- 다중 광고 타입 및 인덱스 지원
- 게시 일정 관리
- 세분화된 비용 관리
- 회사 데이터 자동 수집
- 가격 선호도 관리
- 완전한 감사 로그 시스템
*/