-- ad_tasks와 ai_advertisements 테이블 간의 관계 및 동기화 설정

-- 1. ai_advertisements 테이블에 ad_task_id 참조 추가 (선택사항)
-- ALTER TABLE ai_advertisements ADD COLUMN ad_task_id BIGINT REFERENCES ad_tasks(id);

-- 2. 두 테이블을 연결하는 뷰 생성
CREATE OR REPLACE VIEW advertisement_overview AS
SELECT 
    -- ad_tasks 정보
    at.id as ad_task_id,
    at.round_id,
    at.member_id,
    at.task_status,
    at.ad_content as ad_html_content,
    at.html_file_path as ad_html_file_path,
    at.web_url as ad_web_url,
    at.ad_type,
    at.ad_index,
    at.created_at as ad_task_created_at,
    at.completed_at as ad_task_completed_at,
    
    -- ai_advertisements 정보 (있다면)
    aa.id as ai_advertisement_id,
    aa.title as advertisement_title,
    aa.advertisement_cost,
    aa.html_file_path as ai_html_file_path,
    aa.content_status,
    aa.content_written_at,
    aa.work_completed_at,
    aa.deadline as advertisement_deadline,
    aa.created_by as advertisement_created_by,
    aa.created_at as ai_advertisement_created_at,
    
    -- 라운드 정보
    r.title as round_title,
    r.category as round_category,
    r.order_amount,
    r.start_date as round_start_date,
    r.end_date as round_end_date,
    r.status as round_status,
    
    -- 회원 정보
    m.company_name,
    m.email as member_email
    
FROM ad_tasks at
LEFT JOIN ai_advertisements aa ON at.round_id = aa.round_id AND at.member_id = aa.advertiser_member_id
LEFT JOIN rounds r ON at.round_id = r.id
LEFT JOIN members m ON at.member_id = m.id;

-- 3. ad_tasks에서 ai_advertisements로 데이터 동기화하는 함수
CREATE OR REPLACE FUNCTION sync_ad_task_to_ai_advertisement(p_ad_task_id BIGINT)
RETURNS BIGINT AS $$
DECLARE
    task_record RECORD;
    ai_ad_id BIGINT;
BEGIN
    -- ad_task 정보 조회
    SELECT 
        at.*,
        r.order_amount,
        m.company_name
    INTO task_record
    FROM ad_tasks at
    JOIN rounds r ON at.round_id = r.id
    JOIN members m ON at.member_id = m.id
    WHERE at.id = p_ad_task_id;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'ad_task with id % not found', p_ad_task_id;
    END IF;
    
    -- 이미 ai_advertisements에 있는지 확인
    SELECT id INTO ai_ad_id
    FROM ai_advertisements
    WHERE round_id = task_record.round_id 
    AND advertiser_member_id = task_record.member_id;
    
    IF ai_ad_id IS NULL THEN
        -- 새로 생성
        INSERT INTO ai_advertisements (
            round_id,
            advertiser_member_id,
            title,
            advertisement_cost,
            html_file_path,
            content_status,
            content_written_at,
            work_completed_at,
            deadline,
            created_by
        ) VALUES (
            task_record.round_id,
            task_record.member_id,
            COALESCE(task_record.company_name || ' 광고', 'AI 생성 광고'),
            task_record.order_amount,
            task_record.html_file_path,
            CASE 
                WHEN task_record.task_status = 'COMPLETED' THEN 'WORK_COMPLETED'
                WHEN task_record.task_status = 'PROCESSING' THEN 'CONTENT_NEEDED'
                ELSE 'CONTENT_NEEDED'
            END,
            CASE WHEN task_record.task_status = 'COMPLETED' THEN task_record.completed_at END,
            CASE WHEN task_record.task_status = 'COMPLETED' THEN task_record.completed_at END,
            CURRENT_DATE + INTERVAL '30 days', -- 기본 30일 후 마감
            task_record.member_id -- 생성자를 해당 회원으로 설정
        )
        RETURNING id INTO ai_ad_id;
        
        RAISE NOTICE 'Created new ai_advertisement with id: %', ai_ad_id;
    ELSE
        -- 기존 레코드 업데이트
        UPDATE ai_advertisements SET
            html_file_path = task_record.html_file_path,
            content_status = CASE 
                WHEN task_record.task_status = 'COMPLETED' THEN 'WORK_COMPLETED'
                WHEN task_record.task_status = 'PROCESSING' THEN 'CONTENT_NEEDED'
                ELSE content_status
            END,
            content_written_at = CASE WHEN task_record.task_status = 'COMPLETED' THEN task_record.completed_at ELSE content_written_at END,
            work_completed_at = CASE WHEN task_record.task_status = 'COMPLETED' THEN task_record.completed_at ELSE work_completed_at END,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ai_ad_id;
        
        RAISE NOTICE 'Updated ai_advertisement with id: %', ai_ad_id;
    END IF;
    
    RETURN ai_ad_id;
END;
$$ LANGUAGE plpgsql;

-- 4. ad_tasks 변경 시 자동으로 ai_advertisements 동기화하는 트리거
CREATE OR REPLACE FUNCTION trigger_sync_ad_task_to_ai_advertisement()
RETURNS TRIGGER AS $$
BEGIN
    -- INSERT 또는 UPDATE 시에만 동기화
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        PERFORM sync_ad_task_to_ai_advertisement(NEW.id);
        RETURN NEW;
    END IF;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 트리거 생성 (ad_tasks 테이블의 변경 시 자동 동기화)
DROP TRIGGER IF EXISTS trigger_ad_task_sync ON ad_tasks;
CREATE TRIGGER trigger_ad_task_sync
    AFTER INSERT OR UPDATE ON ad_tasks
    FOR EACH ROW
    EXECUTE FUNCTION trigger_sync_ad_task_to_ai_advertisement();

-- 5. 기존 ad_tasks 데이터를 ai_advertisements로 일괄 동기화
CREATE OR REPLACE FUNCTION sync_all_ad_tasks_to_ai_advertisements()
RETURNS INTEGER AS $$
DECLARE
    task_record RECORD;
    synced_count INTEGER := 0;
BEGIN
    FOR task_record IN 
        SELECT DISTINCT id 
        FROM ad_tasks 
        WHERE task_status = 'COMPLETED'
    LOOP
        BEGIN
            PERFORM sync_ad_task_to_ai_advertisement(task_record.id);
            synced_count := synced_count + 1;
        EXCEPTION WHEN OTHERS THEN
            RAISE WARNING 'Failed to sync ad_task %, error: %', task_record.id, SQLERRM;
        END;
    END LOOP;
    
    RAISE NOTICE 'Synced % ad_tasks to ai_advertisements', synced_count;
    RETURN synced_count;
END;
$$ LANGUAGE plpgsql;

-- 사용 예시:
-- 1. 모든 기존 ad_tasks를 ai_advertisements로 동기화
-- SELECT sync_all_ad_tasks_to_ai_advertisements();

-- 2. 특정 ad_task를 ai_advertisements로 동기화
-- SELECT sync_ad_task_to_ai_advertisement(47);

-- 3. 통합 뷰로 모든 광고 정보 조회
-- SELECT * FROM advertisement_overview WHERE round_id = 16;

-- 4. 결제 완료된 광고만 조회
-- SELECT ao.* 
-- FROM advertisement_overview ao
-- JOIN orders o ON ao.ad_task_id = o.ad_task_id
-- JOIN order_payments op ON o.id = op.order_id
-- WHERE op.payment_status = 'CONFIRMED';