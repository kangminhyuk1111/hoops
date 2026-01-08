-- =====================================================
-- Hoops 프로젝트 스키마 삭제 스크립트
-- Description: 모든 테이블 삭제 (개발/테스트 환경 초기화용)
-- 주의: 프로덕션 환경에서는 절대 실행하지 말 것!
-- =====================================================

-- 외래키 제약조건 비활성화 (삭제 순서 무관하게 처리)
SET FOREIGN_KEY_CHECKS = 0;

-- 테이블 삭제 (역순)
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS participations;
DROP TABLE IF EXISTS matches;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS auth_accounts;
DROP TABLE IF EXISTS users;

-- 외래키 제약조건 재활성화
SET FOREIGN_KEY_CHECKS = 1;

-- 확인 메시지
SELECT 'All tables dropped successfully!' AS status;
