# 사용자 플로우 분석

> 마지막 업데이트: 2026-01-12
> 관련 문서: [MVP 기능](./mvp-features.md) | [API 명세](../api/)

---

## Happy Path (E2E 핵심 플로우)

경기 참가의 핵심 성공 시나리오입니다. 이 플로우는 Cucumber E2E 테스트로 검증됩니다.

### 플로우 단계

| 단계 | 액터 | 행위 | API | 결과 |
|------|------|------|-----|------|
| 1 | 사용자 | 로그인 (없으면 회원가입) | `POST /api/auth/signup` | JWT 토큰 발급 |
| 2 | 사용자 | 근처 매치 목록 조회 (반경 N km) | `GET /api/matches?lat=&lng=&distance=` | 매치 목록 반환 |
| 3 | 사용자 | 매치 상세 조회 | `GET /api/matches/{matchId}` | 매치 상세 정보 |
| 4 | 사용자 | 매치 참가 신청 | `POST /api/matches/{matchId}/participations` | 상태: PENDING |
| 5 | 사용자 | 승인 대기 | - | 호스트 승인 대기 |
| 6 | 호스트 | 신청자 목록 조회 | `GET /api/matches/{matchId}/participants` | 신청자 목록 |
| 7 | 호스트 | 참가 승인 | `PUT /api/.../participations/{id}/approve` | 상태: CONFIRMED |
| 8 | 시스템 | 매치 시작 (스케줄러) | 자동 처리 | 상태: IN_PROGRESS |

### 테스트 파일

- Feature: `src/test/resources/features/e2e-happy-path.feature`
- StepDefs: `src/test/java/com/hoops/acceptance/steps/E2EHappyPathStepDefs.java`

---

## 플로우 단계별 상세

### 1단계: 로그인/회원가입

| 항목 | 상태 | 설명 |
|------|------|------|
| API | 구현됨 | 카카오 OAuth + JWT |
| Cucumber 테스트 | 작성됨 | auth-login.feature, auth-signup.feature |

**관련 API**:
- `GET /api/auth/kakao` - 카카오 인증 URL 조회
- `GET /api/auth/kakao/callback` - 카카오 콜백 처리
- `POST /api/auth/signup` - 회원가입 완료
- `POST /api/auth/refresh` - 토큰 갱신

---

### 2단계: 매치 목록 조회

| 항목 | 상태 | 설명 |
|------|------|------|
| API | 구현됨 | `GET /api/matches` |
| Cucumber 테스트 | 작성됨 | 위치 기반 필터링 포함 |

**요청 파라미터**:
- `latitude`: 사용자 위도
- `longitude`: 사용자 경도
- `distance`: 검색 반경 (km)

---

### 3단계: 매치 상세 조회

| 항목 | 상태 | 설명 |
|------|------|------|
| API | 구현됨 | `GET /api/matches/{matchId}` |
| Cucumber 테스트 | 작성됨 | match-detail.feature |

**추가 조회**:
- `GET /api/matches/{matchId}/participants` - 참가자 목록

---

### 4단계: 참가 신청

| 항목 | 상태 | 설명 |
|------|------|------|
| API | 구현됨 | `POST /api/matches/{matchId}/participations` |
| Cucumber 테스트 | 작성됨 | participation.feature |

**신청 후 상태**: PENDING (호스트 승인 대기)

> 참고: 2026-01-12 이전에는 자동 승인(즉시 CONFIRMED) 방식이었으나, 현재는 호스트 승인 플로우로 변경됨

---

### 5단계: 승인 대기

사용자는 호스트가 승인할 때까지 PENDING 상태로 대기합니다.

**알림**: 호스트에게 참가 신청 알림 발송 (Kafka 이벤트)

---

### 6단계: 신청자 목록 조회 (호스트)

| 항목 | 상태 | 설명 |
|------|------|------|
| API | 구현됨 | `GET /api/matches/{matchId}/participants` |
| 응답 | 닉네임, 프로필 이미지 포함 | ParticipantDetailResponse |

---

### 7단계: 참가 승인 (호스트)

| 항목 | 상태 | 설명 |
|------|------|------|
| API | 구현됨 | `PUT /api/.../participations/{id}/approve` |
| Cucumber 테스트 | 작성됨 | participation-approval.feature |

**승인 후 상태**: CONFIRMED

**알림**: 신청자에게 승인 알림 발송 (Kafka 이벤트)

---

### 8단계: 매치 시작 (스케줄러)

| 항목 | 상태 | 설명 |
|------|------|------|
| 스케줄러 | 구현됨 | MatchStatusScheduler |
| 동작 | 매치 시작 시간 도달 시 자동 상태 변경 | PENDING -> IN_PROGRESS |

**상태 전이**:
- 매치 시작 시간 -> IN_PROGRESS
- 매치 종료 시간 -> ENDED

---

## ParticipationStatus 상태 전이

| 현재 상태 | 이벤트 | 다음 상태 |
|-----------|--------|-----------|
| (없음) | 참가 신청 | PENDING |
| PENDING | 호스트 승인 | CONFIRMED |
| PENDING | 호스트 거절 | REJECTED |
| CONFIRMED | 참가자 취소 | CANCELLED |
| CONFIRMED | 매치 취소 | MATCH_CANCELLED |

**상태 정의**:
| 상태 | 설명 |
|------|------|
| PENDING | 참가 신청 대기 (호스트 승인 대기) |
| CONFIRMED | 참가 확정 |
| REJECTED | 호스트에 의해 거절됨 |
| CANCELLED | 참가자 본인이 취소 |
| MATCH_CANCELLED | 경기 자체가 취소됨 |

---

## MatchStatus 상태 전이

| 현재 상태 | 이벤트 | 다음 상태 |
|-----------|--------|-----------|
| (없음) | 매치 생성 | PENDING |
| PENDING | 시작 시간 도달 (스케줄러) | IN_PROGRESS |
| IN_PROGRESS | 종료 시간 도달 (스케줄러) | ENDED |
| PENDING | 호스트 취소 | CANCELLED |

---

## 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|----------|--------|
| 2026-01-12 | E2E Happy Path 테스트 추가, 자동 승인 → 호스트 승인 플로우로 변경 | Claude |
| 2026-01-12 | Happy Path 섹션 추가, 시각적 다이어그램 텍스트 기반으로 변경 | Claude |
| 2026-01-12 | 최초 작성 - 사용자 플로우 분석 | Claude |
