# Match Domain Refactoring

> 작성일: 2025-01-20
> 기준: `/architecture-patterns` skill, Auth 도메인 구조 참고

## TL;DR (한줄 요약)

Match 도메인을 **Hexagonal Architecture + DDD** 표준에 맞게 리팩토링. Domain 계층을 순수 POJO로 분리하고, 패키지 구조를 표준화함.

---

## 왜 리팩토링했나?

| 문제점 | 해결 |
|--------|------|
| Domain에 Spring 의존성 (`@Component`) | 순수 POJO로 변환 |
| Repository가 `application/port/out`에 위치 | DDD 원칙에 따라 `domain/repository`로 이동 |
| DTO 위치 혼재 | 계층별로 명확히 분리 |
| 네이밍 불일치 (`*Provider` vs `*Port`) | `*Port`로 통일 |
| 예외 분류 없음 | Domain/Application 예외 분리 |

---

## 변경 전후 비교

### Before (기존 구조)

```
match/
├── domain/
│   ├── Match.java                    # 위치 OK, but 하위 폴더 없음
│   ├── MatchStatus.java              # vo/ 폴더 없음
│   └── policy/
│       └── MatchPolicyValidator.java # @Component 사용 (문제!)
│
├── application/
│   ├── port/
│   │   ├── in/                       # UseCase + Command 혼재
│   │   └── out/
│   │       ├── MatchRepository.java  # Repository가 여기? (DDD 위반)
│   │       ├── HostInfoProvider.java # *Provider 네이밍
│   │       ├── HostInfo.java         # DTO가 port/out에?
│   │       └── LocationInfo.java
│   └── exception/                    # 모든 예외가 여기
│
├── adapter/
│   ├── dto/                          # adapter 바로 아래? (잘못된 위치)
│   └── out/
│       ├── MatchEntity.java          # 폴더 없이 바로 위치
│       ├── adapter/                  # 이상한 중첩
│       ├── jpa/
│       └── mapper/
```

### After (리팩토링 후)

```
match/
├── domain/                           # 순수 POJO (No Spring, JPA)
│   ├── model/
│   │   └── Match.java                # Entity (Identity 있음)
│   ├── vo/
│   │   └── MatchStatus.java          # Value Object (불변)
│   ├── repository/
│   │   └── MatchRepository.java      # DDD Repository (인터페이스)
│   ├── exception/                    # 도메인 규칙 위반 예외
│   │   ├── InvalidTimeRangeException.java
│   │   └── ...
│   └── policy/
│       └── MatchPolicyValidator.java # 순수 POJO (No @Component)
│
├── application/
│   ├── port/
│   │   ├── in/                       # UseCase 인터페이스만
│   │   │   ├── CreateMatchUseCase.java
│   │   │   └── ...
│   │   └── out/                      # 외부 서비스 Port (*Port)
│   │       ├── HostInfoPort.java
│   │       └── LocationInfoPort.java
│   ├── dto/                          # UseCase I/O
│   │   ├── HostInfo.java
│   │   └── LocationInfo.java
│   ├── service/                      # UseCase 구현체
│   └── exception/                    # UseCase 실패 예외
│       ├── MatchNotFoundException.java
│       └── ...
│
├── adapter/
│   ├── in/web/
│   │   ├── MatchController.java
│   │   └── dto/                      # HTTP Request/Response
│   │       ├── CreateMatchRequest.java
│   │       └── MatchResponse.java
│   └── out/
│       └── persistence/              # JPA 관련 전부 여기
│           ├── MatchJpaEntity.java
│           ├── MatchJpaAdapter.java
│           ├── SpringDataMatchRepository.java
│           └── MatchMapper.java
│
└── infrastructure/
    └── config/
        └── MatchConfig.java          # Bean 등록
```

---

## 핵심 변경 사항

### 1. Domain Layer 순수 POJO화

**목표**: Domain 계층에서 Spring/JPA 의존성 완전 제거

