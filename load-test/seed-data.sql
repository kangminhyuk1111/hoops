-- Load Test Seed Data
-- 서울 근처 매치 데이터 10000건 생성

-- 1. 테스트 유저 생성 (10명)
INSERT IGNORE INTO users (email, nickname, profile_image, rating, total_matches, created_at, updated_at) VALUES
('loadtest1@test.com', 'LoadUser01', NULL, 3.50, 10, NOW(), NOW()),
('loadtest2@test.com', 'LoadUser02', NULL, 4.00, 20, NOW(), NOW()),
('loadtest3@test.com', 'LoadUser03', NULL, 2.80, 5, NOW(), NOW()),
('loadtest4@test.com', 'LoadUser04', NULL, 4.50, 30, NOW(), NOW()),
('loadtest5@test.com', 'LoadUser05', NULL, 3.20, 15, NOW(), NOW()),
('loadtest6@test.com', 'LoadUser06', NULL, 4.80, 50, NOW(), NOW()),
('loadtest7@test.com', 'LoadUser07', NULL, 3.00, 8, NOW(), NOW()),
('loadtest8@test.com', 'LoadUser08', NULL, 4.20, 25, NOW(), NOW()),
('loadtest9@test.com', 'LoadUser09', NULL, 3.70, 12, NOW(), NOW()),
('loadtest10@test.com', 'LoadUser10', NULL, 4.10, 18, NOW(), NOW());

-- 유저 ID 조회용 변수 설정
SET @user_start_id = (SELECT MIN(id) FROM users WHERE email LIKE 'loadtest%');

-- 2. 위치 데이터 생성 (서울 주요 지역 20곳)
INSERT IGNORE INTO locations (user_id, alias, latitude, longitude, address, created_at, updated_at) VALUES
(@user_start_id, '강남역 농구장', 37.49794000, 127.02760000, '서울 강남구 강남대로 396', NOW(), NOW()),
(@user_start_id + 1, '홍대입구 체육관', 37.55712000, 126.92369000, '서울 마포구 양화로 160', NOW(), NOW()),
(@user_start_id + 2, '잠실 종합운동장', 37.51518000, 127.07340000, '서울 송파구 올림픽로 25', NOW(), NOW()),
(@user_start_id + 3, '여의도공원 코트', 37.52870000, 126.93410000, '서울 영등포구 여의공원로 68', NOW(), NOW()),
(@user_start_id + 4, '성수동 체육관', 37.54460000, 127.05590000, '서울 성동구 성수이로 51', NOW(), NOW()),
(@user_start_id + 5, '신촌 농구장', 37.55590000, 126.93690000, '서울 서대문구 신촌로 73', NOW(), NOW()),
(@user_start_id + 6, '건대입구 코트', 37.54030000, 127.06900000, '서울 광진구 능동로 120', NOW(), NOW()),
(@user_start_id + 7, '이태원 체육관', 37.53440000, 126.99430000, '서울 용산구 이태원로 177', NOW(), NOW()),
(@user_start_id + 8, '노원 농구장', 37.65560000, 127.06160000, '서울 노원구 동일로 1414', NOW(), NOW()),
(@user_start_id + 9, '목동 체육관', 37.52640000, 126.87530000, '서울 양천구 목동동로 99', NOW(), NOW()),
(@user_start_id, '사당 농구장', 37.47640000, 126.98170000, '서울 동작구 사당로 50', NOW(), NOW()),
(@user_start_id + 1, '왕십리 코트', 37.56120000, 127.03780000, '서울 성동구 왕십리로 222', NOW(), NOW()),
(@user_start_id + 2, '구로 체육관', 37.50340000, 126.88230000, '서울 구로구 디지털로 300', NOW(), NOW()),
(@user_start_id + 3, '종로 농구장', 37.57100000, 126.97890000, '서울 종로구 종로 104', NOW(), NOW()),
(@user_start_id + 4, '신림 코트', 37.48410000, 126.92980000, '서울 관악구 신림로 340', NOW(), NOW()),
(@user_start_id + 5, '수유 체육관', 37.63830000, 127.02520000, '서울 강북구 도봉로 325', NOW(), NOW()),
(@user_start_id + 6, '천호 농구장', 37.53890000, 127.12360000, '서울 강동구 천호대로 1005', NOW(), NOW()),
(@user_start_id + 7, '합정 코트', 37.54970000, 126.91380000, '서울 마포구 양화로 45', NOW(), NOW()),
(@user_start_id + 8, '역삼 체육관', 37.50070000, 127.03640000, '서울 강남구 역삼로 180', NOW(), NOW()),
(@user_start_id + 9, '마포 농구장', 37.55350000, 126.95120000, '서울 마포구 마포대로 33', NOW(), NOW());

