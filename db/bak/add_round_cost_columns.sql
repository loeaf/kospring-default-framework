-- 라운드 테이블에 비용 관련 컬럼 추가

-- 템플릿 비용 컬럼 추가
ALTER TABLE rounds ADD COLUMN IF NOT EXISTS template_cost DECIMAL(12,2) DEFAULT 0;

-- AI 기반 광고 생성비 컬럼 추가
ALTER TABLE rounds ADD COLUMN IF NOT EXISTS ai_generation_cost DECIMAL(12,2) DEFAULT 0;

-- 타게팅 게시비 컬럼 추가
ALTER TABLE rounds ADD COLUMN IF NOT EXISTS targeting_posting_cost DECIMAL(12,2) DEFAULT 0;

-- 서버 임대비 컬럼 추가
ALTER TABLE rounds ADD COLUMN IF NOT EXISTS server_rental_cost DECIMAL(12,2) DEFAULT 0;

-- 기타 비용 컬럼 추가
ALTER TABLE rounds ADD COLUMN IF NOT EXISTS other_costs DECIMAL(12,2) DEFAULT 0;

-- 컬럼에 대한 코멘트 추가
COMMENT ON COLUMN rounds.template_cost IS '템플릿 비용';
COMMENT ON COLUMN rounds.ai_generation_cost IS 'AI 기반 광고 생성비';
COMMENT ON COLUMN rounds.targeting_posting_cost IS '타게팅 게시비';
COMMENT ON COLUMN rounds.server_rental_cost IS '서버 임대비';
COMMENT ON COLUMN rounds.other_costs IS '기타 비용';

-- 인덱스 추가 (필요한 경우)
-- CREATE INDEX IF NOT EXISTS idx_rounds_template_cost ON rounds(template_cost);
-- CREATE INDEX IF NOT EXISTS idx_rounds_total_cost ON rounds((template_cost + ai_generation_cost + targeting_posting_cost + server_rental_cost + other_costs));