| 항목 | Before | After |
|------|--------|-------|
| Match | `domain/Match.java` | `domain/model/Match.java` |
| MatchStatus | `domain/MatchStatus.java` | `domain/vo/MatchStatus.java` |
| Repository | `application/port/out/` | `domain/repository/` |
| Policy | `@Component` 사용 | 순수 POJO |

### 2. MatchPolicyValidator 변환

**Before** - Spring에 의존:
```java
@Component  // Spring 의존!
public class MatchPolicyValidator {
    public void validateCreateMatch(CreateMatchCommand command) {
        // command 객체에 의존
    }
}
```

**After** - 순수 POJO:
```java
// @Component 없음!
public class MatchPolicyValidator {
    public void validateCreateMatch(
            LocalDate matchDate,
            LocalTime startTime,
            LocalTime endTime,
            Integer maxParticipants) {
        // 원시 타입만 받음 (application 계층 의존 X)
    }
}
```

**Bean 등록** - infrastructure에서:
```java
// infrastructure/config/MatchConfig.java
@Configuration
public class MatchConfig {
    @Bean
    public MatchPolicyValidator matchPolicyValidator() {
        return new MatchPolicyValidator();
    }
}
```

### 3. Port 네이밍 규칙

| Before | After | 이유 |
|--------|-------|------|
| `HostInfoProvider` | `HostInfoPort` | 표준 네이밍 |
| `LocationInfoProvider` | `LocationInfoPort` | 표준 네이밍 |

### 4. DTO 위치 정리

| DTO | Before | After |
|-----|--------|-------|
| `CreateMatchRequest` | `adapter/dto/` | `adapter/in/web/dto/` |
| `HostInfo` | `application/port/out/` | `application/dto/` |
| `LocationInfo` | `application/port/out/` | `application/dto/` |

### 5. Persistence 구조 통합

| Before | After |
|--------|-------|
| `adapter/out/MatchEntity.java` | `adapter/out/persistence/MatchJpaEntity.java` |
| `adapter/out/adapter/MatchRepositoryImpl.java` | `adapter/out/persistence/MatchJpaAdapter.java` |
| `adapter/out/jpa/JpaMatchRepository.java` | `adapter/out/persistence/SpringDataMatchRepository.java` |

---

## Repository 구조 (핵심!)

**Hexagonal Architecture에서 JPA를 사용하려면 Adapter가 필요합니다.**

| 계층 | 클래스 | 위치 | 역할 |
|------|--------|------|------|
| Domain | `MatchRepository` (interface) | `domain/repository/` | 순수 Java 인터페이스, JPA 의존성 없음, Domain 모델만 다룸 |
| Adapter | `MatchJpaAdapter` | `adapter/out/persistence/` | MatchRepository 구현체, JPA 주입받아 실제 DB 작업 |
| Adapter | `SpringDataMatchRepository` | `adapter/out/persistence/` | Spring Data JPA 인터페이스, JpaEntity만 다룸 |

**의존 관계**: `MatchJpaAdapter` → (implements) → `MatchRepository`, `MatchJpaAdapter` → (uses) → `SpringDataMatchRepository`

### 코드로 보는 구조

**1. Domain Repository (순수 인터페이스)**
```java
// domain/repository/MatchRepository.java
package com.hoops.match.domain.repository;

public interface MatchRepository {
    Match save(Match match);           // Domain 모델만 다룸
    Optional<Match> findById(Long id);
    // ... JPA 의존성 없음!
}
```

**2. JPA Adapter (구현체 - JPA 의존성 주입)**
```java
// adapter/out/persistence/MatchJpaAdapter.java
@Repository
@RequiredArgsConstructor
public class MatchJpaAdapter implements MatchRepository {

    // Spring Data JPA 주입
    private final SpringDataMatchRepository springDataMatchRepository;

    @Override
    public Match save(Match match) {
        // 1. Domain → JpaEntity 변환
        MatchJpaEntity entity = MatchMapper.toEntity(match);
        // 2. JPA로 저장
        MatchJpaEntity saved = springDataMatchRepository.save(entity);
        // 3. JpaEntity → Domain 변환
        return MatchMapper.toDomain(saved);
    }
}
```

