-- =========================================================
-- 1. SPOTS 테이블 10개 정밀 데이터 UPDATE (DB 스키마 완벽 호환)
-- =========================================================

-- 1. 경복궁
UPDATE spots SET
                 name = '경복궁',
                 region = '서울',
                 area_code = '1',
                 area_name = '서울',
                 theme = '역사',
                 tier = 1,
                 initial_price = 10000,
                 current_price = 10000,
                 prev_price = 9500,
                 view_count = 100,
                 content_id = '126508',
                 image_url = 'https://tong.visitkorea.or.kr/cms/resource/98/3487598_image2_1.jpg',
                 mapx = '126.9767219',
                 mapy = '37.57603072',
                 address = '서울특별시 종로구 사직로 161 (세종로)',
                 description = '조선 왕조의 법궁이자 대한민국을 대표하는 궁궐입니다. 조선 시대의 역사와 전통 건축의 아름다움을 가장 잘 보여주는 서울의 대표 역사 문화유산입니다.',
                 theme_tag = '역사,궁궐,조선왕조,국보',
                 last_updated = NOW()
WHERE id = 1;

-- 2. 성산일출봉
UPDATE spots SET
                 name = '성산일출봉',
                 region = '제주',
                 area_code = '39',
                 area_name = '제주',
                 theme = '자연',
                 tier = 1,
                 initial_price = 10000,
                 current_price = 10000,
                 prev_price = 9500,
                 view_count = 100,
                 content_id = '126435',
                 image_url = 'https://tong.visitkorea.or.kr/cms/resource/35/3343535_image2_1.jpg',
                 mapx = '126.9415156',
                 mapy = '33.45811112',
                 address = '제주특별자치도 서귀포시 성산읍 일출로 284-12',
                 description = '제주를 대표하는 화산 지형으로, 유네스코 세계자연유산에 등재된 명소입니다. 독특한 분화구와 아름다운 해안 경관으로 많은 여행객이 찾는 제주의 상징입니다.',
                 theme_tag = '자연,화산,유네스코 세계자연유산,일출',
                 last_updated = NOW()
WHERE id = 2;

-- 3. 전주한옥마을
UPDATE spots SET
                 name = '전주한옥마을',
                 region = '전북',
                 area_code = '37',
                 area_name = '전북',
                 theme = '역사',
                 tier = 2,
                 initial_price = 5000,
                 current_price = 5000,
                 prev_price = 4800,
                 view_count = 50,
                 content_id = '130456',
                 image_url = 'https://tong.visitkorea.or.kr/cms/resource/82/3048882_image2_1.jpg',
                 mapx = '127.1536126',
                 mapy = '35.81827276',
                 address = '전북특별자치도 전주시 완산구 기린대로 99',
                 description = '700여 채의 전통 한옥이 모여 있는 대한민국 대표 전통문화 마을입니다. 한옥과 한복, 전통 음식 등 한국의 옛 정취를 한곳에서 경험할 수 있습니다.',
                 theme_tag = '역사,한옥,전통문화',
                 last_updated = NOW()
WHERE id = 3;

-- 4. 남산서울타워
UPDATE spots SET
                 name = '남산서울타워',
                 region = '서울',
                 area_code = '1',
                 area_name = '서울',
                 theme = '문화',
                 tier = 1,
                 initial_price = 10000,
                 current_price = 10000,
                 prev_price = 9500,
                 view_count = 100,
                 content_id = '126487',
                 image_url = 'https://images.unsplash.com/photo-1538485399081-7191377e8241?w=800',
                 mapx = '126.9878821',
                 mapy = '37.55105454',
                 address = '서울특별시 용산구 남산공원길 105 (용산동2가)',
                 description = '남산 정상에 위치한 서울의 대표 랜드마크입니다. 서울 도심을 한눈에 조망할 수 있는 전망 명소이자 국내외 관광객이 즐겨 찾는 상징적인 관광지입니다.',
                 theme_tag = '문화,전망대,야경',
                 last_updated = NOW()
WHERE id = 4;

