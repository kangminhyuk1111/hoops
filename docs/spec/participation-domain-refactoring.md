# Participation Domain Refactoring

> 작성일: 2025-01-20
> 기준: `/architecture-patterns` skill, User 도메인 리팩토링 참고

## TL;DR (한줄 요약)

Participation 도메인을 **Hexagonal Architecture + DDD** 표준에 맞게 리팩토링. `infrastructure/` 패키지를 `adapter/out/`으로 이동하고, 패키지 구조를 표준화함.

---

## 왜 리팩토링했나?

| 문제점 | 해결 |
|--------|------|
| `infrastructure/` 패키지 사용 | `adapter/out/`으로 통일 |
| Domain 모델이 `domain/` 바로 아래 위치 | `domain/model/`로 분리 |
| Enum이 `domain/` 바로 아래 위치 | `domain/vo/`로 분리 |
| JPA Entity 네이밍 불일치 (`ParticipationEntity`) | `ParticipationJpaEntity`로 변경 |
| Repository 구현체 네이밍 불일치 | `ParticipationJpaAdapter`로 변경 |

---

## 변경 전후 비교

### Before (기존 구조)

| 경로 | 설명 |
|------|------|
| `domain/Participation.java` | model/ 폴더 없음 |
| `domain/ParticipationStatus.java` | vo/ 폴더 없음 |
| `domain/repository/ParticipationRepository.java` | 올바른 위치 (DDD) |
| `infrastructure/ParticipationEntity.java` | 위치 및 네이밍 불일치 |
| `infrastructure/adapter/ParticipationRepositoryImpl.java` | 네이밍 불일치 |
| `infrastructure/jpa/JpaParticipationRepository.java` | 네이밍 불일치 |
| `infrastructure/mapper/ParticipationMapper.java` | 위치 불일치 |
| `infrastructure/adapter/UserInfoAdapter.java` | 위치 불일치 |
| `infrastructure/adapter/MatchInfoAdapter.java` | 위치 불일치 |
| `infrastructure/kafka/KafkaParticipationEventPublisher.java` | 위치 불일치 |

### After (리팩토링 후)

| 경로 | 설명 |
|------|------|
| `domain/model/Participation.java` | Entity (Identity 있음) |
| `domain/vo/ParticipationStatus.java` | Value Object (Enum) |
| `domain/repository/ParticipationRepository.java` | DDD Repository (유지) |
| `adapter/out/persistence/ParticipationJpaEntity.java` | JPA Entity |
| `adapter/out/persistence/ParticipationJpaAdapter.java` | Repository 구현체 |
| `adapter/out/persistence/SpringDataParticipationRepository.java` | Spring Data JPA |
| `adapter/out/persistence/ParticipationMapper.java` | Domain ↔ JPA 변환 |
| `adapter/out/UserInfoAdapter.java` | User Context 조회용 |
| `adapter/out/MatchInfoAdapter.java` | Match Context 조회용 |
| `adapter/out/kafka/KafkaParticipationEventPublisher.java` | Kafka 이벤트 발행 |

---

## 핵심 변경 사항

### 1. Domain Model 위치 이동

| Before | After |
|--------|-------|
| `domain/Participation.java` | `domain/model/Participation.java` |
| `domain/ParticipationStatus.java` | `domain/vo/ParticipationStatus.java` |

### 2. infrastructure → adapter/out 이동

| Before | After |
|--------|-------|
| `infrastructure/ParticipationEntity.java` | `adapter/out/persistence/ParticipationJpaEntity.java` |
| `infrastructure/adapter/ParticipationRepositoryImpl.java` | `adapter/out/persistence/ParticipationJpaAdapter.java` |
| `infrastructure/jpa/JpaParticipationRepository.java` | `adapter/out/persistence/SpringDataParticipationRepository.java` |
| `infrastructure/mapper/ParticipationMapper.java` | `adapter/out/persistence/ParticipationMapper.java` |
| `infrastructure/adapter/UserInfoAdapter.java` | `adapter/out/UserInfoAdapter.java` |
| `infrastructure/adapter/MatchInfoAdapter.java` | `adapter/out/MatchInfoAdapter.java` |
| `infrastructure/kafka/KafkaParticipationEventPublisher.java` | `adapter/out/kafka/KafkaParticipationEventPublisher.java` |

---

## Repository 구조

| 계층 | 클래스 | 위치 | 역할 |
|------|--------|------|------|
| Domain | `ParticipationRepository` (interface) | `domain/repository/` | 순수 Java 인터페이스, JPA 의존성 없음 |
| Adapter | `ParticipationJpaAdapter` | `adapter/out/persistence/` | ParticipationRepository 구현체, JPA 주입 |
| Adapter | `SpringDataParticipationRepository` | `adapter/out/persistence/` | Spring Data JPA 인터페이스 |

**의존 관계**: `ParticipationJpaAdapter` → (implements) → `ParticipationRepository`, `ParticipationJpaAdapter` → (uses) → `SpringDataParticipationRepository`

---

## 외부 Context Adapter

| 클래스 | 위치 | Port | 용도 |
|--------|------|------|------|
| `UserInfoAdapter` | `adapter/out/` | `UserInfoProvider` | User Context에서 사용자 정보 조회 |
| `MatchInfoAdapter` | `adapter/out/` | `MatchInfoProvider` | Match Context에서 경기 정보 조회 |
| `KafkaParticipationEventPublisher` | `adapter/out/kafka/` | `ParticipationEventPublisher` | Kafka로 참가 이벤트 발행 |

---

## Import 변경 요약

```java
// Before
import com.hoops.participation.domain.Participation;
import com.hoops.participation.domain.ParticipationStatus;
import com.hoops.participation.infrastructure.ParticipationEntity;

// After
import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.domain.vo.ParticipationStatus;
import com.hoops.participation.adapter.out.persistence.ParticipationJpaEntity;
```

---

## 체크리스트

리팩토링 완료 확인:

- [x] `domain/model/` - Participation.java 이동
- [x] `domain/vo/` - ParticipationStatus.java 이동
- [x] `domain/repository/` - 유지 (이미 올바른 위치)
- [x] `adapter/out/persistence/` - JPA 관련 통합
- [x] `adapter/out/` - UserInfoAdapter, MatchInfoAdapter 이동
- [x] `adapter/out/kafka/` - KafkaParticipationEventPublisher 이동
- [x] `infrastructure/` 패키지 삭제
- [x] 모든 import 수정
- [x] 테스트 코드 import 수정
