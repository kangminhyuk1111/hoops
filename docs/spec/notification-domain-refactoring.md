# Notification Domain Refactoring

> 작성일: 2025-01-20
> 기준: `/architecture-patterns` skill, Participation 도메인 리팩토링 참고

## TL;DR (한줄 요약)

Notification 도메인을 **Hexagonal Architecture + DDD** 표준에 맞게 리팩토링. `infrastructure/` 패키지를 `adapter/`로 이동하고, 패키지 구조를 표준화함.

---

## 왜 리팩토링했나?

| 문제점 | 해결 |
|--------|------|
| `infrastructure/` 패키지 사용 | `adapter/out/`, `adapter/in/`으로 통일 |
| Domain 모델이 `domain/` 바로 아래 위치 | `domain/model/`로 분리 |
| Enum이 `domain/` 바로 아래 위치 | `domain/vo/`로 분리 |
| JPA Entity 네이밍 불일치 (`NotificationEntity`) | `NotificationJpaEntity`로 변경 |
| Repository 구현체 네이밍 불일치 | `NotificationJpaAdapter`로 변경 |
| Kafka Consumer 위치 불일치 | `adapter/in/kafka/`로 이동 |

---

## 변경 전후 비교

### Before (기존 구조)

| 경로 | 설명 |
|------|------|
| `domain/Notification.java` | model/ 폴더 없음 |
| `domain/NotificationType.java` | vo/ 폴더 없음 |
| `domain/repository/NotificationRepository.java` | 올바른 위치 (DDD) |
| `infrastructure/NotificationEntity.java` | 위치 및 네이밍 불일치 |
| `infrastructure/adapter/NotificationRepositoryImpl.java` | 네이밍 불일치 |
| `infrastructure/jpa/JpaNotificationRepository.java` | 네이밍 불일치 |
| `infrastructure/mapper/NotificationMapper.java` | 위치 불일치 |
| `infrastructure/kafka/NotificationEventConsumer.java` | 위치 불일치 (Inbound Adapter) |

### After (리팩토링 후)

| 경로 | 설명 |
|------|------|
| `domain/model/Notification.java` | Entity (Identity 있음) |
| `domain/vo/NotificationType.java` | Value Object (Enum) |
| `domain/repository/NotificationRepository.java` | DDD Repository (유지) |
| `adapter/out/persistence/NotificationJpaEntity.java` | JPA Entity |
| `adapter/out/persistence/NotificationJpaAdapter.java` | Repository 구현체 |
| `adapter/out/persistence/SpringDataNotificationRepository.java` | Spring Data JPA |
| `adapter/out/persistence/NotificationMapper.java` | Domain ↔ JPA 변환 |
| `adapter/in/kafka/NotificationEventConsumer.java` | Kafka 이벤트 수신 (Inbound) |

---

## 핵심 변경 사항

### 1. Domain Model 위치 이동

| Before | After |
|--------|-------|
| `domain/Notification.java` | `domain/model/Notification.java` |
| `domain/NotificationType.java` | `domain/vo/NotificationType.java` |

### 2. infrastructure → adapter 이동

| Before | After |
|--------|-------|
| `infrastructure/NotificationEntity.java` | `adapter/out/persistence/NotificationJpaEntity.java` |
| `infrastructure/adapter/NotificationRepositoryImpl.java` | `adapter/out/persistence/NotificationJpaAdapter.java` |
| `infrastructure/jpa/JpaNotificationRepository.java` | `adapter/out/persistence/SpringDataNotificationRepository.java` |
| `infrastructure/mapper/NotificationMapper.java` | `adapter/out/persistence/NotificationMapper.java` |
| `infrastructure/kafka/NotificationEventConsumer.java` | `adapter/in/kafka/NotificationEventConsumer.java` |

---

## Repository 구조

| 계층 | 클래스 | 위치 | 역할 |
|------|--------|------|------|
| Domain | `NotificationRepository` (interface) | `domain/repository/` | 순수 Java 인터페이스, JPA 의존성 없음 |
| Adapter | `NotificationJpaAdapter` | `adapter/out/persistence/` | NotificationRepository 구현체, JPA 주입 |
| Adapter | `SpringDataNotificationRepository` | `adapter/out/persistence/` | Spring Data JPA 인터페이스 |

**의존 관계**: `NotificationJpaAdapter` → (implements) → `NotificationRepository`, `NotificationJpaAdapter` → (uses) → `SpringDataNotificationRepository`

---

## Adapter 분류

### Inbound Adapter (`adapter/in/`)

| 클래스 | 위치 | 용도 |
|--------|------|------|
| `NotificationController` | `adapter/in/web/` | HTTP 요청 처리 |
| `NotificationEventConsumer` | `adapter/in/kafka/` | Kafka 이벤트 수신 |

### Outbound Adapter (`adapter/out/`)

| 클래스 | 위치 | 용도 |
|--------|------|------|
| `NotificationJpaAdapter` | `adapter/out/persistence/` | DB 영속성 |

---

## Import 변경 요약

```java
// Before
import com.hoops.notification.domain.Notification;
import com.hoops.notification.domain.NotificationType;
import com.hoops.notification.infrastructure.NotificationEntity;

// After
import com.hoops.notification.domain.model.Notification;
import com.hoops.notification.domain.vo.NotificationType;
import com.hoops.notification.adapter.out.persistence.NotificationJpaEntity;
```

---

## 체크리스트

리팩토링 완료 확인:

- [x] `domain/model/` - Notification.java 이동
- [x] `domain/vo/` - NotificationType.java 이동
- [x] `domain/repository/` - 유지 (이미 올바른 위치)
- [x] `adapter/out/persistence/` - JPA 관련 통합
- [x] `adapter/in/kafka/` - NotificationEventConsumer 이동
- [x] `infrastructure/` 패키지 삭제
- [x] 모든 import 수정
- [x] 테스트 코드 import 수정
