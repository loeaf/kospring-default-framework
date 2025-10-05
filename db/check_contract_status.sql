-- test@cnc.com (id:5) 회원의 계약 상태 확인

-- 1. 회원 정보 확인
SELECT id, email, rental_status, current_rental_expiry 
FROM members 
WHERE email = 'test@cnc.com';

-- 2. 해당 회원의 서비스 계약 확인
SELECT 
    sc.id,
    sc.member_id,
    sc.rental_contract_agreed,
    sc.service_contract_agreed,
    sc.marketing_agreed,
    sc.is_active,
    sc.contract_date,
    sc.created_at
FROM service_contracts sc
JOIN members m ON sc.member_id = m.id
WHERE m.email = 'test@cnc.com';

-- 3. 해당 회원의 임대권 확인
SELECT 
    rr.id,
    rr.member_id,
    rr.purchase_date,
    rr.expiry_date,
    rr.status,
    rr.rental_amount,
    rr.created_at
FROM rental_rights rr
JOIN members m ON rr.member_id = m.id
WHERE m.email = 'test@cnc.com';

-- 4. 계약과 임대권 모두 확인 (JOIN)
SELECT 
    m.id as member_id,
    m.email,
    m.rental_status,
    sc.id as contract_id,
    sc.is_active as contract_active,
    sc.rental_contract_agreed,
    sc.service_contract_agreed,
    rr.id as rental_rights_id,
    rr.status as rental_status,
    rr.expiry_date
FROM members m
LEFT JOIN service_contracts sc ON m.id = sc.member_id AND sc.is_active = true
LEFT JOIN rental_rights rr ON m.id = rr.member_id AND rr.status = 'ACTIVE'
WHERE m.email = 'test@cnc.com';

-- 5. 만약 계약이 없다면 생성 (임시)
-- INSERT INTO service_contracts (member_id, rental_contract_agreed, service_contract_agreed, marketing_agreed, is_active, contract_date)
-- SELECT id, true, true, false, true, CURRENT_TIMESTAMP
-- FROM members 
-- WHERE email = 'test@cnc.com' 
-- AND NOT EXISTS (SELECT 1 FROM service_contracts WHERE member_id = members.id);