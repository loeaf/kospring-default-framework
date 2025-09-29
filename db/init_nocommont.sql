-- ==========================================
-- PostgreSQL 데이터베이스 스키마 (실행 가능한 버전)
-- ==========================================

-- 1. 회원 테이블
CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    business_registration_number VARCHAR(20) NOT NULL UNIQUE,
    contact_number VARCHAR(20) NOT NULL,
    business_registration_file VARCHAR(500) NOT NULL,
    telecommunication_sales_file VARCHAR(500) NOT NULL,
    is_premium BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. 라운드 테이블
CREATE TABLE rounds (
    id BIGSERIAL PRIMARY KEY,
    round_number VARCHAR(20) UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    order_amount DECIMAL(12,2) NOT NULL,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CLOSED', 'PENDING')),
    max_participants INTEGER,
    created_by BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. 발주(참여 신청) 테이블
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(20) UNIQUE,
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    requirements TEXT,
    deadline DATE,
    start_date DATE,
    completion_date DATE,
    failure_date DATE,
    progress_rate INTEGER DEFAULT 0 CHECK (progress_rate >= 0 AND progress_rate <= 100),
    failure_reason TEXT,
    status VARCHAR(30) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAYMENT_WAITING', 'PAYMENT_CONFIRMED', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'CANCELLED')),
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    reviewed_by BIGINT REFERENCES members(id),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(round_id, member_id)
);

-- 4. 시스템 설정 테이블
CREATE TABLE system_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. 발주 입금 테이블
CREATE TABLE order_payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    application_number VARCHAR(20) NOT NULL UNIQUE,
    payment_amount DECIMAL(12,2) NOT NULL,
    depositor_name VARCHAR(100),
    payment_status VARCHAR(20) DEFAULT 'WAITING' CHECK (payment_status IN ('WAITING', 'CONFIRMED', 'FAILED')),
    payment_confirmed_at TIMESTAMP WITH TIME ZONE,
    payment_confirmed_by BIGINT REFERENCES members(id),
    bank_account_number VARCHAR(50) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. AI 생성 광고 컨텐츠 테이블
