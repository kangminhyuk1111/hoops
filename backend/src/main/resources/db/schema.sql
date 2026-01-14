-- =====================================================
-- 1. User 도메인
-- =====================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '이메일 (고유)',
    nickname VARCHAR(50) NOT NULL UNIQUE COMMENT '닉네임 (고유)',
    profile_image VARCHAR(500) NULL COMMENT '프로필 이미지 URL',
    rating DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '사용자 평점 (0.00 ~ 5.00)',
    total_matches INT NOT NULL DEFAULT 0 COMMENT '총 참가 경기 수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT chk_user_rating CHECK (rating >= 0.00 AND rating <= 5.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보';

-- =====================================================
-- 2. Auth 도메인
-- =====================================================

CREATE TABLE IF NOT EXISTS auth_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'users.id 참조',
    provider VARCHAR(50) NOT NULL COMMENT '인증 제공자 (LOCAL, GOOGLE, KAKAO, NAVER)',
    provider_id VARCHAR(255) NULL COMMENT 'OAuth 제공자의 사용자 ID',
    password_hash VARCHAR(255) NULL COMMENT '비밀번호 해시 (LOCAL만 사용)',
    refresh_token VARCHAR(500) NULL COMMENT 'Refresh Token',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_auth_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_auth_provider CHECK (provider IN ('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER')),
    CONSTRAINT chk_auth_credentials CHECK (
        (provider = 'LOCAL' AND password_hash IS NOT NULL) OR
        (provider != 'LOCAL' AND provider_id IS NOT NULL)
    ),
    INDEX idx_auth_user (user_id),
    INDEX idx_auth_provider (provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='인증 계정 정보';

-- =====================================================
-- 3. Location 도메인
-- =====================================================

CREATE TABLE IF NOT EXISTS locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '생성한 사용자 ID',
    alias VARCHAR(100) NOT NULL COMMENT '지역 별칭 (예: "우리 동네 농구장")',
    latitude DECIMAL(10,8) NOT NULL COMMENT '위도',
    longitude DECIMAL(11,8) NOT NULL COMMENT '경도',
    location POINT AS (ST_SRID(POINT(longitude, latitude), 4326)) STORED NOT NULL COMMENT '위치 (Spatial Index용 Generated Column)',
    address VARCHAR(500) NULL COMMENT '주소 (선택)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_location_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_location_latitude CHECK (latitude >= -90 AND latitude <= 90),
    CONSTRAINT chk_location_longitude CHECK (longitude >= -180 AND longitude <= 180),
    INDEX idx_location_user (user_id),
    INDEX idx_location_lat_lng (latitude, longitude),
    SPATIAL INDEX idx_location_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 생성 지역 정보';

-- =====================================================
-- 4. Match 도메인
-- =====================================================

CREATE TABLE IF NOT EXISTS matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    host_id BIGINT NOT NULL COMMENT '경기 주최자 (users.id)',
    host_nickname VARCHAR(50) NOT NULL COMMENT '호스트 닉네임',
    title VARCHAR(200) NOT NULL COMMENT '경기 제목',
    description TEXT NULL COMMENT '경기 설명',
    latitude DECIMAL(10,8) NOT NULL COMMENT '위도 (Location에서 복사)',
    longitude DECIMAL(11,8) NOT NULL COMMENT '경도 (Location에서 복사)',
    location POINT AS (ST_SRID(POINT(longitude, latitude), 4326)) STORED NOT NULL COMMENT '위치 (Spatial Index용 Generated Column)',
    address VARCHAR(500) NULL COMMENT '주소',
    match_date DATE NOT NULL COMMENT '경기 날짜',
    start_time TIME NOT NULL COMMENT '시작 시간',
    end_time TIME NOT NULL COMMENT '종료 시간',
    max_participants INT NOT NULL COMMENT '최대 참가 인원',
    current_participants INT NOT NULL DEFAULT 0 COMMENT '현재 참가 인원',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '경기 상태 (PENDING, CONFIRMED, IN_PROGRESS, ENDED, CANCELLED, FULL)',
    cancelled_at DATETIME NULL COMMENT '취소 일시',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_match_host FOREIGN KEY (host_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_match_max_participants CHECK (max_participants > 0),
    CONSTRAINT chk_match_current_participants CHECK (current_participants >= 0 AND current_participants <= max_participants),
    CONSTRAINT chk_match_status CHECK (status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'ENDED', 'CANCELLED', 'FULL')),
    CONSTRAINT chk_match_latitude CHECK (latitude >= -90 AND latitude <= 90),
    CONSTRAINT chk_match_longitude CHECK (longitude >= -180 AND longitude <= 180),
    INDEX idx_match_host (host_id),
    INDEX idx_match_status (status),
    INDEX idx_match_date (match_date),
    INDEX idx_match_lat_lng (latitude, longitude),
    SPATIAL INDEX idx_match_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='경기 정보';

-- =====================================================
-- 5. Participation 도메인
-- =====================================================

CREATE TABLE IF NOT EXISTS participations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id BIGINT NOT NULL COMMENT 'matches.id 참조',
    user_id BIGINT NOT NULL COMMENT 'users.id 참조',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '참가 상태 (PENDING, CONFIRMED, CANCELLED, REJECTED, MATCH_CANCELLED)',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '참가 신청 일시',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 락 버전',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_participation_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_participation_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_participation_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'REJECTED', 'MATCH_CANCELLED')),
    UNIQUE INDEX idx_participation_unique (match_id, user_id),
    INDEX idx_participation_match (match_id),
    INDEX idx_participation_user (user_id),
    INDEX idx_participation_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='경기 참가 정보';

-- =====================================================
-- 6. Notification 도메인
-- =====================================================

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '알림 수신자 (users.id)',
    type VARCHAR(50) NOT NULL COMMENT '알림 타입 (PARTICIPATION_CREATED, PARTICIPATION_CANCELLED, MATCH_UPCOMING, MATCH_CANCELLED, MATCH_FULL)',
    title VARCHAR(200) NOT NULL COMMENT '알림 제목',
    message TEXT NOT NULL COMMENT '알림 내용',
    related_match_id BIGINT NULL COMMENT '관련 경기 ID (선택)',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '읽음 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_match FOREIGN KEY (related_match_id) REFERENCES matches(id) ON DELETE SET NULL,
    CONSTRAINT chk_notification_type CHECK (type IN ('PARTICIPATION_CREATED', 'PARTICIPATION_CANCELLED', 'MATCH_UPCOMING', 'MATCH_CANCELLED', 'MATCH_FULL')),
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_match (related_match_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 알림';
