# Hoops - 농구 경기 매칭 플랫폼

> **Host 승인 기반** + **위치 기반** 농구 경기 매칭 및 관리 서비스

---

## 📖 프로젝트 개요

**Hoops**는 농구를 좋아하는 사람들이 쉽게 경기를 찾고, 신뢰할 수 있는 사람들과 함께 경기를 할 수 있도록 돕는 플랫폼입니다.

### 핵심 가치

1. **위치 기반 매칭**: 지도에서 내 주변 2km 내 경기를 한눈에 확인
2. **Host 승인 시스템**: Host가 신청자를 선택하여 신뢰할 수 있는 경기 구성
3. **신뢰도 관리**: 노쇼 방지를 위한 평점/리뷰 시스템으로 건전한 커뮤니티 유지

---

## 🎯 핵심 기능

### 1. 지도 기반 경기 검색
- 사용자 위치 기준 반경 2km 내 경기 조회
- 지도 마커로 경기 위치 시각화
- 마커 클릭 시 경기 상세 정보 확인

### 2. Host 승인 기반 매칭
- Host가 경기를 생성하고 신청자를 선택적으로 승인
- Host가 n명을 승인하면 경기 자동 확정
- 신청자의 신뢰도를 보고 판단 가능

### 3. 신뢰도 시스템
- 경기 후 상호 평가 (별점 1~5점, 리뷰)
- 노쇼 시 신뢰도 -0.5점 및 노쇼 카운트 증가
- 신뢰도 2.0 미만 또는 노쇼 3회 이상 시 자동 정지

### 4. 실시간 알림
- 신청, 승인, 경기 확정, 경기 시작 1시간 전 등
- PUSH, IN_APP, SMS 채널 지원

---

## 🏗️ 시스템 아키텍처

### Bounded Context

```
┌─────────────────────────────────────────────────────┐
│                                                     │
│  Match Context          Participation Context      │
│  (경기 관리)            (신청 관리)                │
│                                                     │
│  - Match                - Participation             │
│  - Host                 - Applicant                 │
│  - Court                - Approval Status           │
│  - Status               - Host Approval             │
│                                                     │
└──────────────┬──────────────────┬───────────────────┘
               │                  │
               │                  │
┌──────────────┴──────────────────┴───────────────────┐
│                                                     │
│  User Context           Location Context           │
│  (사용자 관리)          (위치 관리)                │
│                                                     │
│  - User                 - Court Location            │
│  - Reputation           - User Location             │
│  - No-Show Record       - Search Radius             │
│  - Rating & Review      - Distance Calculation      │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### 핵심 상태 전환

#### 신청 상태 (Participation Status)

```
PENDING_APPROVAL (승인 대기)
  ↓
  ├→ Host 승인 → APPROVED (승인됨)
  │               ↓
  │          Host가 n명 승인 → CONFIRMED (확정됨)
  │                            ↓
  │                    경기 시간 경과
  │                            ↓
  │                    ├→ 참석 → COMPLETED
  │                    └→ 미참석 → NO_SHOW
  │
  └→ Host 거부 → REJECTED (거부됨)
```

#### 경기 상태 (Match Status)

```
RECRUITING (모집 중)
  ↓
  Host가 n명 승인 완료
  ↓
CONFIRMED (확정됨)
  ↓
  경기 시간 경과
  ↓
COMPLETED (완료됨)
```

---

## 📱 핵심 유저 플로우

### 해피 패스 (Happy Path)

```
1. 메인 화면 진입
   → 지도에 반경 2km 내 경기 표시

2. 지도 마커 클릭
   → 경기 정보 팝업 확인

3. "신청하기" 클릭
   → PENDING_APPROVAL 상태
   → Host에게 알림

4. Host 승인
   → APPROVED 상태
   → 신청자에게 승인 알림

5. Host가 n명 승인 완료
   → 경기 자동 CONFIRMED
   → 모든 참여자에게 확정 알림

6. 경기 참석
   → COMPLETED 상태
   → 상호 평가 가능
