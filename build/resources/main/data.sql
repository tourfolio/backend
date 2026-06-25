-- 심사위원 현장 테스트용 기본 적재 마스터 데이터셋 명세
INSERT INTO members (id, username, balance, created_at)
VALUES (1, '심사위원_테스트계정', 1000000.00, NOW());

INSERT INTO spots (id, name, area_code, content_id, tier, current_price, prev_price, tourism_data_weight, last_updated, created_at)
VALUES (1, '부산 해운대 엘시티', '6', '264175', 1, 5000.00, 5000.00, 85.50, NOW(), NOW());

INSERT INTO spots (id, name, area_code, content_id, tier, current_price, prev_price, tourism_data_weight, last_updated, created_at)
VALUES (2, '서울 경복궁 광화문', '1', '126508', 1, 12000.00, 12000.00, 92.10, NOW(), NOW());

INSERT INTO spots (id, name, area_code, content_id, tier, current_price, prev_price, tourism_data_weight, last_updated, created_at)
VALUES (3, '제주 성산일출봉', '39', '126449', 2, 3500.00, 3500.00, 74.30, NOW(), NOW());