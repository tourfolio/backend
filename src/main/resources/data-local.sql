-- 테스트 시딩 (local 프로필 전용)
-- 10개 마스터 관광지 데이터 (KTO TourAPI 관광지 시군구 코드정보 v1.0 기준)
-- MySQL 호환 문법 (INSERT ... ON DUPLICATE KEY UPDATE)

-- 경복궁
INSERT INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, ipo_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) 
VALUES (1, '경복궁', '11', 'TODO', '11110', 1, '역사', '서울', '서울특별시 종로구 사직로 161', 10000.00, 10000.00, 10000.00, 10000.00, 0.5000, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '서울', '역사', '조선 왕조의 정궁으로, 한국의 대표적인 역사 문화 유산입니다.')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    area_code = VALUES(area_code),
    content_id = VALUES(content_id),
    signgu_cd = VALUES(signgu_cd),
    tier = VALUES(tier),
    theme = VALUES(theme),
    region = VALUES(region),
    address = VALUES(address),
    initial_price = VALUES(initial_price),
    ipo_price = VALUES(ipo_price),
    current_price = VALUES(current_price),
    prev_price = VALUES(prev_price),
    tourism_data_weight = VALUES(tourism_data_weight),
    last_updated = VALUES(last_updated),
    image_url = VALUES(image_url),
    area_name = VALUES(area_name),
    theme_tag = VALUES(theme_tag),
    description = VALUES(description);

-- 성산일출봉
INSERT INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, ipo_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) 
VALUES (2, '성산일출봉', '50', 'TODO', '50130', 1, '자연', '제주', '제주특별자치도 서귀포시 성산읍 성산리 1', 10000.00, 10000.00, 10000.00, 10000.00, 0.5000, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '제주', '자연', '제주 동부의 화산섬 지형으로, 장엄한 일출과 독특한 지질학적 가치를 지닌 유네스코 자연유산입니다.')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    area_code = VALUES(area_code),
    content_id = VALUES(content_id),
    signgu_cd = VALUES(signgu_cd),
    tier = VALUES(tier),
    theme = VALUES(theme),
    region = VALUES(region),
    address = VALUES(address),
    initial_price = VALUES(initial_price),
    ipo_price = VALUES(ipo_price),
    current_price = VALUES(current_price),
    prev_price = VALUES(prev_price),
    tourism_data_weight = VALUES(tourism_data_weight),
    last_updated = VALUES(last_updated),
    image_url = VALUES(image_url),
    area_name = VALUES(area_name),
    theme_tag = VALUES(theme_tag),
    description = VALUES(description);

-- 해운대해수욕장
INSERT INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, ipo_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) 
VALUES (3, '해운대해수욕장', '26', 'TODO', '26350', 1, '레저', '부산', '부산광역시 해운대구 해운대해변로 264', 10000.00, 10000.00, 10000.00, 10000.00, 0.5000, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '부산', '레저', '한국 최대 규모의 해수욕장으로, 아름다운 해안선과 현대적인 도시 풍경이 어우러진 휴양지입니다.')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    area_code = VALUES(area_code),
    content_id = VALUES(content_id),
    signgu_cd = VALUES(signgu_cd),
    tier = VALUES(tier),
    theme = VALUES(theme),
    region = VALUES(region),
    address = VALUES(address),
    initial_price = VALUES(initial_price),
    ipo_price = VALUES(ipo_price),
    current_price = VALUES(current_price),
    prev_price = VALUES(prev_price),
    tourism_data_weight = VALUES(tourism_data_weight),
    last_updated = VALUES(last_updated),
    image_url = VALUES(image_url),
    area_name = VALUES(area_name),
    theme_tag = VALUES(theme_tag),
    description = VALUES(description);

-- 광안리해수욕장
INSERT INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, ipo_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) 
VALUES (4, '광안리해수욕장', '26', 'TODO', '26500', 2, '레저', '부산', '부산광역시 수영구 광안해변로 219', 5000.00, 5000.00, 5000.00, 5000.00, 0.5000, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '부산', '레저', '광안대교의 야경으로 유명한 해수욕장으로, 밤의 풍경이 특히 아름답습니다.')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    area_code = VALUES(area_code),
    content_id = VALUES(content_id),
    signgu_cd = VALUES(signgu_cd),
    tier = VALUES(tier),
    theme = VALUES(theme),
    region = VALUES(region),
    address = VALUES(address),
    initial_price = VALUES(initial_price),
    ipo_price = VALUES(ipo_price),
    current_price = VALUES(current_price),
    prev_price = VALUES(prev_price),
    tourism_data_weight = VALUES(tourism_data_weight),
    last_updated = VALUES(last_updated),
    image_url = VALUES(image_url),
    area_name = VALUES(area_name),
    theme_tag = VALUES(theme_tag),
    description = VALUES(description);

-- 전주한옥마을
INSERT INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, ipo_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) 
VALUES (5, '전주한옥마을', '52', 'TODO', '52111', 2, '역사', '전북', '전북특별자치도 전주시 완산구 기린대로 99', 5000.00, 5000.00, 5000.00, 5000.00, 0.5000, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '전북', '역사', '한국의 대표적인 전통 한옥 마을로, 전통 문화 체험이 가능합니다.')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    area_code = VALUES(area_code),
    content_id = VALUES(content_id),
    signgu_cd = VALUES(signgu_cd),
    tier = VALUES(tier),
    theme = VALUES(theme),
    region = VALUES(region),
    address = VALUES(address),
    initial_price = VALUES(initial_price),
    ipo_price = VALUES(ipo_price),
    current_price = VALUES(current_price),
    prev_price = VALUES(prev_price),
    tourism_data_weight = VALUES(tourism_data_weight),
    last_updated = VALUES(last_updated),
    image_url = VALUES(image_url),
    area_name = VALUES(area_name),
    theme_tag = VALUES(theme_tag),
    description = VALUES(description);

