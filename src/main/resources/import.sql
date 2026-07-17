-- 10대 마스터 관광지 데이터 (KTO AreaCode 및 SigunguCode 정확 매핑)
-- 심사위원 현장 테스트용 기본 적재 마스터 데이터셋 명세
MERGE INTO members (id, username, email, password, balance, created_at) KEY(id) VALUES (1, '심사위원_테스트계정', 'judge@tourfolio.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1000000.00, NOW());

-- 1. 경복궁 (서울 종로구) - AreaCode: 1, SigunguCode: 1
MERGE INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) KEY(id) VALUES (1, '경복궁', '1', '126508', '1', 1, '역사', '서울', '서울특별시 종로구 사직로 161', 15000.00, 15000.00, 15000.00, 0.9210, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '서울', '역사', '조선 왕조의 정궁으로, 한국의 대표적인 역사 문화 유산입니다. 광화문 앞 광장에서 다양한 문화 행사가 열립니다.');

-- 2. 성산일출봉 (제주 서귀포시) - AreaCode: 39, SigunguCode: 1
MERGE INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) KEY(id) VALUES (2, '성산일출봉', '39', '126449', '1', 1, '자연', '제주', '제주특별자치도 서귀포시 성산읍 성산리 1', 12000.00, 12000.00, 12000.00, 0.8950, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '제주', '자연', '제주 동부의 화산섬 지형으로, 장엄한 일출과 독특한 지질학적 가치를 지닌 유네스코 자연유산입니다.');

-- 3. 전주한옥마을 (전북 전주시) - AreaCode: 37, SigunguCode: 1
MERGE INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) KEY(id) VALUES (3, '전주한옥마을', '37', '264185', '1', 1, '역사', '전북', '전북특별자치도 전주시 완산구 기린대로 99', 13500.00, 13500.00, 13500.00, 0.9340, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '전북', '역사', '한국의 대표적인 전통 한옥 마을로, 전통 문화 체험이 가능합니다.');

-- 4. 남산서울타워 (서울 용산구) - AreaCode: 1, SigunguCode: 2
MERGE INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) KEY(id) VALUES (4, '남산서울타워', '1', '264186', '2', 1, '레저', '서울', '서울특별시 용산구 남산공원길 105', 11000.00, 11000.00, 11000.00, 0.9120, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '서울', '레저', '서울의 랜드마크로, 도심 전망과 낭만적인 야경을 즐길 수 있는 대표적인 관광지입니다.');

-- 5. 지리산 천왕봉 (전북 무주군) - AreaCode: 35, SigunguCode: 1
MERGE INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) KEY(id) VALUES (5, '지리산 천왕봉', '35', '264187', '1', 1, '자연', '전북', '전북특별자치도 무주군 무주읍 무풍로 100', 9500.00, 9500.00, 9500.00, 0.8780, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '전북', '자연', '한국의 3대 명산 중 하나로, 계절별 다양한 매력을 지닌 국립공원입니다.');

-- 6. 순천만국가정원 (전남 순천시) - AreaCode: 38, SigunguCode: 1
MERGE INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) KEY(id) VALUES (6, '순천만국가정원', '38', '264188', '1', 1, '자연', '전남', '전남특별자치도 순천시 승주읍 낙안읍길 100', 10500.00, 10500.00, 10500.00, 0.8890, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '전남', '자연', '유네스코 생물권 보전 지역으로, 갯벌과 철새 서식지가 풍부합니다.');

-- 7. 통영케이블카 (경남 통영시) - AreaCode: 36, SigunguCode: 1
MERGE INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) KEY(id) VALUES (7, '통영케이블카', '36', '264189', '1', 2, '레저', '경남', '경상남도 통영시 통영대로 100', 8500.00, 8500.00, 8500.00, 0.8670, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '경남', '레저', '통영의 대표적인 관광지로, 케이블카에서 바라보는 해안 절경이 장관입니다.');

-- 8. 해운대해수욕장 (부산 해운대구) - AreaCode: 6, SigunguCode: 1
MERGE INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) KEY(id) VALUES (8, '해운대해수욕장', '6', '264175', '1', 1, '레저', '부산', '부산광역시 해운대구 해운대해변로 264', 9000.00, 9000.00, 9000.00, 0.9010, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '부산', '레저', '부산의 대표적인 해수욕장으로, 아름다운 해변과 다양한 해양 스포츠를 즐길 수 있습니다.');

-- 9. 광안리해수욕장 (부산 수영구) - AreaCode: 6, SigunguCode: 2
MERGE INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) KEY(id) VALUES (9, '광안리해수욕장', '6', '264190', '2', 2, '레저', '부산', '부산광역시 수영구 광안로 100', 8200.00, 8200.00, 8200.00, 0.8560, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '부산', '레저', '광안대교의 야경과 함께 아름다운 해변 풍경을 자랑하는 대표적인 해수욕장입니다.');

-- 10. 경주 불국사 (경북 경주시) - AreaCode: 35, SigunguCode: 1
MERGE INTO spots (id, name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, current_price, prev_price, tourism_data_weight, last_updated, created_at, image_url, area_name, theme_tag, description) KEY(id) VALUES (10, '경주 불국사', '35', '264187', '1', 1, '역사', '경북', '경상북도 경주시 불국로 385', 14500.00, 14500.00, 14500.00, 0.9450, NOW(), NOW(), 'https://tong.visitkorea.or.kr/cms/resource/20/2666420_image2_1.jpg', '경북', '역사', '유네스코 세계문화유산으로, 신라 불교 예술의 정수를 보여주는 사찰입니다.');
