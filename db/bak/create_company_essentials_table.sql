-- 회사 에센셜 정보 관리 테이블
CREATE TABLE IF NOT EXISTS company_essentials (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    business_registration_number VARCHAR(20) NOT NULL,
    
    -- 검색 키워드
    search_keywords TEXT[], -- 검색에 사용할 키워드들
    
    -- XML 파일 정보
    xml_file_path VARCHAR(500) NOT NULL, -- XML 파일 저장 경로
    xml_file_size BIGINT DEFAULT 0, -- 파일 크기 (bytes)
    
    -- 수집된 정보 요약
    collected_data_summary TEXT, -- 수집된 정보 요약 (JSON 형태)
    news_count INTEGER DEFAULT 0, -- 수집된 뉴스 개수
    product_info_count INTEGER DEFAULT 0, -- 수집된 상품 정보 개수
    company_info_updated BOOLEAN DEFAULT FALSE, -- 회사 정보 업데이트 여부
    
    -- 검색 및 생성 상태
    collection_status VARCHAR(20) DEFAULT 'PENDING' CHECK (
        collection_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'OUTDATED')
    ),
    
    -- 마지막 업데이트 정보
    last_search_date TIMESTAMP WITH TIME ZONE, -- 마지막 검색 실행 시간
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    next_update_due TIMESTAMP WITH TIME ZONE, -- 다음 업데이트 예정 시간
    
    -- 업데이트 주기 설정
    update_frequency_days INTEGER DEFAULT 7, -- 업데이트 주기 (일)
    auto_update_enabled BOOLEAN DEFAULT TRUE, -- 자동 업데이트 활성화 여부
    
    -- 오류 정보
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    
    -- 메타데이터
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_company_essentials_member_id ON company_essentials(member_id);
CREATE INDEX IF NOT EXISTS idx_company_essentials_status ON company_essentials(collection_status);
CREATE INDEX IF NOT EXISTS idx_company_essentials_update_due ON company_essentials(next_update_due);
CREATE INDEX IF NOT EXISTS idx_company_essentials_company_name ON company_essentials(company_name);
CREATE INDEX IF NOT EXISTS idx_company_essentials_last_updated ON company_essentials(last_updated_at);

-- 에센셜 정보 수집 로그 테이블
CREATE TABLE IF NOT EXISTS company_essential_logs (
    id BIGSERIAL PRIMARY KEY,
    essential_id BIGINT NOT NULL REFERENCES company_essentials(id) ON DELETE CASCADE,
    
    -- 수집 정보
    search_query TEXT NOT NULL, -- 사용된 검색 쿼리
    search_results_count INTEGER DEFAULT 0, -- 검색 결과 개수
    processed_items_count INTEGER DEFAULT 0, -- 처리된 항목 개수
    
    -- 실행 정보
    execution_status VARCHAR(20) DEFAULT 'STARTED' CHECK (
        execution_status IN ('STARTED', 'SEARCHING', 'PROCESSING', 'COMPLETED', 'FAILED')
    ),
    execution_duration_seconds INTEGER, -- 실행 시간 (초)
    
    -- 오류 정보
    error_details TEXT,
    
    -- 생성된 파일 정보
    generated_xml_path VARCHAR(500),
    xml_generation_successful BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_essential_logs_essential_id ON company_essential_logs(essential_id);
CREATE INDEX IF NOT EXISTS idx_essential_logs_status ON company_essential_logs(execution_status);
CREATE INDEX IF NOT EXISTS idx_essential_logs_created_at ON company_essential_logs(created_at);

-- 회사별 키워드 기본값 설정 함수
CREATE OR REPLACE FUNCTION set_default_search_keywords(
    p_member_id BIGINT,
    p_company_name VARCHAR(255),
    p_business_number VARCHAR(20)
) RETURNS TEXT[] AS $$
DECLARE
    default_keywords TEXT[];
BEGIN
    -- 기본 키워드 생성
    default_keywords := ARRAY[
        p_company_name,
        p_company_name || ' 회사',
        p_company_name || ' 제품',
        p_company_name || ' 서비스',
        p_company_name || ' 뉴스',
        p_company_name || ' 보도자료',
        '사업자등록번호 ' || p_business_number
    ];
    
    RETURN default_keywords;
END;
$$ LANGUAGE plpgsql;

-- 다음 업데이트 시간 계산 함수
CREATE OR REPLACE FUNCTION calculate_next_update_time(update_frequency_days INTEGER)
RETURNS TIMESTAMP WITH TIME ZONE AS $$
BEGIN
    RETURN CURRENT_TIMESTAMP + (update_frequency_days || ' days')::INTERVAL;
END;
$$ LANGUAGE plpgsql;

-- 업데이트 필요한 에센셜 조회 뷰
CREATE OR REPLACE VIEW v_essentials_due_for_update AS
SELECT 
    ce.*,
    m.company_name as member_company_name,
    m.email as member_email
FROM company_essentials ce
JOIN members m ON ce.member_id = m.id
WHERE 
    ce.auto_update_enabled = true
    AND (
        ce.next_update_due IS NULL 
        OR ce.next_update_due <= CURRENT_TIMESTAMP
        OR ce.collection_status = 'FAILED'
    )
    AND ce.retry_count < 5
ORDER BY 
    CASE 
        WHEN ce.collection_status = 'FAILED' THEN 1
        WHEN ce.next_update_due IS NULL THEN 2
        ELSE 3
    END,
    ce.next_update_due ASC NULLS FIRST;

-- 테이블 코멘트
COMMENT ON TABLE company_essentials IS '회사별 에센셜 정보 및 XML 파일 관리 테이블';
COMMENT ON COLUMN company_essentials.search_keywords IS '웹 검색에 사용할 키워드 배열';
COMMENT ON COLUMN company_essentials.xml_file_path IS 'Claude가 읽을 수 있는 XML 형태의 에센셜 정보 파일 경로';
COMMENT ON COLUMN company_essentials.collected_data_summary IS '수집된 정보의 요약 (JSON 형태)';
COMMENT ON COLUMN company_essentials.update_frequency_days IS '자동 업데이트 주기 (일 단위)';

COMMENT ON TABLE company_essential_logs IS '에센셜 정보 수집 실행 로그';
COMMENT ON VIEW v_essentials_due_for_update IS '업데이트가 필요한 에센셜 정보 목록';