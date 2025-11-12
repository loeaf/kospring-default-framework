-- create_advertisement_assignments 함수를 ad_task_id 기반으로 수정

-- 기존 함수 삭제
DROP FUNCTION IF EXISTS create_advertisement_assignments(BIGINT);

-- ad_task_id 기반으로 새로운 함수 생성
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

    -- 게시당 수익 계산 (발주금액 ÷ (참여자수-1)) - 절삭 처리로 손실 방지
    revenue_per_post := FLOOR(round_order_amount / (participant_count - 1));

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