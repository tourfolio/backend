-- 초기 데이터베이스 리셋 및 테이블 디클레이어 문서
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS portfolios;
DROP TABLE IF EXISTS watchlists;
DROP TABLE IF EXISTS price_history;
DROP TABLE IF EXISTS spots;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    balance DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    attendance_date TIMESTAMP NOT NULL,
    points_awarded INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_member FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE spots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    area_code VARCHAR(20) NOT NULL,
    content_id VARCHAR(50) NOT NULL,
    signgu_cd VARCHAR(20),
    tier INT NOT NULL,
    theme VARCHAR(20) NOT NULL,
    region VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    initial_price DECIMAL(19,2) NOT NULL,
    ipo_price DECIMAL(19,2),
    current_price DECIMAL(19,2) NOT NULL,
    prev_price DECIMAL(19,2) NOT NULL,
    tourism_data_weight DECIMAL(19,2) NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    image_url VARCHAR(500),
    area_name VARCHAR(50),
    theme_tag VARCHAR(20),
    description VARCHAR(500)
);

CREATE TABLE price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    spot_id BIGINT NOT NULL,
    trade_date DATE NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    change_rate DECIMAL(10,4) NOT NULL,
    ts_score DECIMAL(10,4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_price_history_spot FOREIGN KEY (spot_id) REFERENCES spots(id) ON DELETE CASCADE,
    CONSTRAINT uq_spot_date UNIQUE (spot_id, trade_date)
);

CREATE TABLE watchlists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    spot_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_watchlist_member FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_watchlist_spot FOREIGN KEY (spot_id) REFERENCES spots(id) ON DELETE CASCADE,
    CONSTRAINT uq_member_watchlist UNIQUE (member_id, spot_id)
);

CREATE TABLE portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    spot_id BIGINT NOT NULL,
    quantity DECIMAL(19,2) NOT NULL,
    average_purchase_price DECIMAL(19,2) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_portfolio_member FOREIGN KEY (member_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_portfolio_spot FOREIGN KEY (spot_id) REFERENCES spots(id) ON DELETE CASCADE,
    CONSTRAINT uq_member_spot UNIQUE (member_id, spot_id)
);

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    spot_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    quantity DECIMAL(19,2) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    executed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_member FOREIGN KEY (member_id) REFERENCES users(id),
    CONSTRAINT fk_transaction_spot FOREIGN KEY (spot_id) REFERENCES spots(id)
);

-- 샘플 회원 데이터 (테스트용)
INSERT INTO users (nickname, email, password, balance, active, created_at, updated_at) VALUES
('testuser1', 'test1@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1000000.00, true, NOW(), NOW()),
('testuser2', 'test2@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 5000000.00, true, NOW(), NOW());

-- 샘플 관광지 데이터 (12개)
INSERT INTO spots (name, area_code, content_id, signgu_cd, tier, theme, region, address, initial_price, current_price, prev_price, tourism_data_weight, last_updated, created_at) VALUES
('서울 타워', '1', 'CNTS_0000000001', '1', 1, '문화', '서울', '서울특별시 용산구 남산공원길 105', 10000.00, 10000.00, 10000.00, 0.7500, NOW(), NOW()),
('경복궁', '1', 'CNTS_0000000002', '1', 1, '역사', '서울', '서울특별시 종로구 사직로 161', 15000.00, 15000.00, 15000.00, 0.8000, NOW(), NOW()),
('제주도 한라산', '6', 'CNTS_0000000003', '1', 1, '자연', '제주', '제주특별자치도 제주시 1100로 2070', 12000.00, 12000.00, 12000.00, 0.8500, NOW(), NOW()),
('부산 해운대', '6', 'CNTS_0000000004', '1', 2, '레저', '부산', '부산광역시 해운대구 해운대해변로 264', 8000.00, 8000.00, 8000.00, 0.7000, NOW(), NOW()),
('강원도 설악산', '32', 'CNTS_0000000005', '1', 1, '자연', '강원', '강원도 속초시 설악산로 2843', 9000.00, 9000.00, 9000.00, 0.7200, NOW(), NOW()),
('경주 불국사', '35', 'CNTS_0000000006', '1', 1, '역사', '경북', '경상북도 경주시 불국로 385', 11000.00, 11000.00, 11000.00, 0.7800, NOW(), NOW()),
('남이섬', '31', 'CNTS_0000000007', '1', 2, '자연', '강원', '강원도 춘천시 남산면 남이섬길 1', 7000.00, 7000.00, 7000.00, 0.6800, NOW(), NOW()),
('여수 엑스포', '38', 'CNTS_0000000008', '1', 2, '레저', '전남', '전라남도 여수시 엑스포로 200', 6500.00, 6500.00, 6500.00, 0.6500, NOW(), NOW()),
('남산골 한옥마을', '1', 'CNTS_0000000009', '1', 2, '문화', '서울', '서울특별시 중구 삼일대로 17길 16', 5500.00, 5500.00, 5500.00, 0.6200, NOW(), NOW()),
('속초 설악워터피아', '32', 'CNTS_0000000010', '1', 3, '레저', '강원', '강원도 속초시 노학로 365', 4500.00, 4500.00, 4500.00, 0.5800, NOW(), NOW()),
('안동 하회마을', '35', 'CNTS_0000000011', '1', 2, '문화', '경북', '경상북도 안동시 풍천면 하회종가길 40', 5000.00, 5000.00, 5000.00, 0.6000, NOW(), NOW()),
('통영 케이블카', '38', 'CNTS_0000000012', '1', 3, '레저', '경남', '경상남도 통영시 도산면 케이블카길 50', 4000.00, 4000.00, 4000.00, 0.5500, NOW(), NOW());

-- 샘플 가격 이력 데이터 (최근 7일)
INSERT INTO price_history (spot_id, trade_date, price, change_rate, ts_score) VALUES
(1, DATEADD(DAY, -6, CURRENT_DATE), 9800.00, -0.0200, 0.7200),
(1, DATEADD(DAY, -5, CURRENT_DATE), 9950.00, 0.0153, 0.7350),
(1, DATEADD(DAY, -4, CURRENT_DATE), 10100.00, 0.0151, 0.7450),
(1, DATEADD(DAY, -3, CURRENT_DATE), 10050.00, -0.0050, 0.7400),
(1, DATEADD(DAY, -2, CURRENT_DATE), 9980.00, -0.0070, 0.7380),
(1, DATEADD(DAY, -1, CURRENT_DATE), 10020.00, 0.0040, 0.7420),
(1, CURRENT_DATE, 10000.00, -0.0020, 0.7500),
(2, DATEADD(DAY, -6, CURRENT_DATE), 14800.00, -0.0133, 0.7800),
(2, DATEADD(DAY, -5, CURRENT_DATE), 15100.00, 0.0203, 0.7950),
(2, DATEADD(DAY, -4, CURRENT_DATE), 15200.00, 0.0066, 0.8020),
(2, DATEADD(DAY, -3, CURRENT_DATE), 14950.00, -0.0164, 0.7900),
(2, DATEADD(DAY, -2, CURRENT_DATE), 15050.00, 0.0067, 0.7960),
(2, DATEADD(DAY, -1, CURRENT_DATE), 15100.00, 0.0033, 0.7980),
(2, CURRENT_DATE, 15000.00, -0.0066, 0.8000);