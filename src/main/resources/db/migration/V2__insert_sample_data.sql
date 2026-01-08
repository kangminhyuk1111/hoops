-- =====================================================
-- Hoops 프로젝트 샘플 데이터
-- Version: V2
-- Description: 개발/테스트용 샘플 데이터 삽입
-- =====================================================

-- =====================================================
-- 1. 샘플 사용자 생성
-- =====================================================

INSERT INTO users (email, nickname, profile_image, rating, total_matches) VALUES
('host@example.com', 'HostPlayer', 'https://example.com/profiles/host.jpg', 4.5, 10),
('player1@example.com', 'Player1', 'https://example.com/profiles/player1.jpg', 4.0, 5),
('player2@example.com', 'Player2', 'https://example.com/profiles/player2.jpg', 3.8, 8),
('player3@example.com', 'Player3', NULL, 4.2, 3),
('newbie@example.com', 'Newbie', NULL, 0.0, 0);

-- =====================================================
-- 2. 샘플 인증 계정 생성 (LOCAL 방식)
-- =====================================================

-- BCrypt로 암호화된 "password123" (실제 구현 시 애플리케이션에서 암호화)
-- 여기서는 샘플이므로 간단한 해시값 사용

INSERT INTO auth_accounts (user_id, provider, provider_id, password_hash) VALUES
(1, 'LOCAL', NULL, '$2a$10$dummyHashForHostPlayer'),
(2, 'LOCAL', NULL, '$2a$10$dummyHashForPlayer1'),
(3, 'LOCAL', NULL, '$2a$10$dummyHashForPlayer2'),
(4, 'GOOGLE', 'google_123456', NULL),
(5, 'KAKAO', 'kakao_789012', NULL);

-- =====================================================
-- 3. 샘플 지역(Location) 생성
-- =====================================================

INSERT INTO locations (user_id, alias, latitude, longitude, address) VALUES
(1, '강남 농구장', 37.4979, 127.0276, '서울특별시 강남구 역삼동'),
(1, '잠실 체육관', 37.5133, 127.1000, '서울특별시 송파구 잠실동'),
(2, '홍대 스트리트 코트', 37.5563, 126.9239, '서울특별시 마포구 서교동'),
(3, '부산 해운대 코트', 35.1587, 129.1603, '부산광역시 해운대구');

-- =====================================================
-- 4. 샘플 경기(Match) 생성
-- =====================================================

-- 경기 1: 확정된 경기 (참가자 모집 중)
INSERT INTO matches (host_id, title, description, latitude, longitude, address, match_date, start_time, end_time, max_participants, current_participants, status) VALUES
(1, '주말 농구 게임 - 강남', '주말 아침 가볍게 농구 한 게임 하실 분!', 37.4979, 127.0276, '서울특별시 강남구 역삼동', '2026-01-15', '10:00:00', '12:00:00', 10, 3, 'CONFIRMED');

-- 경기 2: 정원 마감된 경기
INSERT INTO matches (host_id, title, description, latitude, longitude, address, match_date, start_time, end_time, max_participants, current_participants, status) VALUES
(2, '잠실 3on3 토너먼트', '3대3 농구 토너먼트! 실력자만 오세요', 37.5133, 127.1000, '서울특별시 송파구 잠실동', '2026-01-12', '14:00:00', '16:00:00', 6, 6, 'FULL');

-- 경기 3: 진행 예정 경기
INSERT INTO matches (host_id, title, description, latitude, longitude, address, match_date, start_time, end_time, max_participants, current_participants, status) VALUES
(1, '홍대 스트리트 농구', '자유로운 분위기에서 농구 즐기실 분', 37.5563, 126.9239, '서울특별시 마포구 서교동', '2026-01-20', '18:00:00', '20:00:00', 8, 1, 'PENDING');

-- 경기 4: 취소된 경기
INSERT INTO matches (host_id, title, description, latitude, longitude, address, match_date, start_time, end_time, max_participants, current_participants, status) VALUES
(3, '부산 해운대 농구', '날씨가 안 좋아서 취소되었습니다', 35.1587, 129.1603, '부산광역시 해운대구', '2026-01-10', '15:00:00', '17:00:00', 12, 0, 'CANCELLED');

-- =====================================================
-- 5. 샘플 참가(Participation) 생성
-- =====================================================

-- 경기 1의 참가자들 (Host 포함)
INSERT INTO participations (match_id, user_id, status, joined_at) VALUES
(1, 1, 'CONFIRMED', '2026-01-05 09:00:00'), -- Host
(1, 2, 'CONFIRMED', '2026-01-06 10:30:00'),
(1, 3, 'CONFIRMED', '2026-01-07 14:20:00');

-- 경기 2의 참가자들 (정원 마감)
INSERT INTO participations (match_id, user_id, status, joined_at) VALUES
(2, 2, 'CONFIRMED', '2026-01-03 08:00:00'), -- Host
(2, 1, 'CONFIRMED', '2026-01-03 09:15:00'),
(2, 3, 'CONFIRMED', '2026-01-03 10:45:00'),
(2, 4, 'CONFIRMED', '2026-01-04 11:00:00'),
(2, 5, 'CONFIRMED', '2026-01-04 12:30:00'),
(2, 1, 'CONFIRMED', '2026-01-04 13:00:00');

-- 경기 3의 참가자 (Host만)
INSERT INTO participations (match_id, user_id, status, joined_at) VALUES
(1, 1, 'CONFIRMED', '2026-01-08 08:00:00'); -- Host

-- =====================================================
-- 6. 샘플 알림(Notification) 생성
-- =====================================================

INSERT INTO notifications (user_id, type, title, message, related_match_id, is_read) VALUES
(1, 'PARTICIPATION_CREATED', '새로운 참가 신청', 'Player1님이 "주말 농구 게임 - 강남"에 참가 신청했습니다.', 1, FALSE),
(1, 'PARTICIPATION_CREATED', '새로운 참가 신청', 'Player2님이 "주말 농구 게임 - 강남"에 참가 신청했습니다.', 1, TRUE),
(2, 'MATCH_FULL', '경기 정원 마감', '"잠실 3on3 토너먼트"가 정원 마감되었습니다!', 2, TRUE),
(1, 'MATCH_UPCOMING', '경기 임박 알림', '"주말 농구 게임 - 강남" 경기가 곧 시작됩니다.', 1, FALSE),
(3, 'MATCH_CANCELLED', '경기 취소 알림', '"부산 해운대 농구" 경기가 취소되었습니다.', 4, FALSE);
