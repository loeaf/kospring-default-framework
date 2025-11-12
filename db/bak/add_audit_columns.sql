-- 서비스 계약 테이블에 누락된 컬럼들 추가
ALTER TABLE service_contracts 
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- 임대권 테이블에 누락된 컬럼들 추가
ALTER TABLE rental_rights
ADD COLUMN IF NOT EXISTS auto_renewal BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS renewal_notice_sent BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- updated_at 자동 업데이트 트리거 추가
DROP TRIGGER IF EXISTS update_service_contracts_updated_at ON service_contracts;
CREATE TRIGGER update_service_contracts_updated_at
    BEFORE UPDATE ON service_contracts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_rental_rights_updated_at ON rental_rights;
CREATE TRIGGER update_rental_rights_updated_at
    BEFORE UPDATE ON rental_rights
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();