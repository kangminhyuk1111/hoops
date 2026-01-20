# Location Domain Refactoring

> 작성일: 2025-01-20
> 기준: `/architecture-patterns` skill, Match 도메인 리팩토링 참고

## TL;DR (한줄 요약)

Location 도메인을 **Hexagonal Architecture + DDD** 표준에 맞게 리팩토링. `infrastructure/` 패키지를 `adapter/out/`으로 이동하고, 패키지 구조를 표준화함.

---

## 왜 리팩토링했나?

| 문제점 | 해결 |
|--------|------|
| `infrastructure/` 패키지 사용 | `adapter/out/`으로 통일 |
| Domain 모델이 `domain/` 바로 아래 위치 | `domain/model/`로 분리 |
| 도메인 예외가 `application/exception/`에 위치 | `domain/exception/`으로 이동 |
| JPA Entity 네이밍 불일치 (`LocationEntity`) | `LocationJpaEntity`로 변경 |

---

## 변경 전후 비교

### Before (기존 구조)

```
location/
├── domain/
│   ├── Location.java                    # model/ 폴더 없음
│   └── repository/
│       └── LocationRepository.java      # 올바른 위치 (DDD)
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateLocationCommand.java
│   │   │   ├── CreateLocationUseCase.java
│   │   │   └── LocationQueryUseCase.java
│   │   └── out/
│   │       └── LocationQueryPort.java
│   ├── service/
│   │   ├── LocationCreator.java
│   │   └── LocationFinder.java
│   └── exception/
│       ├── InvalidLocationNameException.java  # 도메인 예외인데 여기 있음
│       ├── DuplicateLocationNameException.java
│       └── LocationNotFoundException.java
│
├── adapter/in/web/
│   ├── LocationController.java
│   └── dto/
│       ├── CreateLocationRequest.java
│       └── LocationResponse.java
│
└── infrastructure/                      # adapter/out 대신 infrastructure 사용
    ├── LocationEntity.java              # 위치 및 네이밍 불일치
    ├── adapter/
    │   ├── LocationQueryAdapter.java
    │   └── LocationRepositoryImpl.java
    ├── jpa/
    │   └── JpaLocationRepository.java
    └── mapper/
        └── LocationMapper.java
```

### After (리팩토링 후)

```
location/
├── domain/
│   ├── model/
│   │   └── Location.java                # Entity (Identity 있음)
│   ├── repository/
│   │   └── LocationRepository.java      # DDD Repository (유지)
│   └── exception/
│       └── InvalidLocationNameException.java  # 도메인 예외 이동
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateLocationCommand.java
│   │   │   ├── CreateLocationUseCase.java
│   │   │   └── LocationQueryUseCase.java
│   │   └── out/
│   │       └── LocationQueryPort.java
│   ├── service/
│   │   ├── LocationCreator.java
│   │   └── LocationFinder.java
│   └── exception/                       # Application 예외만 남음
│       ├── DuplicateLocationNameException.java
│       └── LocationNotFoundException.java
│
├── adapter/
│   ├── in/web/
│   │   ├── LocationController.java
│   │   └── dto/
│   │       ├── CreateLocationRequest.java
│   │       └── LocationResponse.java
│   └── out/
│       ├── LocationQueryAdapter.java    # 외부 Context 조회용
│       └── persistence/                 # JPA 관련 전부 여기
│           ├── LocationJpaEntity.java
│           ├── LocationJpaAdapter.java
│           ├── SpringDataLocationRepository.java
│           └── LocationMapper.java
```

---

## 핵심 변경 사항

### 1. Domain Model 위치 이동

| Before | After |
|--------|-------|
| `domain/Location.java` | `domain/model/Location.java` |

### 2. infrastructure → adapter/out 이동

| Before | After |
|--------|-------|
| `infrastructure/LocationEntity.java` | `adapter/out/persistence/LocationJpaEntity.java` |
| `infrastructure/adapter/LocationRepositoryImpl.java` | `adapter/out/persistence/LocationJpaAdapter.java` |
| `infrastructure/jpa/JpaLocationRepository.java` | `adapter/out/persistence/SpringDataLocationRepository.java` |
| `infrastructure/mapper/LocationMapper.java` | `adapter/out/persistence/LocationMapper.java` |
| `infrastructure/adapter/LocationQueryAdapter.java` | `adapter/out/LocationQueryAdapter.java` |

### 3. 도메인 예외 분리

| Before | After | 이유 |
|--------|-------|------|
| `application/exception/InvalidLocationNameException` | `domain/exception/InvalidLocationNameException` | 장소명 2자 이상 규칙은 도메인 규칙 |

---

## 예외 분류 체계

### Domain Exception (`domain/exception/`)

> 도메인 규칙 위반 시 발생

| 예외 | 설명 | 발생 상황 |
|------|------|----------|
| `InvalidLocationNameException` | 장소명 규칙 위반 | 2자 미만 장소명 |

### Application Exception (`application/exception/`)

> UseCase 실행 실패 시 발생

| 예외 | 설명 | HTTP |
|------|------|------|
| `LocationNotFoundException` | 장소 없음 | 404 |
| `DuplicateLocationNameException` | 중복 장소명 | 409 |

---

## Repository 구조

| 계층 | 클래스 | 위치 | 역할 |
|------|--------|------|------|
| Domain | `LocationRepository` (interface) | `domain/repository/` | 순수 Java 인터페이스, JPA 의존성 없음 |
| Adapter | `LocationJpaAdapter` | `adapter/out/persistence/` | LocationRepository 구현체, JPA 주입 |
| Adapter | `SpringDataLocationRepository` | `adapter/out/persistence/` | Spring Data JPA 인터페이스 |

**의존 관계**: `LocationJpaAdapter` → (implements) → `LocationRepository`, `LocationJpaAdapter` → (uses) → `SpringDataLocationRepository`

---

## Import 변경 요약

```java
// Before
import com.hoops.location.domain.Location;
import com.hoops.location.application.exception.InvalidLocationNameException;
import com.hoops.location.infrastructure.LocationEntity;

// After
import com.hoops.location.domain.model.Location;
import com.hoops.location.domain.exception.InvalidLocationNameException;
import com.hoops.location.adapter.out.persistence.LocationJpaEntity;
```

---

## 체크리스트

리팩토링 완료 확인:

- [x] `domain/model/` - Location.java 이동
- [x] `domain/repository/` - 유지 (이미 올바른 위치)
- [x] `domain/exception/` - InvalidLocationNameException 이동
- [x] `adapter/out/persistence/` - JPA 관련 통합
- [x] `adapter/out/` - LocationQueryAdapter 이동
- [x] `infrastructure/` 패키지 삭제
- [x] 모든 import 수정
- [x] 테스트 코드 import 수정
