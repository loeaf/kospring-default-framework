-- members 테이블에 사업 관련 필드들 추가
ALTER TABLE members 
ADD COLUMN business_field VARCHAR(255) NOT NULL DEFAULT '',
ADD COLUMN product_description VARCHAR(1000) NOT NULL DEFAULT '',
ADD COLUMN company_description VARCHAR(1000) NOT NULL DEFAULT '';

-- 컬럼 코멘트 추가
COMMENT ON COLUMN members.business_field IS '사업분야';
COMMENT ON COLUMN members.product_description IS '상품/제품 소개';
COMMENT ON COLUMN members.company_description IS '회사 소개';

-- 기본값 설정 후 NOT NULL 제약조건 유지를 위해 기존 레코드들에 기본값 업데이트
UPDATE members 
SET business_field = '미분류', 
    product_description = '상품 설명 미입력', 
    company_description = '회사 소개 미입력'
WHERE business_field = '' OR product_description = '' OR company_description = '';