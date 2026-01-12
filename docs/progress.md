# 프로젝트 진행 상황

> 이 문서는 컨텍스트 전환 시 빠르게 현재 상태를 파악하기 위한 문서입니다.
> 작업 시작/완료 시 반드시 업데이트합니다.

---

## 현재 상태

**Phase**: 4 - 부가 기능 (완료)
**브랜치**: `feat/participant-details`
**전체 진행률**: 88%

---

## 진행 중

> 현재 작업 중인 항목

| 작업 | 상태 | 담당 | 비고 |
|------|------|------|------|
| - | - | - | 대기 중 |

---

## 다음 작업 (우선순위 순)

1. **테스트 보강**
   - WireMock을 통한 카카오 API 테스트
   - Cucumber 참가 신청/취소 테스트
   - 동시성 테스트 (낙관적 락)

2. **경기 수정 API** (`PUT /api/matches/{matchId}`)
   - Host 권한 검증
   - 참가자 존재 시 수정 정책

---

## 최근 완료

| 날짜 | 작업 | PR |
|------|------|-----|
| 2026-01-12 | E2E Happy Path 테스트 추가, 호스트 승인 플로우 구현 | - |
| 2026-01-12 | Kafka 이벤트 기반 알림 시스템 | #9 |
| 2026-01-12 | 알림 조회 및 관리 API | - |
| 2026-01-12 | 경기 상태 자동 변경 스케줄러 | #8 |
| 2025-01-12 | Lombok 허용 및 가이드 문서 | #5 |
| 2025-01-12 | 문서 구조 정리, Git 컨벤션 추가 | #4 |
| 2025-01-10 | 경기 참가 신청/취소 구현 | - |
| 2025-01-10 | 카카오 OAuth 로그인 구현 | - |

---

## Phase 현황

| Phase | 내용 | 상태 |
|-------|------|------|
| 1 | 핵심 인프라 (프로젝트 셋업, 인증) | 완료 ✅ |
| 2 | 핵심 기능 (경기 CRUD, 참가) | 완료 ✅ |
| 3 | 사용자 경험 (프로필, 내 경기) | 완료 ✅ |
| 4 | 운영 필수 (경기 취소, 스케줄러, 알림) | 완료 ✅ |
| **5** | **테스트 & 배포** | **대기** |

---

## 구현된 API 목록

### Auth
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/auth/kakao` | 카카오 인증 URL 요청 |
| GET | `/api/auth/kakao/callback` | 카카오 콜백 처리 |
| POST | `/api/auth/signup` | 회원가입 완료 |
| POST | `/api/auth/refresh` | 토큰 갱신 |

### Match
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/matches` | 경기 생성 |
| GET | `/api/matches` | 경기 목록 조회 |
| GET | `/api/matches/{matchId}` | 경기 상세 조회 |
| DELETE | `/api/matches/{matchId}` | 경기 취소 |

### Participation
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/matches/{matchId}/participations` | 참가 신청 |
| DELETE | `/api/matches/{matchId}/participations/{id}` | 참가 취소 |
| GET | `/api/matches/{matchId}/participants` | 참가자 목록 |

### User
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/users/me` | 내 프로필 조회 |
| GET | `/api/users/me/participations` | 내 참가 경기 목록 |

### Location
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/locations` | 장소 추가 |

### Notification
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/notifications` | 알림 목록 조회 |
| PUT | `/api/notifications/{id}/read` | 알림 읽음 처리 |
| GET | `/api/notifications/unread-count` | 읽지 않은 알림 개수 |

---

## 참고 문서

- 전체 기능 목록: `/docs/spec/mvp-features.md`
- 프로젝트 구조: `/docs/architecture/project-structure.md`
- PRD: `/docs/prd.md`
- 아키텍처: `/docs/architecture/architecture.md`

---

## 업데이트 규칙

1. **작업 시작 시**: "진행 중" 섹션에 추가
2. **작업 완료 시**: "최근 완료"로 이동, "다음 작업"에서 제거
3. **Phase 완료 시**: Phase 현황 업데이트