**3. Spring Data JPA (실제 DB 접근)**
```java
// adapter/out/persistence/SpringDataMatchRepository.java
public interface SpringDataMatchRepository
        extends JpaRepository<MatchJpaEntity, Long> {
    // JpaEntity만 다룸, Domain 모델 모름
}
```

### 왜 이렇게 분리하나?

| 장점 | 설명 |
|------|------|
| **테스트 용이** | Domain Repository를 Mock으로 대체 가능 |
| **기술 독립적** | JPA → MongoDB 변경 시 Adapter만 교체 |
| **계층 분리** | Domain이 인프라(JPA)에 의존하지 않음 |
| **단방향 의존성** | Adapter → Domain (역방향 불가) |

---

## 예외 분류 체계

### Domain Exception (`domain/exception/`)

> 도메인 규칙 위반 시 발생 (비즈니스 로직 검증 실패)

| 예외 | 설명 | 발생 상황 |
|------|------|----------|
| `InvalidTimeRangeException` | 시간 범위 오류 | 종료시간 < 시작시간 |
| `InvalidMatchDateException` | 날짜 오류 | 과거 날짜 |
| `InvalidMaxParticipantsException` | 참가자 수 오류 | 2명 미만 또는 100명 초과 |
| `MatchTooSoonException` | 너무 빠른 생성 | 24시간 이내 경기 |
| `MatchTooFarException` | 너무 먼 미래 | 30일 초과 |

### Application Exception (`application/exception/`)

> UseCase 실행 실패 시 발생 (외부 요인, 권한 등)

| 예외 | 설명 | HTTP |
|------|------|------|
| `MatchNotFoundException` | 경기 없음 | 404 |
| `NotMatchHostException` | 호스트 아님 | 403 |
| `MatchFullException` | 정원 초과 | 409 |
| `MatchAlreadyStartedException` | 이미 시작됨 | 400 |
| `CancelReasonRequiredException` | 취소 사유 필요 | 400 |

---

## 의존성 방향

| From | To | 설명 |
|------|----|------|
| Controller | UseCase Interface | 컨트롤러는 UseCase 인터페이스만 알고 있음 |
| Service | UseCase Interface | Service가 UseCase 구현 |
| Service | Domain Repository | Service가 Repository 인터페이스 사용 |
| Service | Outbound Port | Service가 외부 서비스 Port 사용 |
| JpaAdapter | Domain Repository | Adapter가 Repository 인터페이스 구현 |
| External Adapter | Outbound Port | Adapter가 Port 인터페이스 구현 |
| 모든 계층 | Domain Model/VO | 모든 계층이 도메인 모델 사용 |

**핵심 원칙**:
- 의존성은 항상 **안쪽(Domain)**을 향함
- Domain은 **아무것도 의존하지 않음**

---

## Import 변경 요약

```java
// Before
import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;
import com.hoops.match.application.port.out.MatchRepository;

// After
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.match.domain.repository.MatchRepository;
```

---

## 체크리스트

리팩토링 완료 확인:

- [x] `domain/model/` - Match.java 이동
- [x] `domain/vo/` - MatchStatus.java 이동
- [x] `domain/repository/` - MatchRepository.java 이동
- [x] `domain/exception/` - 도메인 예외 분리
- [x] `domain/policy/` - @Component 제거
- [x] `application/port/out/` - *Provider → *Port 변경
- [x] `application/dto/` - HostInfo, LocationInfo 이동
- [x] `adapter/in/web/dto/` - Request/Response DTO 이동
- [x] `adapter/out/persistence/` - JPA 관련 통합
- [x] `infrastructure/config/` - MatchConfig 생성
- [x] 모든 import 수정
- [x] 테스트 코드 import 수정
