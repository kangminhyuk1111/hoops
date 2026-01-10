# Architecture: Hexagonal & DDD

## 1. 핵심 원칙
- **Domain 격리**: 도메인 로직은 프레임워크, DB, 외부 라이브러리로부터 완전히 독립되어야 한다.
- **의존성 역전**: 모든 의존성은 Domain을 향해야 하며, Infrastructure는 Domain에 정의된 Port를 구현한다.
- **Entity 분리**: Domain Model과 JPA Entity는 별도 패키지로 분리하며, Mapper를 통해 변환한다.

## 2. Package Structure

### 2.1 실제 프로젝트 구조 (match 도메인 기준)
```
src/main/java/com/hoops/match/
├── domain/
│   ├── Match.java                # Domain Model (Pure POJO)
│   └── MatchStatus.java          # Domain Enum
│
├── application/
│   ├── port/
│   │   ├── in/                   # Inbound Port
│   │   │   ├── CreateMatchUseCase.java      # Use Case Interface
│   │   │   ├── CreateMatchCommand.java      # Command DTO
│   │   │   └── MatchQueryUseCase.java       # Query Use Case
│   │   └── out/                  # Outbound Port
│   │       └── MatchRepository.java         # Repository Interface
│   ├── service/                  # Application Service (UseCase별 분리)
│   │   ├── MatchCreator.java     # 생성 Use Case 구현
│   │   └── MatchFinder.java      # 조회 Use Case 구현
│   └── exception/                # Application Exception
│       ├── MatchNotFoundException.java
│       ├── InvalidMatchDateException.java
│       ├── InvalidTimeRangeException.java
│       └── InvalidMaxParticipantsException.java
│
└── adapter/
    ├── dto/                      # DTO (Controller와 공유)
    │   ├── CreateMatchRequest.java    # record
    │   └── MatchResponse.java         # record
    ├── in/
    │   └── web/                  # REST Controller
    │       └── MatchController.java
    └── out/
        ├── MatchEntity.java      # JPA Entity
        ├── adapter/              # Repository 구현체
        │   └── MatchRepositoryImpl.java
        ├── jpa/                  # Spring Data JPA Repository
        │   └── JpaMatchRepository.java
        └── mapper/               # Entity ↔ Domain Mapper
            └── MatchMapper.java
```

### 2.2 구조 특징 및 차이점

#### 실제 구조의 주요 특징
1. **DTO 위치**: `adapter/dto/`에 Request/Response DTO 통합 관리
2. **JPA 관련 코드**: `adapter/out/`에 집중 배치
   - Entity, JPA Repository, Mapper, Repository 구현체 모두 포함
3. **예외 처리**: `application/exception/`에 애플리케이션 예외 관리
4. **Service 분리**: UseCase별로 명확히 분리 (Creator, Finder 등)
5. **Command 패턴**: `CreateMatchCommand`로 입력 데이터 캡슐화

#### 표준 구조 대비 차이
| 항목 | 표준 구조 | 실제 구조 |
|------|-----------|-----------|
| DTO 위치 | `adapter/in/web/dto/` | `adapter/dto/` |
| JPA Entity | `infrastructure/persistence/entity/` | `adapter/out/` |
| JPA Repository | `infrastructure/persistence/repository/` | `adapter/out/jpa/` |
| Mapper | `infrastructure/mapper/` | `adapter/out/mapper/` |
| Exception | `domain/exception/` | `application/exception/` |
| Service 구조 | 단일 Service 클래스 | UseCase별 분리 (Creator, Finder) |

#### 선택 가이드
- **표준 구조**: Domain Exception, Infrastructure 계층 명확히 분리 선호 시
- **실제 구조**: Adapter 중심, 실용적 구조 선호 시
- **프로젝트 일관성**: 기존 match 도메인 구조를 따라 새 도메인 구현 권장

## 3. Layer Definitions

### 3.1 Domain Layer
- **책임**: 핵심 비즈니스 로직, 비즈니스 규칙 검증
- **실제 프로젝트 구조**: `domain/` 하위에 직접 배치 (model 폴더 없음)
- **구성요소**:
  - **Domain Model**: `Match.java` (Pure POJO)
  - **Domain Enum**: `MatchStatus.java`
  - **Domain Service**: 복잡한 비즈니스 로직 (선택적)
  - **Value Object**: 불변 객체 (필요시)
- **제약사항**:
  - Spring, JPA, JSON 등 외부 라이브러리 의존성 금지
  - Adapter/Infrastructure 계층 참조 금지
  - Pure Java (POJO)만 허용
  - 생성자에서 모든 불변식(Invariant) 검증

### 3.2 Application Layer
- **책임**: Use Case 구현, 트랜잭션 관리, 도메인 객체 오케스트레이션
- **실제 프로젝트 구조**: Port와 Service를 명확히 분리
- **구성요소**:
  - **Inbound Port**: Use Case 인터페이스와 Command (`application/port/in/`)
    - `CreateMatchUseCase.java`: Use Case Interface
    - `CreateMatchCommand.java`: Command DTO
    - `MatchQueryUseCase.java`: Query Use Case
  - **Outbound Port**: Repository 인터페이스 (`application/port/out/`)
    - `MatchRepository.java`: Repository Interface (Adapter가 구현)
  - **Application Service**: Use Case 구현체 (`application/service/`)
    - `MatchCreator.java`: 생성 UseCase 구현
    - `MatchFinder.java`: 조회 UseCase 구현
  - **Exception**: Application 예외 (`application/exception/`)
    - `MatchNotFoundException`, `InvalidMatchDateException` 등