CREATE TABLE ai_advertisements (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    advertiser_member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    advertisement_cost DECIMAL(12,2) NOT NULL,
    html_file_path VARCHAR(500),
    content_status VARCHAR(20) DEFAULT 'CONTENT_NEEDED' CHECK (
        content_status IN ('CONTENT_NEEDED', 'CONTENT_COMPLETED', 'WORK_COMPLETED')
    ),
    content_written_at TIMESTAMP WITH TIME ZONE,
    work_completed_at TIMESTAMP WITH TIME ZONE,
    deadline DATE NOT NULL,
    created_by BIGINT NOT NULL REFERENCES members(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(round_id, advertiser_member_id)
);

-- 7. 광고 게시 할당 테이블
CREATE TABLE advertisement_assignments (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    advertiser_member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    publisher_member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    ai_advertisement_id BIGINT NOT NULL REFERENCES ai_advertisements(id) ON DELETE CASCADE,
    revenue_per_post DECIMAL(12,2) NOT NULL,
    assignment_status VARCHAR(20) DEFAULT 'ASSIGNED' CHECK (
        assignment_status IN ('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'FAILED')
    ),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(round_id, advertiser_member_id, publisher_member_id),
    CHECK (advertiser_member_id != publisher_member_id)
);

-- 8. 광고 게시 테이블
CREATE TABLE advertisement_posts (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES advertisement_assignments(id) ON DELETE CASCADE,
    content TEXT,
    ctr_rate DECIMAL(5,2),
    final_revenue DECIMAL(12,2),
    post_status VARCHAR(20) DEFAULT 'PENDING' CHECK (
        post_status IN ('PENDING', 'APPROVED', 'REJECTED', 'PUBLISHED', 'FAILED')
    ),
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT,
    rejection_reason TEXT,
    reviewed_by BIGINT REFERENCES members(id),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. 수익 거래 테이블
CREATE TABLE revenue_transactions (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    transaction_type VARCHAR(10) NOT NULL CHECK (transaction_type IN ('INCOME', 'EXPENSE')),

    -- 매출 관련
    assignment_id BIGINT REFERENCES advertisement_assignments(id) ON DELETE SET NULL,
    round_number VARCHAR(20),
    round_category VARCHAR(100),
    ctr_rate DECIMAL(5,2),

    -- 매입 관련
    order_id BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    order_number VARCHAR(20),
    order_status VARCHAR(20),

    -- 공통 필드
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    transaction_date DATE NOT NULL,
    tax_invoice_issued BOOLEAN DEFAULT FALSE,
    tax_invoice_number VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. 정산 계좌 정보 테이블
CREATE TABLE settlement_accounts (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_holder VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(member_id, account_number)
);

-- 11. 정산 배치 테이블
CREATE TABLE settlement_batches (
    id BIGSERIAL PRIMARY KEY,
    round_id BIGINT NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    settlement_date DATE NOT NULL,
    total_participants INTEGER NOT NULL,
    total_settlement_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    processed_at TIMESTAMP WITH TIME ZONE,
    processed_by BIGINT REFERENCES members(id),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 12. 개별 정산 내역 테이블
CREATE TABLE member_settlements (
    id BIGSERIAL PRIMARY KEY,
    settlement_batch_id BIGINT NOT NULL REFERENCES settlement_batches(id) ON DELETE CASCADE,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    settlement_account_id BIGINT NOT NULL REFERENCES settlement_accounts(id),

    total_income DECIMAL(12,2) DEFAULT 0,
    total_expense DECIMAL(12,2) DEFAULT 0,
    net_amount DECIMAL(12,2) NOT NULL,

    settlement_status VARCHAR(20) DEFAULT 'PENDING' CHECK (
        settlement_status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED')
    ),
    settlement_date DATE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT,

    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_holder VARCHAR(100) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(settlement_batch_id, member_id)
);

-- 13. 회원별 정산 요약 테이블
CREATE TABLE member_settlement_summary (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE UNIQUE,

    total_accumulated_amount DECIMAL(15,2) DEFAULT 0,
    last_settlement_date DATE,
    next_settlement_date DATE,
    pending_settlement_amount DECIMAL(12,2) DEFAULT 0,

    default_settlement_account_id BIGINT REFERENCES settlement_accounts(id),

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
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

-- advertisement_assignments 테이블
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

-- 라운드 광고 할당 생성 함수
CREATE OR REPLACE FUNCTION create_advertisement_assignments(p_round_id BIGINT)
RETURNS VOID AS $$
DECLARE
    participant_count INTEGER;
    revenue_per_post DECIMAL(12,2);
    round_order_amount DECIMAL(12,2);
BEGIN
    SELECT COUNT(*), r.order_amount
    INTO participant_count, round_order_amount
    FROM orders o
    JOIN rounds r ON o.round_id = r.id
    WHERE o.round_id = p_round_id AND o.status = 'COMPLETED'
    GROUP BY r.order_amount;

    revenue_per_post := round_order_amount / (participant_count - 1);

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
        AND advertiser.member_id != publisher.member_id
        AND advertiser.status = 'COMPLETED'
        AND publisher.status = 'COMPLETED';
END;
$$ LANGUAGE plpgsql;

-- ==========================================
-- 트리거 생성
-- ==========================================

CREATE TRIGGER update_members_updated_at
    BEFORE UPDATE ON members
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_rounds_updated_at
    BEFORE UPDATE ON rounds
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_system_settings_updated_at
    BEFORE UPDATE ON system_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_order_payments_updated_at
    BEFORE UPDATE ON order_payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ai_advertisements_updated_at
    BEFORE UPDATE ON ai_advertisements
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_advertisement_assignments_updated_at
    BEFORE UPDATE ON advertisement_assignments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_advertisement_posts_updated_at
    BEFORE UPDATE ON advertisement_posts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_revenue_transactions_updated_at
    BEFORE UPDATE ON revenue_transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_settlement_accounts_updated_at
    BEFORE UPDATE ON settlement_accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_settlement_batches_updated_at
    BEFORE UPDATE ON settlement_batches
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_member_settlements_updated_at
    BEFORE UPDATE ON member_settlements
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_member_settlement_summary_updated_at
    BEFORE UPDATE ON member_settlement_summary
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ==========================================
-- 기본 데이터 삽입
-- ==========================================

INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('bank_account_number', '072-12-55-2-4215', '입금 계좌번호'),
('bank_name', 'NH 농협 (주) 도시와 농촌', '은행명'),
('bank_account_holder', '(주) 도시와 농촌', '예금주명');