-- 경주 불국사
INSERT INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, ipo_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) 
VALUES (6, '경주 불국사', '47', 'TODO', '47130', 2, '역사', '경북', '경상북도 경주시 불국로 385', 5000.00, 5000.00, 5000.00, 5000.00, 0.5000, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '경북', '역사', '유네스코 세계문화유산으로, 신라 불교 예술의 정수를 보여주는 사찰입니다.')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    area_code = VALUES(area_code),
    content_id = VALUES(content_id),
    signgu_cd = VALUES(signgu_cd),
    tier = VALUES(tier),
    theme = VALUES(theme),
    region = VALUES(region),
    address = VALUES(address),
    initial_price = VALUES(initial_price),
    ipo_price = VALUES(ipo_price),
    current_price = VALUES(current_price),
    prev_price = VALUES(prev_price),
    tourism_data_weight = VALUES(tourism_data_weight),
    last_updated = VALUES(last_updated),
    image_url = VALUES(image_url),
    area_name = VALUES(area_name),
    theme_tag = VALUES(theme_tag),
    description = VALUES(description);

-- 통영케이블카
INSERT INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, ipo_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) 
VALUES (7, '통영케이블카', '48', 'TODO', '48220', 3, '레저', '경남', '경상남도 통영시 도산면 케이블카길 50', 2000.00, 2000.00, 2000.00, 2000.00, 0.5000, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '경남', '레저', '해안가에 위치한 케이블카로, 통영의 아름다운 해안 절경을 조망할 수 있습니다.')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    area_code = VALUES(area_code),
    content_id = VALUES(content_id),
    signgu_cd = VALUES(signgu_cd),
    tier = VALUES(tier),
    theme = VALUES(theme),
    region = VALUES(region),
    address = VALUES(address),
    initial_price = VALUES(initial_price),
    ipo_price = VALUES(ipo_price),
    current_price = VALUES(current_price),
    prev_price = VALUES(prev_price),
    tourism_data_weight = VALUES(tourism_data_weight),
    last_updated = VALUES(last_updated),
    image_url = VALUES(image_url),
    area_name = VALUES(area_name),
    theme_tag = VALUES(theme_tag),
    description = VALUES(description);

-- 순천만국가정원
INSERT INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, ipo_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) 
VALUES (8, '순천만국가정원', '46', 'TODO', '46150', 3, '자연', '전남', '전남특별자치도 순천시 승주읍 낙안읍길 100', 2000.00, 2000.00, 2000.00, 2000.00, 0.5000, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '전남', '자연', '아시아 최대 규모의 정원으로, 계절별 다양한 꽃과 자연 풍경을 즐길 수 있습니다.')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    area_code = VALUES(area_code),
    content_id = VALUES(content_id),
    signgu_cd = VALUES(signgu_cd),
    tier = VALUES(tier),
    theme = VALUES(theme),
    region = VALUES(region),
    address = VALUES(address),
    initial_price = VALUES(initial_price),
    ipo_price = VALUES(ipo_price),
    current_price = VALUES(current_price),
    prev_price = VALUES(prev_price),
    tourism_data_weight = VALUES(tourism_data_weight),
    last_updated = VALUES(last_updated),
    image_url = VALUES(image_url),
    area_name = VALUES(area_name),
    theme_tag = VALUES(theme_tag),
    description = VALUES(description);

-- 지리산 천왕봉
INSERT INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, ipo_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) 
VALUES (9, '지리산 천왕봉', '48', 'TODO', '48860', 4, '자연', '전남', '전남특별자치도 구례군 광의면 천왕봉길 1', 1000.00, 1000.00, 1000.00, 1000.00, 0.5000, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '전남', '자연', '한국의 3대 명산 중 하나로, 장엄한 자연 풍경과 등산 코스가 탁월합니다.')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    area_code = VALUES(area_code),
    content_id = VALUES(content_id),
    signgu_cd = VALUES(signgu_cd),
    tier = VALUES(tier),
    theme = VALUES(theme),
    region = VALUES(region),
    address = VALUES(address),
    initial_price = VALUES(initial_price),
    ipo_price = VALUES(ipo_price),
    current_price = VALUES(current_price),
    prev_price = VALUES(prev_price),
    tourism_data_weight = VALUES(tourism_data_weight),
    last_updated = VALUES(last_updated),
    image_url = VALUES(image_url),
    area_name = VALUES(area_name),
    theme_tag = VALUES(theme_tag),
    description = VALUES(description);

-- 남산서울타워
INSERT INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, ipo_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) 
VALUES (10, '남산서울타워', '11', 'TODO', '11170', 1, '문화', '서울', '서울특별시 용산구 남산공원길 105', 10000.00, 10000.00, 10000.00, 10000.00, 0.5000, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '서울', '문화', '서울의 랜드마크로, 도시 전체의 야경을 조망할 수 있는 대표적인 관광지입니다.')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    area_code = VALUES(area_code),
    content_id = VALUES(content_id),
    signgu_cd = VALUES(signgu_cd),
    tier = VALUES(tier),
    theme = VALUES(theme),
    region = VALUES(region),
    address = VALUES(address),
    initial_price = VALUES(initial_price),
    ipo_price = VALUES(ipo_price),
    current_price = VALUES(current_price),
    prev_price = VALUES(prev_price),
    tourism_data_weight = VALUES(tourism_data_weight),
    last_updated = VALUES(last_updated),
    image_url = VALUES(image_url),
    area_name = VALUES(area_name),
    theme_tag = VALUES(theme_tag),
    description = VALUES(description);
