# Hoops MVP 기능 목록

> 마지막 업데이트: 2025-01-10
> 현재 브랜치: `feat/participation`

## 개요

농구 경기 매칭 플랫폼 Hoops의 MVP(Minimum Viable Product) 구현을 위한 기능 목록입니다.
각 기능의 구현 상태를 체크리스트로 관리합니다.

---

## 1. 인증 (Auth)

### 1.1 카카오 OAuth 로그인
- [x] 카카오 인증 URL 요청 API (`GET /api/auth/kakao`)
- [x] 카카오 콜백 처리 API (`GET /api/auth/kakao/callback`)
- [x] 카카오 API 연동 (KakaoAuthClient)
- [x] 기존 회원 로그인 처리
- [x] 신규 회원 임시 토큰 발급

### 1.2 회원가입
- [x] 회원가입 완료 API (`POST /api/auth/signup`)
- [x] 닉네임 설정
- [x] User 도메인 생성
- [x] AuthAccount 연동

### 1.3 토큰 관리
- [x] JWT Access Token 발급
- [x] JWT Refresh Token 발급
- [x] 토큰 갱신 API (`POST /api/auth/refresh`)
- [x] SecurityContext 연동

### 1.4 테스트
- [x] AuthService 단위 테스트
- [x] JwtTokenProvider 테스트
- [ ] 카카오 API WireMock 테스트
- [ ] 인증 플로우 통합 테스트

---

## 2. 경기 (Match)

### 2.1 경기 생성
- [x] 경기 생성 API (`POST /api/matches`)
- [x] Match 도메인 모델
- [x] 정책 검증 (날짜, 시간, 인원수)
- [x] CreateMatchCommand
- [x] MatchCreator UseCase

### 2.2 경기 조회
- [x] 경기 목록 조회 API (`GET /api/matches`)
- [x] 위치 기반 필터링 (위도/경도/반경)
- [x] 경기 상세 조회 API (`GET /api/matches/{matchId}`)
- [x] MatchFinder UseCase

### 2.3 경기 수정/취소
- [ ] 경기 수정 API (`PUT /api/matches/{matchId}`)
- [ ] 경기 취소 API (`DELETE /api/matches/{matchId}`)
- [ ] 호스트 권한 검증
- [ ] 참가자 존재 시 취소 정책

### 2.4 경기 상태 관리
- [x] MatchStatus Enum (PENDING, IN_PROGRESS, ENDED, CANCELLED)
- [ ] 상태 변경 스케줄러 (PENDING → IN_PROGRESS → ENDED)
- [ ] 상태 변경 이벤트 발행

### 2.5 테스트
- [x] Match 도메인 테스트
- [x] MatchFinder 서비스 테스트
- [ ] MatchCreator 서비스 테스트
- [x] 경기 생성 Cucumber 테스트
- [x] 경기 목록 조회 Cucumber 테스트
- [x] 경기 상세 조회 Cucumber 테스트

---

## 3. 경기 참가 (Participation)

### 3.1 참가 신청
- [x] 참가 신청 API (`POST /api/matches/{matchId}/participations`)
- [x] Participation 도메인 모델
- [x] ParticipationStatus Enum (PENDING, CONFIRMED, CANCELLED)
- [x] 참가 유효성 검증 (호스트 불가, 정원 체크, 중복 불가)
- [x] 낙관적 락 적용 (@Version)
- [x] 재시도 로직 (@Retryable)

### 3.2 참가 취소
- [x] 참가 취소 API (`DELETE /api/matches/{matchId}/participations/{participationId}`)
- [x] 본인 확인 로직
- [x] 취소 가능 상태 검증
- [x] Match 참가자 수 감소

### 3.3 참가자 조회
- [ ] 참가자 목록 조회 API (`GET /api/matches/{matchId}/participants`)
- [ ] 참가자 상세 정보 (닉네임, 프로필 등)

### 3.4 테스트
- [x] Participation 도메인 테스트
- [x] ParticipationService 테스트
- [x] ParticipationCancellation 테스트
- [ ] 참가 신청 Cucumber 테스트
- [ ] 참가 취소 Cucumber 테스트
- [ ] 동시성 테스트 (낙관적 락)

---

## 4. 장소 (Location)

### 4.1 장소 관리
- [x] 장소 추가 API (`POST /api/locations`)
- [x] Location 도메인 모델
- [ ] 장소 목록 조회 API (`GET /api/locations`)
- [ ] 장소 검색 API (이름/주소 기반)

### 4.2 테스트
- [ ] Location 도메인 테스트
- [ ] 장소 추가 통합 테스트

---

