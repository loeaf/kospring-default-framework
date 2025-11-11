-- 임대권 테이블에 게시비 단가 선택 컬럼 추가
ALTER TABLE rental_rights 
ADD COLUMN pricing_preference VARCHAR(20) NULL 
COMMENT '게시비 단가 선택 기준: highest(최고금액), lowest(최저금액), undecided(모르겠음)';

-- 기존 데이터에 대한 기본값 설정 (선택사항)
-- UPDATE rental_rights SET pricing_preference = 'undecided' WHERE pricing_preference IS NULL;