
-- 1. 회원 테이블
CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '이메일 주소',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    company_name VARCHAR(255) NOT NULL COMMENT '회사명',
    business_registration_number VARCHAR(20) NOT NULL UNIQUE COMMENT '사업자등록번호',
    contact_number VARCHAR(20) NOT NULL COMMENT '연락처',
    business_registration_file VARCHAR(500) NOT NULL COMMENT '사업자등록증 파일 경로',
    telecommunication_sales_file VARCHAR(500) NOT NULL COMMENT '통신판매업신고증 파일 경로',
    is_premium BOOLEAN DEFAULT FALSE COMMENT '프리미엄 회원 여부',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- 2. 라운드 테이블
CREATE TABLE rounds (
    id BIGSERIAL PRIMARY KEY,
    round_number VARCHAR(20) UNIQUE COMMENT '라운드 번호 (Round #246)',
    title VARCHAR(255) NOT NULL COMMENT '라운드 제목',
    description TEXT COMMENT '라운드 설명',
    category VARCHAR(100) COMMENT '라운드 카테고리 (헬스케어 분야)',
    order_amount DECIMAL(12,2) NOT NULL COMMENT '라운드별 정해진 발주 금액',
    start_date TIMESTAMP WITH TIME ZONE NOT NULL COMMENT '라운드 시작일',
    end_date TIMESTAMP WITH TIME ZONE NOT NULL COMMENT '라운드 종료일',
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CLOSED', 'PENDING')) COMMENT '라운드 상태',
    max_participants INTEGER COMMENT '최대 참여자 수',
    created_by BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '라운드 생성자(관리자)',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- 3. 발주(참여 신청) 테이블
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(20) UNIQUE COMMENT '주문번호 (ORD-2024-001)',
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE COMMENT '참여한 라운드 ID',
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '발주 신청 회원 ID',
    product_name VARCHAR(255) NOT NULL COMMENT '상품명',
    quantity INTEGER NOT NULL CHECK (quantity > 0) COMMENT '수량',
    requirements TEXT COMMENT '특별 요구사항',
    deadline DATE COMMENT '마감일',
    start_date DATE COMMENT '시작일',
    completion_date DATE COMMENT '완료일',
    failure_date DATE COMMENT '실패일',
    progress_rate INTEGER DEFAULT 0 CHECK (progress_rate >= 0 AND progress_rate <= 100) COMMENT '진행률',
    failure_reason TEXT COMMENT '실패 사유',
    status VARCHAR(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAYMENT_WAITING', 'PAYMENT_CONFIRMED', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'CANCELLED')) COMMENT '발주 상태',
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '신청일시',
    reviewed_at TIMESTAMP WITH TIME ZONE COMMENT '검토일시',
    reviewed_by BIGINT REFERENCES members(id) COMMENT '검토자',
    notes TEXT COMMENT '관리자 메모',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',

    UNIQUE(round_id, member_id) -- 한 라운드에 회원당 하나의 발주만 가능
);

-- 4. 시스템 설정 테이블
CREATE TABLE system_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE COMMENT '설정 키',
    setting_value TEXT NOT NULL COMMENT '설정 값',
    description TEXT COMMENT '설정 설명',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시'
);

-- 5. 발주 입금 테이블
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

-- 6. AI 생성 광고 컨텐츠 테이블
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

-- 7. 광고 게시 할당 테이블 (핵심 추가)
CREATE TABLE advertisement_assignments (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE COMMENT '라운드 ID',
    advertiser_member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '광고주 (광고 소유자)',
    publisher_member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '게시자 (광고 게시하는 사람)',
    ai_advertisement_id BIGINT NOT NULL REFERENCES ai_advertisements(id) ON DELETE CASCADE COMMENT 'AI 광고 ID',
    revenue_per_post DECIMAL(12,2) NOT NULL COMMENT '게시당 수익 (발주금액 ÷ (참여자수-1))',
    assignment_status VARCHAR(20) DEFAULT 'ASSIGNED' CHECK (
        assignment_status IN ('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'FAILED')
    ) COMMENT '할당 상태',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',

    UNIQUE(round_id, advertiser_member_id, publisher_member_id),
    CHECK (advertiser_member_id != publisher_member_id) -- 자기 광고는 게시 안함
);

-- 8. 광고 게시 테이블
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

-- 9. 수익 거래 테이블
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

-- 10. 정산 계좌 정보 테이블
CREATE TABLE settlement_accounts (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE COMMENT '회원 ID',
    bank_name VARCHAR(100) NOT NULL COMMENT '은행명',
    account_number VARCHAR(50) NOT NULL COMMENT '계좌번호',
    account_holder VARCHAR(100) NOT NULL COMMENT '예금주',
    is_default BOOLEAN DEFAULT FALSE COMMENT '기본 계좌 여부',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',

    UNIQUE(member_id, account_number) -- 같은 회원이 같은 계좌번호 중복 등록 방지
);

-- 11. 정산 배치 테이블
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

-- 12. 개별 정산 내역 테이블
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

-- 13. 회원별 정산 요약 테이블
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
-- 인덱스 생성
-- ==========================================

-- members 테이블
CREATE INDEX idx_members_email ON members(email);
CREATE INDEX idx_members_business_number ON members(business_registration_number);
CREATE INDEX idx_members_premium ON members(is_premium);

-- rounds 테이블
CREATE INDEX idx_rounds_status ON rounds(status);
CREATE INDEX idx_rounds_dates ON rounds(start_date, end_date);
CREATE INDEX idx_rounds_created_by ON rounds(created_by);
CREATE INDEX idx_rounds_round_number ON rounds(round_number);

-- orders 테이블
CREATE INDEX idx_orders_round_id ON orders(round_id);
CREATE INDEX idx_orders_member_id ON orders(member_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_submitted_at ON orders(submitted_at);
CREATE INDEX idx_orders_order_number ON orders(order_number);

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

-- advertisement_assignments 테이블 (핵심 추가)
CREATE INDEX idx_advertisement_assignments_round_id ON advertisement_assignments(round_id);
CREATE INDEX idx_advertisement_assignments_advertiser ON advertisement_assignments(advertiser_member_id);
CREATE INDEX idx_advertisement_assignments_publisher ON advertisement_assignments(publisher_member_id);
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

-- 라운드 광고 할당 생성 함수 (핵심 추가)
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
    revenue_per_post := round_order_amount / (participant_count - 1);

    -- 모든 참여자에 대해 광고 할당 생성
    INSERT INTO advertisement_assignments (
        round_id, advertiser_member_id, publisher_member_id,
        ai_advertisement_id, revenue_per_post
    )
    SELECT
        p_round_id,
        advertiser.member_id as advertiser_member_id,
        publisher.member_id as publisher_member_id,
        aa.id as ai_advertisement_id,
        revenue_per_post
    FROM orders advertiser
    CROSS JOIN orders publisher
    JOIN ai_advertisements aa ON aa.round_id = p_round_id
        AND aa.advertiser_member_id = advertiser.member_id
    WHERE advertiser.round_id = p_round_id
        AND publisher.round_id = p_round_id
        AND advertiser.member_id != publisher.member_id  -- 자기 광고는 게시 안함
        AND advertiser.status = 'COMPLETED'
        AND publisher.status = 'COMPLETED';
END;
$$ LANGUAGE plpgsql;