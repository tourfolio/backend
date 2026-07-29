-- 포토카드 초기 더미 데이터 (10개)
-- spots 테이블의 관광지 ID와 매핑됨
-- 실제 운영 시에는 spots 테이블의 데이터와 동기화 필요

-- 경복궁 (서울) - LEGEND
INSERT INTO cards (spot_id, rarity, theme, latitude, longitude, glow_color_code, phrase, created_at)
VALUES (1, 'LEGEND', '역사', 37.5796, 126.9770, '#FFD700', '500년의 역사가 숨 쉬는 곳', NOW());

-- 해운대 (부산) - EPIC
INSERT INTO cards (spot_id, rarity, theme, latitude, longitude, glow_color_code, phrase, created_at)
VALUES (2, 'EPIC', '레저', 35.1587, 129.1604, '#8A2BE2', '바다와 함께하는 설렘', NOW());

-- 성산일출봉 (제주) - LEGEND
INSERT INTO cards (spot_id, rarity, theme, latitude, longitude, glow_color_code, phrase, created_at)
VALUES (3, 'LEGEND', '자연', 33.4576, 126.9442, '#FFD700', '일출이 가장 아름다운 곳', NOW());

-- 남이섬 (강원) - RARE
INSERT INTO cards (spot_id, rarity, theme, latitude, longitude, glow_color_code, phrase, created_at)
VALUES (4, 'RARE', '자연', 37.7876, 127.5250, '#C0C0C0', '계절마다 다른 매력', NOW());

-- 한옥마을 (전주) - EPIC
INSERT INTO cards (spot_id, rarity, theme, latitude, longitude, glow_color_code, phrase, created_at)
VALUES (5, 'EPIC', '문화', 35.8154, 127.1495, '#8A2BE2', '한국의 아름다움을 느끼다', NOW());

-- 불국사 (경주) - LEGEND
INSERT INTO cards (spot_id, rarity, theme, latitude, longitude, glow_color_code, phrase, created_at)
VALUES (6, 'LEGEND', '역사', 35.7896, 129.1110, '#FFD700', '천년의 시간이 머무는 곳', NOW());

-- 제주항 (제주) - RARE
INSERT INTO cards (spot_id, rarity, theme, latitude, longitude, glow_color_code, phrase, created_at)
VALUES (7, 'RARE', '레저', 33.5141, 126.5297, '#C0C0C0', '바다의 풍경을 담다', NOW());

-- 남산타워 (서울) - NORMAL
INSERT INTO cards (spot_id, rarity, theme, latitude, longitude, glow_color_code, phrase, created_at)
VALUES (8, 'NORMAL', '문화', 37.5512, 126.9882, NULL, '서울의 랜드마크', NOW());

-- 속초해수욕장 (강원) - NORMAL
INSERT INTO cards (spot_id, rarity, theme, latitude, longitude, glow_color_code, phrase, created_at)
VALUES (9, 'NORMAL', '레저', 38.2070, 128.5918, NULL, '푸른 바다의 휴식', NOW());

-- 동부산여객터미널 (부산) - RARE
INSERT INTO cards (spot_id, rarity, theme, latitude, longitude, glow_color_code, phrase, created_at)
VALUES (10, 'RARE', '레저', 35.1270, 129.0485, '#C0C0C0', '여행의 시작과 끝', NOW());
