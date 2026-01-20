# User Domain Refactoring

> 작성일: 2025-01-20
> 기준: `/architecture-patterns` skill, Location 도메인 리팩토링 참고

## TL;DR (한줄 요약)

User 도메인을 **Hexagonal Architecture + DDD** 표준에 맞게 리팩토링. `infrastructure/` 패키지를 `adapter/out/`으로 이동하고, 패키지 구조를 표준화함.

---

## 왜 리팩토링했나?

| 문제점 | 해결 |
|--------|------|
| `infrastructure/` 패키지 사용 | `adapter/out/`으로 통일 |
| Domain 모델이 `domain/` 바로 아래 위치 | `domain/model/`로 분리 |
| 도메인 예외가 `application/exception/`에 위치 | `domain/exception/`으로 이동 |
| JPA Entity 네이밍 불일치 (`UserEntity`) | `UserJpaEntity`로 변경 |
| Repository 구현체 네이밍 불일치 (`UserRepositoryImpl`) | `UserJpaAdapter`로 변경 |

---

## 변경 전후 비교

### Before (기존 구조)

| 경로 | 설명 |
|------|------|
| `domain/User.java` | model/ 폴더 없음 |
| `domain/repository/UserRepository.java` | 올바른 위치 (DDD) |
| `infrastructure/UserEntity.java` | 위치 및 네이밍 불일치 |
| `infrastructure/adapter/UserRepositoryImpl.java` | 네이밍 불일치 |
| `infrastructure/adapter/UserCommandAdapter.java` | 위치 불일치 |
| `infrastructure/adapter/UserQueryAdapter.java` | 위치 불일치 |
| `infrastructure/jpa/JpaUserRepository.java` | 네이밍 불일치 |
| `infrastructure/mapper/UserMapper.java` | 위치 불일치 |
| `application/exception/InvalidNicknameException.java` | 도메인 예외인데 여기 있음 |

### After (리팩토링 후)

| 경로 | 설명 |
|------|------|
| `domain/model/User.java` | Entity (Identity 있음) |
| `domain/repository/UserRepository.java` | DDD Repository (유지) |
| `domain/exception/InvalidNicknameException.java` | 도메인 예외 이동 |
| `adapter/out/persistence/UserJpaEntity.java` | JPA Entity |
| `adapter/out/persistence/UserJpaAdapter.java` | Repository 구현체 |
| `adapter/out/persistence/SpringDataUserRepository.java` | Spring Data JPA |
| `adapter/out/persistence/UserMapper.java` | Domain ↔ JPA 변환 |
| `adapter/out/UserCommandAdapter.java` | 외부 Context 명령용 |
| `adapter/out/UserQueryAdapter.java` | 외부 Context 조회용 |

---

## 핵심 변경 사항

### 1. Domain Model 위치 이동

| Before | After |
|--------|-------|
| `domain/User.java` | `domain/model/User.java` |

### 2. infrastructure → adapter/out 이동

| Before | After |
|--------|-------|
| `infrastructure/UserEntity.java` | `adapter/out/persistence/UserJpaEntity.java` |
| `infrastructure/adapter/UserRepositoryImpl.java` | `adapter/out/persistence/UserJpaAdapter.java` |
| `infrastructure/jpa/JpaUserRepository.java` | `adapter/out/persistence/SpringDataUserRepository.java` |
| `infrastructure/mapper/UserMapper.java` | `adapter/out/persistence/UserMapper.java` |
| `infrastructure/adapter/UserCommandAdapter.java` | `adapter/out/UserCommandAdapter.java` |
| `infrastructure/adapter/UserQueryAdapter.java` | `adapter/out/UserQueryAdapter.java` |

### 3. 도메인 예외 분리

| Before | After | 이유 |
|--------|-------|------|
| `application/exception/InvalidNicknameException` | `domain/exception/InvalidNicknameException` | 닉네임 형식(2~20자) 규칙은 도메인 규칙 |

---

## 예외 분류 체계

### Domain Exception (`domain/exception/`)

> 도메인 규칙 위반 시 발생

| 예외 | 설명 | 발생 상황 |
|------|------|----------|
| `InvalidNicknameException` | 닉네임 규칙 위반 | 2~20자 규칙 위반 |

### Application Exception (`application/exception/`)

> UseCase 실행 실패 시 발생

| 예외 | 설명 | HTTP |
|------|------|------|
| `UserNotFoundException` | 사용자 없음 | 404 |
| `DuplicateNicknameException` | 중복 닉네임 | 409 |

---

## Repository 구조

| 계층 | 클래스 | 위치 | 역할 |
|------|--------|------|------|
| Domain | `UserRepository` (interface) | `domain/repository/` | 순수 Java 인터페이스, JPA 의존성 없음 |
| Adapter | `UserJpaAdapter` | `adapter/out/persistence/` | UserRepository 구현체, JPA 주입 |
| Adapter | `SpringDataUserRepository` | `adapter/out/persistence/` | Spring Data JPA 인터페이스 |

**의존 관계**: `UserJpaAdapter` → (implements) → `UserRepository`, `UserJpaAdapter` → (uses) → `SpringDataUserRepository`

---

## 외부 Context Adapter

| 클래스 | 위치 | Port | 용도 |
|--------|------|------|------|
| `UserCommandAdapter` | `adapter/out/` | `UserCommandPort` | 외부에서 사용자 생성 |
| `UserQueryAdapter` | `adapter/out/` | `UserQueryPort` | 외부에서 사용자 조회 |

---

## Import 변경 요약

```java
// Before
import com.hoops.user.domain.User;
import com.hoops.user.application.exception.InvalidNicknameException;
import com.hoops.user.infrastructure.UserEntity;

// After
import com.hoops.user.domain.model.User;
import com.hoops.user.domain.exception.InvalidNicknameException;
import com.hoops.user.adapter.out.persistence.UserJpaEntity;
```

---

## 체크리스트

리팩토링 완료 확인:

- [x] `domain/model/` - User.java 이동
- [x] `domain/repository/` - 유지 (이미 올바른 위치)
- [x] `domain/exception/` - InvalidNicknameException 이동
- [x] `adapter/out/persistence/` - JPA 관련 통합
- [x] `adapter/out/` - UserCommandAdapter, UserQueryAdapter 이동
- [x] `infrastructure/` 패키지 삭제
- [x] 모든 import 수정
- [x] 테스트 코드 import 수정
