-- ad_tasks 테이블에 web_url 컬럼 추가
-- 정적 웹서비스 URL을 저장하여 iframe으로 서비스할 수 있도록 함

ALTER TABLE ad_tasks 
ADD COLUMN IF NOT EXISTS web_url VARCHAR(1000);

-- 컬럼에 대한 코멘트 추가
COMMENT ON COLUMN ad_tasks.web_url IS '생성된 광고의 웹 서비스 URL (iframe 서비스용)';

-- 기존 데이터에 대한 web_url 업데이트 (html_file_path가 있는 경우)
-- 예시: generated_ads/round_20/member_15_1759808786.html -> /round_20/member_15_1759808786.html
UPDATE ad_tasks 
SET web_url = CASE 
    WHEN html_file_path IS NOT NULL AND html_file_path != '' THEN 
        '/' || REPLACE(html_file_path, 'generated_ads/', '')
    ELSE NULL 
END
WHERE html_file_path IS NOT NULL AND web_url IS NULL;

-- 인덱스 추가 (URL 기반 검색을 위해)
CREATE INDEX IF NOT EXISTS idx_ad_tasks_web_url ON ad_tasks(web_url);