```

---

## 📚 문서 구조

### 필수 문서

| 문서 | 설명 | 경로 |
|------|------|------|
| **용어 사전** | 팀 공통 용어 정의 | [docs/ubiquitous_language.md](docs/ubiquitous_language.md) |
| **비즈니스 규칙** | 도메인 제약과 규칙 | [docs/business_rules.md](docs/business_rules.md) |
| **상태 다이어그램** | 경기/신청 상태 전환 흐름 | [docs/state_diagrams.md](docs/state_diagrams.md) |
| **개념 가이드** | 헷갈리는 개념 비교 설명 | [docs/concept_guide.md](docs/concept_guide.md) |
| **유저 플로우** | 핵심 해피 패스 정의 | [docs/user_flow.md](docs/user_flow.md) |
| **커뮤니케이션 가이드** | 팀 대화 방식 | [docs/communication_guide.md](docs/communication_guide.md) |

---

## 🔑 핵심 비즈니스 규칙

### 절대 깨져선 안 되는 규칙

```
1. 경기 정원은 n명 (변경 불가)
2. Host는 필수 (1명)
3. (userId, matchId)는 유일 (중복 신청 불가)
4. 신뢰도 < 2.0 또는 노쇼 3회 → 자동 정지
5. Host가 n명 승인하면 경기 자동 CONFIRMED
6. 경기가 CONFIRMED되면 승인된 신청도 자동 CONFIRMED
```

### Host 승인 규칙

```
- Host만 신청 승인/거부 가능
- Host는 최대 (n-1)명까지만 승인 가능 (본인 포함 n명)
- 승인된 참여자는 다시 거부 불가 (APPROVED 이후)
- Host가 n명을 승인하면 경기 자동 확정
```

### 위치 기반 검색 규칙

```
- 기본 검색 반경: 2km (사용자 위치 기준)
- GPS 권한 필요
- 직선 거리 기준 계산
- 지도 마커로 경기 위치 표시
```

---

## 🛠️ 기술 스택 (예상)

### Backend
- Java / Spring Boot
- JPA / Hibernate
- PostgreSQL (+ PostGIS for geospatial)
- Redis (캐싱)

### Frontend
- React / React Native
- Google Maps API / Naver Maps API
- WebSocket (실시간 알림)

### Infrastructure
- AWS / GCP
- Docker / Kubernetes
- Firebase Cloud Messaging (PUSH)

---

## 🚀 시작하기

### 문서 읽는 순서

```
1. README.md (현재 파일) - 프로젝트 전체 이해
2. docs/ubiquitous_language.md - 용어 숙지
3. docs/user_flow.md - 핵심 플로우 이해
4. docs/state_diagrams.md - 상태 전환 이해
5. docs/business_rules.md - 비즈니스 규칙 숙지
6. docs/concept_guide.md - 헷갈리는 개념 확인
7. docs/communication_guide.md - 팀 대화 방식
```

### 개발 전 체크리스트

```
□ 모든 문서를 읽었는가?
□ 용어 사전을 이해했는가?
□ 핵심 비즈니스 규칙을 알고 있는가?
□ 상태 전환 흐름을 이해했는가?
□ Host 승인 시스템을 이해했는가?
□ 위치 기반 검색 로직을 이해했는가?
```

---

## 👥 팀 협업 원칙

### 공통 언어 사용

- 데이터 관점 표현 금지 (❌ "레코드 추가" → ✅ "신청 기록 생성")
- 시스템 관점 표현 금지 (❌ "이벤트 처리" → ✅ "Host에게 알림 전송")
- 용어 사전 기준으로 대화

### 상태 구분 명확히

- **승인 (APPROVED)**: Host가 개별 신청을 승인
- **확정 (CONFIRMED)**: Host가 n명 승인 완료 후 경기 자동 확정
- **신청자 (Applicant)**: PENDING_APPROVAL 또는 APPROVED 상태
- **참여자 (Participant)**: CONFIRMED 상태 (n명)

---

## 📞 문의 및 기여

- 이슈: [GitHub Issues](링크)
- 문서 개선: Pull Request 환영
- 문의: [이메일 주소]

---

**Hoops와 함께 즐거운 농구 경기를 만들어보세요! 🏀**