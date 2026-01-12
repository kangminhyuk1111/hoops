# 프로젝트 구조

> 마지막 업데이트: 2026-01-12

## 개요

Hoops 프로젝트는 **Hexagonal Architecture + DDD** 패턴을 적용한 농구 경기 매칭 플랫폼입니다.

---

## 디렉토리 구조

```
src/main/java/com/hoops/
├── auth/                    # 인증 도메인
├── user/                    # 사용자 도메인
├── match/                   # 경기 도메인
├── participation/           # 경기 참가 도메인
├── location/                # 장소 도메인
├── notification/            # 알림 도메인
└── common/                  # 공통 기능
    ├── config/              # 설정 (Kafka, Retry 등)
    ├── event/               # 이벤트 클래스
    └── exception/           # 예외 클래스
```

---

## 도메인별 패키지 구조

각 도메인은 Hexagonal Architecture 패턴에 따라 다음 구조를 가집니다:

```
{domain}/
├── adapter/
│   ├── in/web/              # 인바운드 어댑터 (Controller)
│   │   ├── {Entity}Controller.java
│   │   └── dto/
│   │       ├── {Request}Request.java
│   │       └── {Response}Response.java
│   └── out/                 # 아웃바운드 어댑터
│       ├── jpa/
│       ├── mapper/
│       └── adapter/
├── application/
│   ├── port/
│   │   ├── in/              # 인바운드 포트 (UseCase)
│   │   └── out/             # 아웃바운드 포트 (Repository 인터페이스)
│   ├── service/             # 비즈니스 로직 구현
│   ├── exception/           # 도메인별 예외
│   └── scheduler/           # 스케줄러 (선택)
├── domain/                  # 도메인 모델 (Pure POJO)
│   ├── {Entity}.java
│   └── repository/          # 리포지토리 포트
└── infrastructure/
    ├── {Entity}Entity.java  # JPA 엔티티
    ├── jpa/                 # Spring Data JPA
    ├── mapper/              # Entity ↔ Domain 변환
    ├── adapter/             # 포트 구현체
    └── kafka/               # Kafka 관련 (선택)
```

---

## 도메인별 상세

### 1. Auth (인증)

**역할**: 카카오 OAuth 로그인, JWT 토큰 관리

| 구성요소 | 파일 | 설명 |
|----------|------|------|
| Controller | `AuthController` | 인증 API 엔드포인트 |
| UseCase | `KakaoLoginUseCase` | 카카오 로그인 처리 |
| UseCase | `SignupUseCase` | 회원가입 처리 |
| UseCase | `RefreshTokenUseCase` | 토큰 갱신 |
| Domain | `AuthAccount` | 인증 계정 모델 |
| Adapter | `KakaoOAuthClient` | 카카오 API 통신 |
| Adapter | `JwtTokenProviderImpl` | JWT 토큰 생성/검증 |

**API 엔드포인트**:
```
GET  /api/auth/kakao           - 카카오 인증 URL 요청
GET  /api/auth/kakao/callback  - 카카오 콜백 처리
POST /api/auth/signup          - 회원가입 완료
POST /api/auth/refresh         - 토큰 갱신
```

---

### 2. Match (경기)

**역할**: 경기 생성, 조회, 취소, 상태 관리

| 구성요소 | 파일 | 설명 |
|----------|------|------|
| Controller | `MatchController` | 경기 API 엔드포인트 |
| UseCase | `CreateMatchUseCase` | 경기 생성 |
| UseCase | `MatchQueryUseCase` | 경기 조회 |
| UseCase | `CancelMatchUseCase` | 경기 취소 |
| UseCase | `UpdateMatchStatusUseCase` | 상태 변경 |
| Domain | `Match` | 경기 도메인 모델 |
| Scheduler | `MatchStatusScheduler` | 상태 자동 변경 |

**Domain Model**:
```java
Match {
  id, version, hostId, hostNickname, title, description,
  latitude, longitude, address, matchDate, startTime, endTime,
  maxParticipants, currentParticipants, status
}

MatchStatus: PENDING, CONFIRMED, FULL, IN_PROGRESS, ENDED, CANCELLED
```

**API 엔드포인트**:
```
POST   /api/matches              - 경기 생성
GET    /api/matches              - 경기 목록 조회 (위치 기반)
GET    /api/matches/{matchId}    - 경기 상세 조회
DELETE /api/matches/{matchId}    - 경기 취소
```

---

### 3. Participation (경기 참가)

**역할**: 경기 참가 신청/취소, 참가자 관리

| 구성요소 | 파일 | 설명 |
|----------|------|------|
| Controller | `ParticipationController` | 참가 API 엔드포인트 |
| UseCase | `ParticipateInMatchUseCase` | 참가 신청 |
| UseCase | `CancelParticipationUseCase` | 참가 취소 |
| UseCase | `GetMatchParticipantsUseCase` | 참가자 목록 조회 |
| Domain | `Participation` | 참가 도메인 모델 |
| Adapter | `ParticipationEventPublisher` | Kafka 이벤트 발행 |

**Domain Model**:
```java
Participation {
  id, matchId, userId, status, joinedAt
}

ParticipationStatus: PENDING, CONFIRMED, CANCELLED
```

**특징**:
- 낙관적 락 (`@Version`) 적용
- 재시도 로직 (`@Retryable`) 적용
- Kafka 이벤트 발행 (참가/취소 시)

**API 엔드포인트**:
```
POST   /api/matches/{matchId}/participations                    - 참가 신청
DELETE /api/matches/{matchId}/participations/{participationId}  - 참가 취소
GET    /api/matches/{matchId}/participants                      - 참가자 목록
```

---

### 4. Location (장소)

**역할**: 경기 장소 관리

