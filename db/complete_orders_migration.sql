-- orders 테이블을 round_id에서 ad_task_id로 완전 마이그레이션하는 통합 스크립트

BEGIN;

-- 1. orders 테이블 구조 변경
DO $$
BEGIN
    RAISE NOTICE 'Step 1: orders 테이블 구조 변경 시작...';
END $$;

-- 기존 round_id 관련 제약조건 제거
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_round_id_member_id_key;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_round_id_fkey;

-- 기존 인덱스 제거
DROP INDEX IF EXISTS idx_orders_round_id;

-- round_id 컬럼 삭제
ALTER TABLE orders DROP COLUMN round_id;

-- ad_task_id 컬럼 추가
ALTER TABLE orders ADD COLUMN ad_task_id BIGINT NOT NULL REFERENCES ad_tasks(id) ON DELETE CASCADE;

-- 새로운 제약조건 추가 (한 ad_task당 하나의 주문만 가능)
ALTER TABLE orders ADD CONSTRAINT unique_orders_ad_task_id UNIQUE (ad_task_id);

-- 새로운 인덱스 생성
CREATE INDEX idx_orders_ad_task_id ON orders(ad_task_id);

-- 2. create_advertisement_assignments 함수 업데이트
DO $$
BEGIN
    RAISE NOTICE 'Step 2: create_advertisement_assignments 함수 업데이트...';
END $$;

-- 기존 함수 삭제
DROP FUNCTION IF EXISTS create_advertisement_assignments(BIGINT);

-- 새로운 함수 생성 (ad_task_id 기반)
CREATE OR REPLACE FUNCTION create_advertisement_assignments(p_round_id BIGINT)
RETURNS VOID AS $$
DECLARE
    participant_count INTEGER;
    revenue_per_post DECIMAL(12,2);
    round_order_amount DECIMAL(12,2);
BEGIN
    -- 라운드 참여자 수와 발주 금액 조회 (ad_tasks와 orders를 조인)
    SELECT COUNT(*), r.order_amount
    INTO participant_count, round_order_amount
    FROM orders o
    JOIN ad_tasks at ON o.ad_task_id = at.id
    JOIN rounds r ON at.round_id = r.id
    WHERE at.round_id = p_round_id AND o.status = 'COMPLETED'
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
        advertiser_orders.member_id as advertiser_member_id,
        publisher_orders.member_id as publisher_member_id,
        aa.id as ai_advertisement_id,
        revenue_per_post
    FROM orders advertiser_orders
    JOIN ad_tasks advertiser_ad_tasks ON advertiser_orders.ad_task_id = advertiser_ad_tasks.id
    CROSS JOIN orders publisher_orders
    JOIN ad_tasks publisher_ad_tasks ON publisher_orders.ad_task_id = publisher_ad_tasks.id
    JOIN ai_advertisements aa ON aa.round_id = p_round_id
        AND aa.advertiser_member_id = advertiser_orders.member_id
    WHERE advertiser_ad_tasks.round_id = p_round_id
        AND publisher_ad_tasks.round_id = p_round_id
        AND advertiser_orders.member_id != publisher_orders.member_id  -- 자기 광고는 게시 안함
        AND advertiser_orders.status = 'COMPLETED'
        AND publisher_orders.status = 'COMPLETED';
END;
$$ LANGUAGE plpgsql;

-- 3. 편의를 위한 뷰 생성
DO $$
BEGIN
    RAISE NOTICE 'Step 3: 편의를 위한 뷰 생성...';
END $$;

-- orders에서 라운드 정보를 쉽게 조회할 수 있는 뷰
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
    o.status as order_status,
    o.submitted_at,
    o.created_at as order_created_at,
    o.updated_at as order_updated_at,
    -- ad_tasks에서 라운드 정보 조회
    at.round_id,
    at.task_status as ad_task_status,
    at.web_url as ad_web_url,
    at.ad_type,
    at.created_at as ad_task_created_at,
    at.completed_at as ad_task_completed_at,
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
FROM orders o
LEFT JOIN ad_tasks at ON o.ad_task_id = at.id
LEFT JOIN rounds r ON at.round_id = r.id
LEFT JOIN members m ON o.member_id = m.id;

-- 4. 마이그레이션 완료 확인
DO $$
DECLARE
    orders_count INTEGER;
    ad_task_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO orders_count FROM orders;
    SELECT COUNT(*) INTO ad_task_count FROM ad_tasks;
    
    RAISE NOTICE 'Step 4: 마이그레이션 완료 확인...';
    RAISE NOTICE '총 주문 수: %', orders_count;
    RAISE NOTICE '총 광고 작업 수: %', ad_task_count;
    
    RAISE NOTICE '✅ orders 테이블 마이그레이션 완료!';
    RAISE NOTICE '   - round_id 제거됨';
    RAISE NOTICE '   - ad_task_id 추가됨';
    RAISE NOTICE '   - UNIQUE(ad_task_id) 제약조건 추가됨';
    RAISE NOTICE '   - create_advertisement_assignments 함수 업데이트됨';
    RAISE NOTICE '   - orders_with_round_info 뷰 생성됨';
END $$;

COMMIT;

-- 사용 예시:
-- 
-- 1. 주문과 라운드 정보 함께 조회:
-- SELECT * FROM orders_with_round_info WHERE member_id = 15;
--
-- 2. 특정 라운드의 모든 주문 조회:
-- SELECT * FROM orders_with_round_info WHERE round_id = 16;
--
-- 3. 완료된 광고가 있는 주문만 조회:
-- SELECT * FROM orders_with_round_info WHERE ad_task_status = 'COMPLETED';