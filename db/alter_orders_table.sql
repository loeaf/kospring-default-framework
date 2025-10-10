-- orders 테이블에서 round_id 제거하고 ad_task_id 추가

BEGIN;

-- 1. 기존 round_id 관련 제약조건 제거
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_round_id_member_id_key;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_round_id_fkey;

-- 2. round_id 컬럼 삭제
ALTER TABLE orders DROP COLUMN round_id;

-- 3. ad_task_id 컬럼 추가
ALTER TABLE orders ADD COLUMN ad_task_id BIGINT NOT NULL REFERENCES ad_tasks(id) ON DELETE CASCADE;

-- 4. 새로운 제약조건 추가 (한 ad_task당 하나의 주문만 가능)
ALTER TABLE orders ADD CONSTRAINT unique_orders_ad_task_id UNIQUE (ad_task_id);

-- 5. 인덱스 생성
CREATE INDEX idx_orders_ad_task_id ON orders(ad_task_id);

COMMIT;