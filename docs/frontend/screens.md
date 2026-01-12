# 프론트엔드 화면 목록

> 마지막 업데이트: 2026-01-12
> 관련 문서: [User Flow](../spec/user-flow.md) | [API 명세](../api/) | [Progress](../progress.md)

---

## 개요

E2E Happy Path 테스트와 Cucumber 시나리오를 기반으로 도출한 프론트엔드 화면 목록입니다.
Next.js 기반으로 구현 예정입니다.

---

## 화면 목록

### 1. 인증 (Auth)

| 화면 | 경로 | 설명 | 관련 API |
|------|------|------|----------|
| 로그인 | `/login` | 카카오 OAuth 로그인 버튼 | `GET /api/auth/kakao` |
| 회원가입 | `/signup` | 닉네임 입력 (카카오 인증 후) | `POST /api/auth/signup` |

**로그인 플로우**:
1. 사용자가 카카오 로그인 버튼 클릭
2. 카카오 인증 페이지로 리다이렉트
3. 인증 완료 후 콜백 처리
4. 기존 회원: 토큰 발급 후 홈으로 이동
5. 신규 회원: 회원가입 페이지로 이동

---

### 2. 경기 (Match)

| 화면 | 경로 | 설명 | 관련 API |
|------|------|------|----------|
| 경기 목록 | `/` (홈) | 위치 기반 경기 목록, 지도 또는 리스트 | `GET /api/matches` |
| 경기 상세 | `/matches/[id]` | 경기 정보, 호스트, 장소, 참가 신청 버튼 | `GET /api/matches/{id}` |
| 경기 생성 | `/matches/new` | 경기 생성 폼 | `POST /api/matches` |

**경기 목록 페이지 요소**:
- 현재 위치 기반 검색 (거리 필터)
- 경기 카드 (제목, 날짜, 장소, 현재 인원)
- 상태 필터 (PENDING, IN_PROGRESS 등)

**경기 상세 페이지 요소**:
- 경기 기본 정보 (제목, 설명, 일시)
- 호스트 정보 (닉네임, 프로필)
- 장소 정보 (이름, 주소, 지도)
- 현재 참가자 목록
- 참가 신청 버튼 (비로그인 시 로그인 유도)
- 경기 취소 버튼 (호스트 전용)

**경기 생성 페이지 요소**:
- 제목 입력
- 설명 입력
- 장소 선택 (검색 또는 지도 선택)
- 날짜/시간 선택 (시작, 종료)
- 최대 인원 설정 (최소 4명)

---

### 3. 참가 관리 (Participation)

| 화면 | 경로 | 설명 | 관련 API |
|------|------|------|----------|
| 참가자 관리 | `/matches/[id]/participants` | 호스트 전용, 승인/거절 | `GET /api/matches/{id}/participants`, `PUT .../approve`, `PUT .../reject` |

**참가자 관리 페이지 요소 (호스트 전용)**:
- 참가 신청자 목록 (PENDING 상태)
- 각 신청자 정보 (닉네임, 프로필 이미지)
- 승인/거절 버튼
- 확정된 참가자 목록 (CONFIRMED 상태)

---

### 4. 마이페이지 (My Page)

| 화면 | 경로 | 설명 | 관련 API |
|------|------|------|----------|
| 내 프로필 | `/mypage` | 프로필 조회 및 수정 | `GET /api/users/me`, `PUT /api/users/me` |
| 내 참가 경기 | `/mypage/participations` | 내가 참가 신청한 경기 목록 | `GET /api/users/me/participations` |
| 주최한 경기 | `/mypage/hosted` | 내가 호스팅한 경기 목록 | `GET /api/users/me/matches` |

**내 프로필 페이지 요소**:
- 프로필 이미지 (카카오 연동)
- 닉네임 (수정 가능)
- 가입일

**내 참가 경기 페이지 요소**:
- 참가 상태별 필터 (PENDING, CONFIRMED, CANCELLED)
- 경기 카드 (제목, 날짜, 장소, 참가 상태)
- 참가 취소 버튼 (PENDING, CONFIRMED 상태일 때)

**주최한 경기 페이지 요소**:
- 경기 상태별 필터 (PENDING, IN_PROGRESS, ENDED, CANCELLED)
- 경기 카드 (제목, 날짜, 현재 인원)
- 참가자 관리 바로가기

---

### 5. 알림 (Notification)

| 화면 | 경로 | 설명 | 관련 API |
|------|------|------|----------|
| 알림 목록 | `/notifications` | 알림 목록 및 읽음 처리 | `GET /api/notifications`, `PUT /api/notifications/{id}/read` |

**알림 페이지 요소**:
- 알림 목록 (최신순)
- 읽음/안읽음 표시
- 전체 읽음 처리 버튼
- 알림 클릭 시 관련 페이지로 이동

