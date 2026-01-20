# Hexagonal Architecture Refactoring Summary

> 작성일: 2025-01-20
> 기준: `/architecture-patterns` skill

## Overview

Hoops 프로젝트의 모든 도메인을 **Hexagonal Architecture + DDD** 표준에 맞게 리팩토링 완료.

### 주요 변경 사항

| 변경 유형 | Before | After |
|-----------|--------|-------|
| 영속성 계층 위치 | `infrastructure/` | `adapter/out/persistence/` |
| Domain Model 위치 | `domain/` 바로 아래 | `domain/model/` |
| Value Object 위치 | `domain/` 바로 아래 | `domain/vo/` |
| JPA Entity 네이밍 | `*Entity` | `*JpaEntity` |
| Repository 구현체 네이밍 | `*RepositoryImpl` | `*JpaAdapter` |
| Spring Data JPA 네이밍 | `Jpa*Repository` | `SpringData*Repository` |

---

## 표준 패키지 구조

```
{domain}/
├── domain/
│   ├── model/           # Entity (Identity 있음)
│   ├── vo/              # Value Object (Enum 포함)
│   ├── repository/      # DDD Repository Interface
│   └── exception/       # 도메인 규칙 위반 예외
│
├── application/
│   ├── port/in/         # Inbound Port (*UseCase)
│   ├── port/out/        # Outbound Port (*Port)
│   ├── service/         # UseCase 구현체
│   ├── dto/             # Command, Response
│   └── exception/       # UseCase 실패 예외
│
├── adapter/
│   ├── in/
│   │   ├── web/         # HTTP Controller
│   │   └── kafka/       # Kafka Consumer (Inbound)
│   └── out/
│       ├── persistence/ # JPA 영속성
│       ├── kafka/       # Kafka Producer (Outbound)
│       └── {context}/   # 외부 컨텍스트 Adapter
│
└── infrastructure/
    └── config/          # Spring Configuration
```

---

## 도메인별 리팩토링 상세

### 1. Auth 도메인

**상태**: 기존 표준 준수 (리팩토링 불필요)

Auth 도메인은 프로젝트 초기부터 Hexagonal Architecture 표준을 따라 설계되어 별도 리팩토링 없이 참조 모델로 사용.

**구조**:
- `domain/model/AuthAccount.java` - 인증 계정 Entity
- `domain/vo/` - AuthProvider, OAuthUserInfo, TokenPair 등 다수의 Value Object
- `domain/repository/AuthAccountRepository.java` - DDD Repository
- `adapter/out/persistence/` - JPA 영속성
- `adapter/out/oauth/kakao/` - 카카오 OAuth Adapter (벤더별 분리)

**특이사항**:
- 외부 OAuth Provider를 `adapter/out/oauth/{provider}/`로 벤더별 분리
- 벤더 중립적 Value Object 사용 (`OAuthUserInfo`, `OAuthTokenInfo`)

---

### 2. Match 도메인

**PR**: #36

**주요 변경**:

| Before | After |
|--------|-------|
| `domain/Match.java` | `domain/model/Match.java` |
| `domain/MatchStatus.java` | `domain/vo/MatchStatus.java` |
| `infrastructure/MatchEntity.java` | `adapter/out/persistence/MatchJpaEntity.java` |
| `infrastructure/adapter/MatchRepositoryImpl.java` | `adapter/out/persistence/MatchJpaAdapter.java` |
| `infrastructure/jpa/JpaMatchRepository.java` | `adapter/out/persistence/SpringDataMatchRepository.java` |

**특이사항**:
- `domain/policy/MatchPolicyValidator.java` - 도메인 정책 검증 로직 분리
- 다른 컨텍스트 Adapter 3개:
  - `adapter/out/LocationInfoAdapter.java` - Location 컨텍스트 연동
  - `adapter/out/UserHostInfoAdapter.java` - User 컨텍스트 연동 (호스트 정보)
  - `adapter/out/MatchParticipationAdapter.java` - Participation 컨텍스트 연동
- `application/scheduler/MatchStatusScheduler.java` - 경기 상태 자동 업데이트 스케줄러

---

### 3. Location 도메인

**PR**: #37

