-- =========================================================
-- 가짜/자동생성 데이터 삭제 쿼리
-- =========================================================
-- 삭제 대상: price_history 테이블의 ID 261번 이상 또는 2026-08-04 이후의 모든 데이터
-- 보존 대상: ID 1번 ~ 260번 (2026-07-10 ~ 2026-08-04)까지의 실제 원본 데이터 260건

DELETE FROM price_history WHERE id > 260 OR trade_date > '2026-08-04';

-- 확인 쿼리 (삭제 후 실행하여 확인)
SELECT COUNT(*) as remaining_count FROM price_history;
SELECT MIN(id) as min_id, MAX(id) as max_id, MIN(trade_date) as min_date, MAX(trade_date) as max_date FROM price_history;