-- 5. 지리산 천왕봉
UPDATE spots SET
                 name = '지리산 천왕봉',
                 region = '경남',
                 area_code = '36',
                 area_name = '경남',
                 theme = '자연',
                 tier = 1,
                 initial_price = 10000,
                 current_price = 10000,
                 prev_price = 9500,
                 view_count = 100,
                 content_id = '126543',
                 image_url = 'https://tong.visitkorea.or.kr/cms/resource/63/2805963_image2_1.jpg',
                 mapx = '127.7488858',
                 mapy = '35.30358263',
                 address = '경상남도 산청군 시천면 중산리',
                 description = '대한민국 최고봉으로, 웅장한 산세와 다양한 생태계를 품은 국립공원의 중심입니다. 국내를 대표하는 산악 관광지이자 등산 명소로 사랑받고 있습니다.',
                 theme_tag = '자연,산,대한민국 100대 명산,등산',
                 last_updated = NOW()
WHERE id = 5;

-- 6. 순천만국가정원
UPDATE spots SET
                 name = '순천만국가정원',
                 region = '전남',
                 area_code = '38',
                 area_name = '전남',
                 theme = '자연',
                 tier = 3,
                 initial_price = 2000,
                 current_price = 2000,
                 prev_price = 1900,
                 view_count = 20,
                 content_id = '678901',
                 image_url = 'https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?w=800',
                 mapx = '127.4996986',
                 mapy = '34.92860321',
                 address = '전라남도 순천시 국가정원1호길 47',
                 description = '대한민국 최초의 국가정원으로, 다양한 식물과 정원 문화를 만날 수 있는 자연 관광지입니다. 아름다운 경관과 사계절의 풍경으로 힐링 여행지로 손꼽힙니다.',
                 theme_tag = '자연,정원,꽃,힐링',
                 last_updated = NOW()
WHERE id = 6;

-- 7. 통영케이블카
UPDATE spots SET
                 name = '통영케이블카',
                 region = '경남',
                 area_code = '36',
                 area_name = '경남',
                 theme = '문화',
                 tier = 3,
                 initial_price = 2000,
                 current_price = 2000,
                 prev_price = 1900,
                 view_count = 20,
                 content_id = '567890',
                 image_url = 'https://tong.visitkorea.or.kr/cms/resource/82/2612382_image2_1.jpg',
                 mapx = '128.4250765',
                 mapy = '34.82659821',
                 address = '경상남도 통영시 발개로 205 (도남동)',
                 description = '한려수도의 아름다운 바다와 섬을 한눈에 감상할 수 있는 통영의 대표 관광시설입니다. 미륵산과 남해의 절경을 함께 즐길 수 있는 인기 명소입니다.',
                 theme_tag = '문화,케이블카,한려수도,바다',
                 last_updated = NOW()
WHERE id = 7;

-- 8. 해운대해수욕장
UPDATE spots SET
                 name = '해운대해수욕장',
                 region = '부산',
                 area_code = '6',
                 area_name = '부산',
                 theme = '자연',
                 tier = 1,
                 initial_price = 10000,
                 current_price = 10000,
                 prev_price = 9500,
                 view_count = 100,
                 content_id = '126078',
                 image_url = 'https://tong.visitkorea.or.kr/cms/resource/88/2650088_image2_1.jpg',
                 mapx = '129.1602786',
                 mapy = '35.15908402',
                 address = '부산광역시 해운대구 해운대해변로 264 (우동)',
                 description = '대한민국을 대표하는 해수욕장으로, 넓은 백사장과 푸른 바다가 어우러진 부산 최고의 관광 명소입니다. 사계절 내내 많은 여행객이 찾는 국내 대표 해변입니다.',
                 theme_tag = '자연,해수욕장,바다,여름',
                 last_updated = NOW()
WHERE id = 8;

-- 9. 광안리해수욕장
UPDATE spots SET
                 name = '광안리해수욕장',
                 region = '부산',
                 area_code = '6',
                 area_name = '부산',
                 theme = '자연',
                 tier = 2,
                 initial_price = 5000,
                 current_price = 5000,
                 prev_price = 4800,
                 view_count = 50,
                 content_id = '126078',
                 image_url = 'https://tong.visitkorea.or.kr/cms/resource/21/2650021_image2_1.jpg',
                 mapx = '129.1185199',
                 mapy = '35.15377279',
                 address = '부산광역시 수영구 광안해변로 219 (광안동)',
                 description = '광안대교를 배경으로 펼쳐지는 부산의 대표 해변입니다. 아름다운 바다와 도심 야경이 조화를 이루며 부산을 상징하는 관광지로 알려져 있습니다.',
                 theme_tag = '자연,해수욕장,바다,여름,야경',
                 last_updated = NOW()