**주요 변경**:

| Before | After |
|--------|-------|
| `domain/Location.java` | `domain/model/Location.java` |
| `infrastructure/LocationEntity.java` | `adapter/out/persistence/LocationJpaEntity.java` |
| `infrastructure/adapter/LocationRepositoryImpl.java` | `adapter/out/persistence/LocationJpaAdapter.java` |
| `infrastructure/jpa/JpaLocationRepository.java` | `adapter/out/persistence/SpringDataLocationRepository.java` |

**특이사항**:
- 가장 단순한 도메인 구조 (Value Object 없음)
- `adapter/out/LocationQueryAdapter.java` - 다른 컨텍스트에서 Location 조회용 Adapter

---

### 4. User 도메인

**PR**: #38

**주요 변경**:

| Before | After |
|--------|-------|
| `domain/User.java` | `domain/model/User.java` |
| `application/exception/InvalidNicknameException.java` | `domain/exception/InvalidNicknameException.java` |
| `infrastructure/UserEntity.java` | `adapter/out/persistence/UserJpaEntity.java` |
| `infrastructure/adapter/UserRepositoryImpl.java` | `adapter/out/persistence/UserJpaAdapter.java` |
| `infrastructure/jpa/JpaUserRepository.java` | `adapter/out/persistence/SpringDataUserRepository.java` |

**특이사항**:
- `InvalidNicknameException`을 `application/exception/`에서 `domain/exception/`으로 이동
  - 닉네임 유효성은 도메인 규칙 위반이므로 Domain Exception이 적합
- 다른 컨텍스트 Adapter 2개:
  - `adapter/out/UserCommandAdapter.java` - User 생성/수정용
  - `adapter/out/UserQueryAdapter.java` - User 조회용

---

### 5. Participation 도메인

**PR**: #39

**주요 변경**:

| Before | After |
|--------|-------|
| `domain/Participation.java` | `domain/model/Participation.java` |
| `domain/ParticipationStatus.java` | `domain/vo/ParticipationStatus.java` |
| `infrastructure/ParticipationEntity.java` | `adapter/out/persistence/ParticipationJpaEntity.java` |
| `infrastructure/adapter/ParticipationRepositoryImpl.java` | `adapter/out/persistence/ParticipationJpaAdapter.java` |
| `infrastructure/jpa/JpaParticipationRepository.java` | `adapter/out/persistence/SpringDataParticipationRepository.java` |
| `infrastructure/kafka/KafkaParticipationEventPublisher.java` | `adapter/out/kafka/KafkaParticipationEventPublisher.java` |

**특이사항**:
- **Kafka Outbound Adapter**: 참가 이벤트 발행용 `adapter/out/kafka/`
  - Kafka Publisher는 외부로 이벤트를 **발행**하므로 Outbound Adapter
- 다른 컨텍스트 Adapter 2개:
  - `adapter/out/UserInfoAdapter.java` - User 컨텍스트 연동
  - `adapter/out/MatchInfoAdapter.java` - Match 컨텍스트 연동
- 가장 많은 Application Exception (13개) - 복잡한 참가 비즈니스 규칙 반영

---

### 6. Notification 도메인

**PR**: #40

**주요 변경**:

| Before | After |
|--------|-------|
| `domain/Notification.java` | `domain/model/Notification.java` |
| `domain/NotificationType.java` | `domain/vo/NotificationType.java` |
| `infrastructure/NotificationEntity.java` | `adapter/out/persistence/NotificationJpaEntity.java` |
| `infrastructure/adapter/NotificationRepositoryImpl.java` | `adapter/out/persistence/NotificationJpaAdapter.java` |
| `infrastructure/jpa/JpaNotificationRepository.java` | `adapter/out/persistence/SpringDataNotificationRepository.java` |
| `infrastructure/kafka/NotificationEventConsumer.java` | `adapter/in/kafka/NotificationEventConsumer.java` |

**특이사항**:
- **Kafka Inbound Adapter**: 참가 이벤트 수신용 `adapter/in/kafka/`
  - Kafka Consumer는 외부에서 이벤트를 **수신**하므로 Inbound Adapter
  - Participation 도메인의 `adapter/out/kafka/`와 대응