SET @loc_start_id = (SELECT MIN(id) FROM locations WHERE alias LIKE '%농구장' OR alias LIKE '%체육관' OR alias LIKE '%코트');

-- 3. 매치 데이터 10000건 생성 (프로시저 사용)
DELIMITER //
CREATE PROCEDURE seed_matches()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_host_id BIGINT;
    DECLARE v_loc_id BIGINT;
    DECLARE v_latitude DECIMAL(10,8);
    DECLARE v_longitude DECIMAL(11,8);
    DECLARE v_address VARCHAR(500);
    DECLARE v_match_date DATE;
    DECLARE v_start_hour INT;
    DECLARE v_max_participants INT;
    DECLARE v_status VARCHAR(50);
    DECLARE v_nickname VARCHAR(50);

    WHILE i < 10000 DO
        -- 랜덤 호스트 선택
        SET v_host_id = @user_start_id + (i % 10);
        SET v_nickname = CONCAT('LoadUser', LPAD((i % 10) + 1, 2, '0'));

        -- 랜덤 위치 선택 (20개 중)
        SET v_loc_id = @loc_start_id + (i % 20);

        -- 서울 근처 좌표 랜덤 생성 (37.45~37.66, 126.87~127.13)
        SET v_latitude = 37.45 + (RAND() * 0.21);
        SET v_longitude = 126.87 + (RAND() * 0.26);
        SET v_address = CONCAT('서울시 테스트구 테스트동 ', i);

        -- 날짜: 오늘부터 30일 이내
        SET v_match_date = DATE_ADD(CURDATE(), INTERVAL FLOOR(RAND() * 30) DAY);

        -- 시작 시간: 8~20시
        SET v_start_hour = 8 + FLOOR(RAND() * 12);

        -- 참가 인원: 4~20명
        SET v_max_participants = 4 + FLOOR(RAND() * 17);

        -- 상태: 60% PENDING, 15% CONFIRMED, 10% IN_PROGRESS, 10% FULL, 5% CANCELLED
        SET v_status = CASE
            WHEN RAND() < 0.60 THEN 'PENDING'
            WHEN RAND() < 0.75 THEN 'CONFIRMED'
            WHEN RAND() < 0.85 THEN 'IN_PROGRESS'
            WHEN RAND() < 0.95 THEN 'FULL'
            ELSE 'CANCELLED'
        END;

        INSERT INTO matches (
            host_id, host_nickname, title, description,
            latitude, longitude, address,
            match_date, start_time, end_time,
            max_participants, current_participants,
            status, version, created_at, updated_at
        ) VALUES (
            v_host_id,
            v_nickname,
            CONCAT('부하테스트 매치 #', i + 1),
            CONCAT('부하테스트용 매치 데이터입니다. 인덱스: ', i),
            v_latitude,
            v_longitude,
            v_address,
            v_match_date,
            MAKETIME(v_start_hour, 0, 0),
            MAKETIME(v_start_hour + 2, 0, 0),
            v_max_participants,
            1 + FLOOR(RAND() * (v_max_participants - 1)),
            v_status,
            0,
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY),
            NOW()
        );

        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL seed_matches();
DROP PROCEDURE seed_matches;

-- 결과 확인
SELECT '=== Seed Data Summary ===' AS info;
SELECT COUNT(*) AS total_users FROM users WHERE email LIKE 'loadtest%';
SELECT COUNT(*) AS total_locations FROM locations;
SELECT COUNT(*) AS total_matches FROM matches;
SELECT status, COUNT(*) AS count FROM matches GROUP BY status;
