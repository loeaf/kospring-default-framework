-- 임대권 만료 테스트를 위한 SQL

-- 1. 현재 활성 임대권 조회
SELECT id, member_id, purchase_date, expiry_date, status 
FROM rental_rights 
WHERE status = 'ACTIVE';

-- 2. 특정 회원의 임대권을 만료된 상태로 변경 (member_id = 1 예시)
UPDATE rental_rights 
SET expiry_date = '2023-12-31',  -- 과거 날짜로 설정
    status = 'EXPIRED'
WHERE member_id = 5 AND status = 'ACTIVE';

-- 3. 회원 테이블의 임대권 상태도 업데이트
UPDATE members 
SET rental_status = 'EXPIRED',
    current_rental_expiry = '2023-12-31'
WHERE id = 1;

-- 4. 여러 회원의 임대권을 만료시키고 싶다면
-- UPDATE rental_rights 
-- SET expiry_date = CURRENT_DATE - INTERVAL '1 day',
--     status = 'EXPIRED'
-- WHERE member_id IN (1, 2, 3) AND status = 'ACTIVE';

-- 5. 특정 일수 전에 만료되도록 설정 (예: 30일 전)
-- UPDATE rental_rights 
-- SET expiry_date = CURRENT_DATE - INTERVAL '30 days',
--     status = 'EXPIRED'
-- WHERE member_id = 1 AND status = 'ACTIVE';

-- 6. 만료 예정 임대권 만들기 (예: 7일 후 만료)
-- UPDATE rental_rights 
-- SET expiry_date = CURRENT_DATE + INTERVAL '7 days'
-- WHERE member_id = 2 AND status = 'ACTIVE';

-- 7. 변경 결과 확인
SELECT 
    r.id,
    r.member_id,
    m.email,
    r.purchase_date,
    r.expiry_date,
    r.status as rental_status,
    m.rental_status as member_rental_status,
    CASE 
        WHEN r.expiry_date < CURRENT_DATE THEN 'EXPIRED'
        WHEN r.expiry_date <= CURRENT_DATE + INTERVAL '30 days' THEN 'EXPIRING_SOON'
        ELSE 'ACTIVE'
    END as calculated_status
FROM rental_rights r
JOIN members m ON r.member_id = m.id
ORDER BY r.expiry_date;