- `NotificationType` enum을 Value Object로 분류하여 `domain/vo/`로 이동

---

## Kafka Adapter 분류 기준

| 방향 | 역할 | 위치 | 예시 |
|------|------|------|------|
| **Inbound** | 외부 이벤트 수신 (Consumer) | `adapter/in/kafka/` | NotificationEventConsumer |
| **Outbound** | 이벤트 발행 (Producer) | `adapter/out/kafka/` | KafkaParticipationEventPublisher |

```
[Participation 도메인]                    [Notification 도메인]
adapter/out/kafka/                       adapter/in/kafka/
KafkaParticipationEventPublisher  --->   NotificationEventConsumer
        (Producer)                              (Consumer)
```

---

## 외부 컨텍스트 Adapter 정리

다른 Bounded Context와 통신하는 Adapter들은 `adapter/out/`에 위치:

| 도메인 | Adapter | 연동 대상 |
|--------|---------|-----------|
| Match | `LocationInfoAdapter` | Location 컨텍스트 |
| Match | `UserHostInfoAdapter` | User 컨텍스트 |
| Match | `MatchParticipationAdapter` | Participation 컨텍스트 |
| Location | `LocationQueryAdapter` | (타 컨텍스트에서 호출) |
| User | `UserCommandAdapter` | (타 컨텍스트에서 호출) |
| User | `UserQueryAdapter` | (타 컨텍스트에서 호출) |
| Participation | `UserInfoAdapter` | User 컨텍스트 |
| Participation | `MatchInfoAdapter` | Match 컨텍스트 |

---

## 네이밍 컨벤션 정리

### Repository 계층

| 계층 | 클래스 | 위치 | 역할 |
|------|--------|------|------|
| Domain | `{Entity}Repository` | `domain/repository/` | 순수 Java 인터페이스, JPA 의존성 없음 |
| Adapter | `{Entity}JpaAdapter` | `adapter/out/persistence/` | Repository 구현체, JPA 주입 |
| Adapter | `SpringData{Entity}Repository` | `adapter/out/persistence/` | Spring Data JPA 인터페이스 |

### Mapper

| 파일 | 위치 | 역할 |
|------|------|------|
| `{Entity}Mapper` | `adapter/out/persistence/` | Domain Model ↔ JPA Entity 변환 |

---

## Import 변경 패턴

```java
// Domain Model
// Before: import com.hoops.{domain}.domain.{Entity};
// After:  import com.hoops.{domain}.domain.model.{Entity};

// Value Object (Enum)
// Before: import com.hoops.{domain}.domain.{Type};
// After:  import com.hoops.{domain}.domain.vo.{Type};

// JPA Entity
// Before: import com.hoops.{domain}.infrastructure.{Entity}Entity;
// After:  import com.hoops.{domain}.adapter.out.persistence.{Entity}JpaEntity;
```

---

## 리팩토링 체크리스트

새 도메인 생성 또는 기존 도메인 리팩토링 시 확인:

- [ ] `domain/model/` - Domain Model (순수 POJO)
- [ ] `domain/vo/` - Value Objects, Enums
- [ ] `domain/repository/` - DDD Repository Interface
- [ ] `domain/exception/` - 도메인 규칙 위반 예외
- [ ] `adapter/out/persistence/` - JPA Entity, Adapter, Mapper, SpringData Repository
- [ ] `adapter/in/kafka/` - Kafka Consumer (Inbound) - 필요시
- [ ] `adapter/out/kafka/` - Kafka Producer (Outbound) - 필요시
- [ ] `adapter/out/{context}/` - 외부 컨텍스트 Adapter - 필요시
- [ ] `infrastructure/` 패키지는 `config/`만 존재
- [ ] 모든 import 수정 완료
- [ ] 테스트 코드 import 수정 완료

---

## 관련 문서

- [Match Domain Refactoring](./match-domain-refactoring.md)
- [Location Domain Refactoring](./location-domain-refactoring.md)
- [User Domain Refactoring](./user-domain-refactoring.md)
- [Participation Domain Refactoring](./participation-domain-refactoring.md)
- [Notification Domain Refactoring](./notification-domain-refactoring.md)