**알림 유형**:
| 타입 | 메시지 예시 | 이동 경로 |
|------|------------|-----------|
| PARTICIPATION_REQUEST | "OOO님이 참가를 신청했습니다" | 참가자 관리 페이지 |
| PARTICIPATION_APPROVED | "참가가 승인되었습니다" | 경기 상세 페이지 |
| PARTICIPATION_REJECTED | "참가가 거절되었습니다" | - |
| MATCH_CANCELLED | "경기가 취소되었습니다" | - |

---

### 6. 공통 컴포넌트 (Shared)

| 컴포넌트 | 설명 | 위치 |
|----------|------|------|
| Header | 로고, 알림 아이콘, 프로필 메뉴 | 전역 |
| BottomNav | 홈, 검색, 경기생성, 알림, 마이페이지 | 모바일 전역 |
| MatchCard | 경기 요약 카드 | 목록 페이지들 |
| ParticipantCard | 참가자 정보 카드 | 참가자 관리 |
| NotificationBadge | 읽지 않은 알림 개수 | 헤더 |

---

## 라우팅 구조 (Next.js App Router)

```
app/
├── page.tsx                      # 홈 (경기 목록)
├── login/
│   └── page.tsx                  # 로그인
├── signup/
│   └── page.tsx                  # 회원가입
├── matches/
│   ├── new/
│   │   └── page.tsx              # 경기 생성
│   └── [id]/
│       ├── page.tsx              # 경기 상세
│       └── participants/
│           └── page.tsx          # 참가자 관리 (호스트)
├── mypage/
│   ├── page.tsx                  # 내 프로필
│   ├── participations/
│   │   └── page.tsx              # 내 참가 경기
│   └── hosted/
│       └── page.tsx              # 주최한 경기
├── notifications/
│   └── page.tsx                  # 알림 목록
└── auth/
    └── kakao/
        └── callback/
            └── page.tsx          # 카카오 콜백 처리
```

---

## E2E Happy Path와 화면 매핑

| E2E 단계 | 화면 | 액션 |
|----------|------|------|
| 1. 회원가입/로그인 | `/login`, `/signup` | 카카오 인증 후 토큰 저장 |
| 2. 근처 경기 목록 조회 | `/` (홈) | 위치 권한 요청, 경기 목록 표시 |
| 3. 경기 상세 조회 | `/matches/[id]` | 경기 카드 클릭 |
| 4. 참가 신청 | `/matches/[id]` | 참가 신청 버튼 클릭 |
| 5. 승인 대기 | `/mypage/participations` | 상태: PENDING 확인 |
| 6. 신청자 목록 조회 (호스트) | `/matches/[id]/participants` | 알림 클릭 또는 경기 관리 |
| 7. 참가 승인 (호스트) | `/matches/[id]/participants` | 승인 버튼 클릭 |
| 8. 참가 확정 확인 | `/mypage/participations` | 상태: CONFIRMED 확인 |

---

## 인증 상태별 접근 제어

| 화면 | 비로그인 | 로그인 | 호스트 전용 |
|------|----------|--------|-------------|
| 로그인 | O | 리다이렉트 (홈) | - |
| 회원가입 | O (임시토큰 필요) | 리다이렉트 (홈) | - |
| 경기 목록 | O | O | - |
| 경기 상세 | O | O | - |
| 경기 생성 | 리다이렉트 (로그인) | O | - |
| 참가 신청 | 리다이렉트 (로그인) | O | - |
| 참가자 관리 | 리다이렉트 (로그인) | 403 | O |
| 마이페이지 | 리다이렉트 (로그인) | O | - |
| 알림 | 리다이렉트 (로그인) | O | - |

---

## 구현 우선순위

### Phase 1: 핵심 플로우 (MVP)
1. 로그인/회원가입
2. 경기 목록 (홈)
3. 경기 상세
4. 참가 신청

### Phase 2: 호스트 기능
5. 경기 생성
6. 참가자 관리 (승인/거절)

### Phase 3: 마이페이지
7. 내 프로필
8. 내 참가 경기
9. 주최한 경기

### Phase 4: 부가 기능
10. 알림
11. 프로필 수정

---

## 기술 스택 (예정)

| 분류 | 기술 |
|------|------|
| Framework | Next.js 14+ (App Router) |
| Styling | Tailwind CSS |
| State Management | Zustand 또는 React Query |
| HTTP Client | Axios 또는 fetch |
| Map | Kakao Maps SDK |
| Auth | NextAuth.js (Kakao Provider) |

---

## 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|----------|--------|
| 2026-01-12 | 최초 작성 - E2E 기반 화면 목록 도출 | Claude |