- **특징**:
  - UseCase별로 Service 클래스 분리 (Creator, Finder, Updater 등)
  - Command 패턴으로 입력 데이터 캡슐화
  - `@Transactional` 선언으로 트랜잭션 관리

### 3.3 Adapter (Out) Layer
- **책임**: 외부 시스템 연동 (DB, Kafka, External API)
- **실제 프로젝트 구조**: `adapter/out/`에 통합 배치
- **구성요소**:
  - **JPA Entity**: DB 테이블 매핑 (@Entity 허용) - `adapter/out/MatchEntity.java`
  - **JPA Repository**: Spring Data JPA Interface - `adapter/out/jpa/`
  - **Mapper**: Entity ↔ Domain Model 변환 - `adapter/out/mapper/`
  - **Repository Adapter**: Outbound Port 구현체 - `adapter/out/adapter/`
- **참고**: 표준 구조에서는 `infrastructure/` 패키지 사용 가능

### 3.4 Adapter (In) Layer - Presentation
- **책임**: HTTP 요청/응답 처리, DTO 변환
- **실제 프로젝트 구조**:
  - **Controller**: `adapter/in/web/MatchController.java`
  - **DTO**: `adapter/dto/` (Request/Response 통합)
    - `CreateMatchRequest.java`: record 타입
    - `MatchResponse.java`: record 타입
- **흐름**:
  1. HTTP Request 수신
  2. Request DTO → Command 변환
  3. UseCase 호출
  4. Domain Model → Response DTO 변환
  5. HTTP Response 반환
- **제약사항**:
  - 비즈니스 로직 포함 금지
  - Entity 직접 반환 금지
  - Java 17 `record` DTO 사용
  - Use Case 호출만 담당
  - Validation은 `@Valid`, `@NotNull` 등 Annotation 사용

## 4. Dependency Rule

### 4.1 의존성 흐름도
```
┌─────────────────────────────────────┐
│    Adapter (In) - Presentation      │
│        (adapter/in/web)             │
│            ↓                        │
│    ┌─────────────────────────┐     │
│    │   Application Layer      │     │
│    │  (application/service)   │     │
│    │  ┌──────────────────┐   │     │
│    │  │  Domain Layer    │   │     │
│    │  │    (domain/)     │   │     │
│    │  └──────────────────┘   │     │
│    └─────────────────────────┘     │
│            ↑                        │
│    Adapter (Out) - Persistence      │
│        (adapter/out/)               │
└─────────────────────────────────────┘

의존성 방향: 외부 → 내부 (Domain)
```

### 4.2 의존성 규칙
1. **Domain은 다른 레이어를 참조하지 않는다.**
   - Pure POJO만 허용
   - Spring, JPA 의존성 금지

2. **Application은 Domain만 참조한다.**
   - Port(Interface)를 정의하여 외부와 통신
   - 구체적인 구현체(Adapter)는 모른다

3. **Adapter (Out)는 Application/Domain의 Port를 구현한다.**
   - `MatchRepositoryImpl`이 `MatchRepository` 인터페이스 구현
   - JPA Entity, Mapper 포함

4. **Adapter (In)는 Application의 Use Case를 호출한다.**
   - Controller는 비즈니스 로직 없이 Use Case만 호출
   - DTO ↔ Command 변환 담당

### 4.3 실제 예시 (match 도메인)
```
MatchController (adapter/in/web)
    ↓ 호출
CreateMatchUseCase (application/port/in)
    ↑ 구현
MatchCreator (application/service)
    ↓ 사용
Match (domain)
    ↓ 참조
MatchRepository (application/port/out)
    ↑ 구현
MatchRepositoryImpl (adapter/out/adapter)
    ↓ 사용
MatchEntity (adapter/out)
```

## 5. UseCase 패턴 (실제 프로젝트 적용)

### 5.1 UseCase별 Service 분리
실제 프로젝트에서는 하나의 큰 Service 대신 UseCase별로 Service를 분리합니다.

**예시: match 도메인**
- `MatchCreator`: 경기 생성 UseCase 담당
- `MatchFinder`: 경기 조회 UseCase 담당
- `MatchUpdater`: 경기 수정 UseCase 담당 (향후)
- `MatchCanceller`: 경기 취소 UseCase 담당 (향후)

### 5.2 Command 패턴
입력 데이터를 캡슐화하여 UseCase에 전달합니다.

```java
// application/port/in/CreateMatchCommand.java
public record CreateMatchCommand(
    Long hostId,
    Long locationId,
    String title,
    String description,
    LocalDate matchDate,
    LocalTime startTime,
    LocalTime endTime,
    Integer maxParticipants
) {}

// application/port/in/CreateMatchUseCase.java
public interface CreateMatchUseCase {
    Match createMatch(CreateMatchCommand command);
}

// application/service/MatchCreator.java
public class MatchCreator implements CreateMatchUseCase {
    @Override
    public Match createMatch(CreateMatchCommand command) {
        // Use Case 로직 구현
    }
}
```

### 5.3 Query UseCase
조회 UseCase는 별도 인터페이스로 분리합니다.

```java
// application/port/in/MatchQueryUseCase.java
public interface MatchQueryUseCase {
    Match findById(Long matchId);
    List<Match> findByNearLocation(double latitude, double longitude, double radius);
}

// application/service/MatchFinder.java
public class MatchFinder implements MatchQueryUseCase {
    // 조회 로직 구현
}
```