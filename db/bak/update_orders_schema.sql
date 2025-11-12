-- orders 테이블 스키마를 ad_task_id 기반으로 완전히 업데이트하는 스크립트
-- 이 스크립트는 새로운 orders 테이블 구조를 정의합니다.

-- 기존 orders 테이블이 있다면 백업 후 삭제하고 재생성
-- DROP TABLE IF EXISTS orders CASCADE;

-- 새로운 orders 테이블 생성 (ad_task_id 기반)
CREATE TABLE IF NOT EXISTS orders_new (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(20) UNIQUE COMMENT '주문번호 (ORD-2024-001)',
    ad_task_id BIGINT NOT NULL REFERENCES ad_tasks(id) ON DELETE CASCADE COMMENT '선택한 광고 템플릿 ID',
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

    -- 중요: ad_task_id는 하나의 주문에만 연결될 수 있음 (1:1 관계)
    UNIQUE(ad_task_id)
);

-- 인덱스 생성
CREATE INDEX idx_orders_new_ad_task_id ON orders_new(ad_task_id);
CREATE INDEX idx_orders_new_member_id ON orders_new(member_id);
CREATE INDEX idx_orders_new_status ON orders_new(status);
CREATE INDEX idx_orders_new_submitted_at ON orders_new(submitted_at);
CREATE INDEX idx_orders_new_order_number ON orders_new(order_number);

-- updated_at 자동 업데이트 트리거 추가
CREATE TRIGGER update_orders_new_updated_at
    BEFORE UPDATE ON orders_new
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 기존 orders 테이블이 있다면 데이터 마이그레이션
-- INSERT INTO orders_new (...)
-- SELECT ... FROM orders WHERE ...;

-- 마이그레이션 완료 후 테이블 교체
-- DROP TABLE orders CASCADE;
-- ALTER TABLE orders_new RENAME TO orders;

-- 마이그레이션 검증을 위한 뷰 생성
CREATE OR REPLACE VIEW orders_with_round_info AS
SELECT 
    o.id,
    o.order_number,
    o.ad_task_id,
    o.member_id,
    o.product_name,
    o.quantity,
    o.requirements,
    o.deadline,
    o.status,
    o.submitted_at,
    o.created_at,
    o.updated_at,
    -- ad_tasks에서 라운드 정보 조회
    at.round_id,
    at.task_status as ad_task_status,
    at.web_url as ad_web_url,
    at.ad_type,
    -- rounds에서 라운드 상세 정보 조회
    r.title as round_title,
    r.category as round_category,
    r.order_amount as round_order_amount,
    r.start_date as round_start_date,
    r.end_date as round_end_date,
    r.status as round_status,
    -- members에서 회원 정보 조회
    m.company_name,
    m.email as member_email
FROM orders_new o
LEFT JOIN ad_tasks at ON o.ad_task_id = at.id
LEFT JOIN rounds r ON at.round_id = r.id
LEFT JOIN members m ON o.member_id = m.id;

-- 사용 예시 쿼리들:

-- 1. 주문과 함께 라운드 정보 조회
-- SELECT * FROM orders_with_round_info WHERE member_id = 15;

-- 2. 특정 라운드의 모든 주문 조회
-- SELECT * FROM orders_with_round_info WHERE round_id = 16;

-- 3. 완료된 광고가 있는 주문만 조회
-- SELECT * FROM orders_with_round_info WHERE ad_task_status = 'COMPLETED';