WHERE id = 9;

-- 10. 경주 불국사
UPDATE spots SET
                 name = '경주 불국사',
                 region = '경북',
                 area_code = '35',
                 area_name = '경북',
                 theme = '역사',
                 tier = 2,
                 initial_price = 5000,
                 current_price = 5000,
                 prev_price = 4800,
                 view_count = 50,
                 content_id = '126512',
                 image_url = 'https://tong.visitkorea.or.kr/cms/resource/28/2612328_image2_1.jpg',
                 mapx = '129.3317254',
                 mapy = '35.79230232',
                 address = '경상북도 경주시 불국로 385 (진현동)',
                 description = '신라 불교문화를 대표하는 사찰로, 유네스코 세계문화유산에 등재된 대한민국의 대표 문화유산입니다. 천년의 역사와 뛰어난 건축미를 간직한 경주의 상징입니다.',
                 theme_tag = '역사,사찰,유네스코 세계문화유산,국보',
                 last_updated = NOW()
WHERE id = 10;


-- =========================================================
-- 2. CARDS 테이블 10개 정밀 데이터 UPDATE
-- =========================================================

UPDATE cards SET rarity = 'LEGEND', theme = '역사', latitude = 37.57603072, longitude = 126.9767219, glow_color_code = '#FFD700', phrase = '500년의 역사가 숨 쉬는 조선의 법궁' WHERE spot_id = 1;
UPDATE cards SET rarity = 'LEGEND', theme = '자연', latitude = 33.45811112, longitude = 126.9415156, glow_color_code = '#FFD700', phrase = '세계자연유산, 푸른 바다 위 솟아오른 일출' WHERE spot_id = 2;
UPDATE cards SET rarity = 'EPIC', theme = '역사', latitude = 35.81827276, longitude = 127.1536126, glow_color_code = '#8A2BE2', phrase = '700여 채 한옥 속에 흐르는 한국의 멋' WHERE spot_id = 3;
UPDATE cards SET rarity = 'NORMAL', theme = '문화', latitude = 37.55105454, longitude = 126.9878821, glow_color_code = NULL, phrase = '서울 한복판, 파노라마로 펼쳐지는 도심 야경' WHERE spot_id = 4;
UPDATE cards SET rarity = 'RARE', theme = '자연', latitude = 35.30358263, longitude = 127.7488858, glow_color_code = '#C0C0C0', phrase = '대한민국 최고봉, 웅장한 구름 바다를 품다' WHERE spot_id = 5;
UPDATE cards SET rarity = 'RARE', theme = '자연', latitude = 34.92860321, longitude = 127.4996986, glow_color_code = '#C0C0C0', phrase = '대한민국 1호 국가정원, 사계절 힐링의 숲' WHERE spot_id = 6;
UPDATE cards SET rarity = 'EPIC', theme = '문화', latitude = 34.82659821, longitude = 128.4250765, glow_color_code = '#8A2BE2', phrase = '한려수도 푸른 바다와 섬을 한눈에 담다' WHERE spot_id = 7;
UPDATE cards SET rarity = 'EPIC', theme = '자연', latitude = 35.15908402, longitude = 129.1602786, glow_color_code = '#8A2BE2', phrase = '대한민국 대표 해변, 시원한 바다 파도의 설렘' WHERE spot_id = 8;
UPDATE cards SET rarity = 'NORMAL', theme = '자연', latitude = 35.15377279, longitude = 129.1185199, glow_color_code = NULL, phrase = '광안대교 불빛 아래 빛나는 낭만의 밤바다' WHERE spot_id = 9;
UPDATE cards SET rarity = 'LEGEND', theme = '역사', latitude = 35.79230232, longitude = 129.3317254, glow_color_code = '#FFD700', phrase = '신라 천년의 숨결이 서린 유네스코 문화유산' WHERE spot_id = 10;