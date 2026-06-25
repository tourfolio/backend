-- 초기 데이터베이스 리셋 및 테이블 디클레이어 문서
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS portfolios;
DROP TABLE IF EXISTS spots;
DROP TABLE IF EXISTS members;

CREATE TABLE members (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         username VARCHAR(50) NOT NULL,
                         balance DECIMAL(19,2) NOT NULL,
                         created_at TIMESTAMP NOT NULL
);

CREATE TABLE spots (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       area_code VARCHAR(20) NOT NULL,
                       content_id VARCHAR(50) NOT NULL,
                       tier INT NOT NULL,
                       current_price DECIMAL(19,2) NOT NULL,
                       prev_price DECIMAL(19,2) NOT NULL,
                       tourism_data_weight DECIMAL(19,2) NOT NULL,
                       last_updated TIMESTAMP NOT NULL,
                       created_at TIMESTAMP NOT NULL
);

CREATE TABLE portfolios (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            member_id BIGINT NOT NULL,
                            spot_id BIGINT NOT NULL,
                            quantity DECIMAL(19,2) NOT NULL,
                            average_purchase_price DECIMAL(19,2) NOT NULL,
                            updated_at TIMESTAMP NOT NULL,
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
                              created_at TIMESTAMP NOT NULL
);