| 구성요소 | 파일 | 설명 |
|----------|------|------|
| Controller | `LocationController` | 장소 API 엔드포인트 |
| UseCase | `CreateLocationUseCase` | 장소 추가 |
| Domain | `Location` | 장소 도메인 모델 |

**Domain Model**:
```java
Location {
  id, userId, alias, latitude, longitude, address
}
```

**API 엔드포인트**:
```
POST /api/locations  - 장소 추가
```

---

### 5. User (사용자)

**역할**: 사용자 프로필 관리

| 구성요소 | 파일 | 설명 |
|----------|------|------|
| Controller | `UserController` | 사용자 API 엔드포인트 |
| UseCase | `GetMyProfileUseCase` | 프로필 조회 |
| Domain | `User` | 사용자 도메인 모델 |

**Domain Model**:
```java
User {
  id, email, nickname, profileImage, rating, totalMatches
}
```

**API 엔드포인트**:
```
GET /api/users/me                 - 내 프로필 조회
GET /api/users/me/participations  - 내 참가 경기 목록
```

---

### 6. Notification (알림)

**역할**: 알림 생성, 조회, 읽음 처리

| 구성요소 | 파일 | 설명 |
|----------|------|------|
| Controller | `NotificationController` | 알림 API 엔드포인트 |
| UseCase | `GetNotificationsUseCase` | 알림 목록 조회 |
| UseCase | `MarkNotificationAsReadUseCase` | 읽음 처리 |
| UseCase | `GetUnreadCountUseCase` | 읽지 않은 개수 |
| UseCase | `CreateNotificationUseCase` | 알림 생성 |
| Domain | `Notification` | 알림 도메인 모델 |
| Consumer | `NotificationEventConsumer` | Kafka 이벤트 수신 |

**Domain Model**:
```java
Notification {
  id, userId, type, title, message, relatedMatchId, isRead, createdAt
}

NotificationType: PARTICIPATION_CREATED, PARTICIPATION_CANCELLED,
                  MATCH_UPCOMING, MATCH_CANCELLED, MATCH_FULL
```

**API 엔드포인트**:
```
GET /api/notifications              - 알림 목록 조회
PUT /api/notifications/{id}/read    - 알림 읽음 처리
GET /api/notifications/unread-count - 읽지 않은 알림 개수
```

---

### 7. Common (공통)

**역할**: 공통 설정, 예외 처리, 이벤트

| 패키지 | 주요 파일 | 설명 |
|--------|----------|------|
| `config` | `KafkaConfig` | Kafka Producer/Consumer 설정 |
| `config` | `RetryConfig` | Spring Retry 설정 |
| `event` | `ParticipationCreatedEvent` | 참가 신청 이벤트 |
| `event` | `ParticipationCancelledEvent` | 참가 취소 이벤트 |
| `exception` | `BusinessException` | 비즈니스 예외 기본 클래스 |
| `exception` | `DomainException` | 도메인 예외 기본 클래스 |
| `exception` | `GlobalExceptionHandler` | 전역 예외 처리 |

---

## 이벤트 플로우

```
┌──────────────────┐      ┌─────────────┐      ┌────────────────────┐
│ ParticipationSvc │─────▶│    Kafka    │─────▶│ NotificationConsumer│
│ (Event Producer) │      │(participation│      │                    │
└──────────────────┘      │   -events)  │      └─────────┬──────────┘
                          └─────────────┘                │
                                                         ▼
                                               ┌──────────────────┐
                                               │NotificationService│
                                               │  → DB 저장        │
                                               └──────────────────┘
```

**발행 이벤트**:
- `ParticipationCreatedEvent`: 참가 신청 시
- `ParticipationCancelledEvent`: 참가 취소 시

---

## 예외 계층

```
RuntimeException
└── BusinessException (abstract)
    ├── DomainException (도메인 규칙 위반)
    │   ├── MatchNotFoundException
    │   ├── ParticipationNotFoundException
    │   ├── NotificationNotFoundException
    │   └── ...
    └── ApplicationException (애플리케이션 실패)
```

**네이밍 규칙**: `{Entity}{Reason}Exception`

---

## 테스트 구조

```
src/test/java/com/hoops/
├── acceptance/              # 인수 테스트 (Cucumber)
│   ├── adapter/             # 테스트 어댑터
│   ├── config/              # 테스트 설정
│   ├── mock/                # Mock 구현체
│   └── steps/               # Step 정의
├── auth/                    # Auth 단위/통합 테스트
├── match/                   # Match 테스트
├── participation/           # Participation 테스트
└── ...

src/test/resources/
├── features/                # Cucumber 시나리오
│   ├── match.feature
│   ├── participation.feature
│   └── notification.feature
└── application-test.yml     # 테스트 설정
```

---

## 데이터베이스 스키마

### ERD (간략)

```
users (1)
  ├─→ (N) auth_accounts
  ├─→ (N) locations
  ├─→ (N) matches (as host)
  ├─→ (N) participations
  └─→ (N) notifications

matches (1)
  └─→ (N) participations
```

### 주요 테이블

| 테이블 | 설명 |
|--------|------|
| `users` | 사용자 정보 |
| `auth_accounts` | OAuth 인증 정보 |
| `locations` | 장소 정보 |
| `matches` | 경기 정보 |
| `participations` | 참가 정보 |
| `notifications` | 알림 정보 |

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 3.x |
| 데이터 접근 | Spring Data JPA |
| 데이터베이스 | MySQL (운영), H2 (테스트) |
| 메시징 | Apache Kafka |
| 보안 | Spring Security, JWT |
| 테스트 | JUnit 5, Cucumber |
| 빌드 | Gradle (Kotlin DSL) |