## 5. 사용자 (User)

### 5.1 사용자 프로필
- [x] User 도메인 모델
- [ ] 프로필 조회 API (`GET /api/users/{userId}`)
- [ ] 프로필 수정 API (`PUT /api/users/{userId}`)
- [ ] 내 정보 조회 API (`GET /api/users/me`)

### 5.2 참가 이력
- [ ] 내 참가 경기 목록 API (`GET /api/users/me/participations`)
- [ ] 내가 생성한 경기 목록 API (`GET /api/users/me/matches`)

### 5.3 테스트
- [ ] User 도메인 테스트
- [ ] 프로필 조회 통합 테스트

---

## 6. 알림 (Notification)

### 6.1 알림 발송
- [ ] Notification 도메인 모델
- [ ] 알림 생성 로직
- [ ] 알림 타입 정의 (참가 신청, 경기 시작 등)

### 6.2 알림 조회
- [ ] 알림 목록 조회 API (`GET /api/notifications`)
- [ ] 알림 읽음 처리 API (`PUT /api/notifications/{id}/read`)
- [ ] 읽지 않은 알림 개수 API

### 6.3 테스트
- [ ] Notification 도메인 테스트
- [ ] 알림 발송 통합 테스트

---

## 7. 이벤트 (Kafka)

### 7.1 이벤트 발행
- [ ] 경기 생성 이벤트
- [ ] 참가 신청 이벤트
- [ ] 참가 취소 이벤트
- [ ] 경기 상태 변경 이벤트

### 7.2 이벤트 구독
- [ ] 알림 생성 Consumer
- [ ] 이벤트 처리 로직

### 7.3 테스트
- [ ] Kafka 이벤트 발행 테스트
- [ ] Consumer 통합 테스트

---

## 8. 인프라 및 공통

### 8.1 예외 처리
- [x] BusinessException 기본 클래스
- [x] GlobalExceptionHandler
- [x] 도메인별 커스텀 예외
- [x] ErrorResponse DTO

### 8.2 보안
- [x] JWT 인증 필터
- [x] SecurityConfig
- [ ] Rate Limiting
- [ ] CORS 설정 검토

### 8.3 문서화
- [x] 아키텍처 문서 (`docs/architecture/`)
- [x] 코딩 컨벤션 (`docs/convention/`)
- [x] DB 스키마 (`docs/spec/schema.md`)
- [x] API 명세서 (`docs/api/`)
- [x] 시퀀스 다이어그램 (`docs/sequence/`)
- [ ] API Swagger/OpenAPI 연동

### 8.4 테스트 인프라
- [x] IntegrationTestSupport 기본 클래스
- [x] Cucumber 설정
- [x] H2 인메모리 DB
- [ ] Testcontainers MySQL
- [ ] WireMock 설정

---

## 진행 현황 요약

| 카테고리 | 완료 | 미완료 | 진행률 |
|----------|------|--------|--------|
| 인증 (Auth) | 13 | 2 | 87% |
| 경기 (Match) | 14 | 7 | 67% |
| 참가 (Participation) | 11 | 4 | 73% |
| 장소 (Location) | 2 | 4 | 33% |
| 사용자 (User) | 1 | 6 | 14% |
| 알림 (Notification) | 0 | 7 | 0% |
| 이벤트 (Kafka) | 0 | 7 | 0% |
| 인프라/공통 | 11 | 5 | 69% |
| **전체** | **52** | **42** | **55%** |

---

## MVP 핵심 기능 (필수)

MVP 출시를 위한 최소 필수 기능입니다.

### Phase 1: 핵심 플로우 (현재 완료)
- [x] 카카오 로그인/회원가입
- [x] 경기 생성
- [x] 경기 목록/상세 조회
- [x] 경기 참가 신청/취소
- [x] 장소 추가

### Phase 2: 사용자 경험 개선
- [ ] 내 프로필 조회
- [ ] 내 참가 경기 목록
- [ ] 참가자 목록 조회

### Phase 3: 운영 필수
- [ ] 경기 상태 자동 변경 (스케줄러)
- [ ] 경기 취소 기능

### Phase 4: 부가 기능
- [ ] 알림 기능
- [ ] 이벤트 기반 아키텍처 (Kafka)

---

## 다음 작업 권장 순서

1. **테스트 보강** - WireMock 적용, Cucumber 테스트 추가
2. **사용자 프로필** - 내 정보 조회/수정 API
3. **경기 상태 스케줄러** - 자동 상태 전환
4. **알림 기능** - 기본 알림 발송/조회

---

## 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|----------|--------|
| 2025-01-10 | 최초 작성 | Claude |
