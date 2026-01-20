# CLAUDE.md

## Skills
- `/architecture-patterns` - Hoops 프로젝트 Hexagonal Architecture 가이드
- `/tdd-workflow` - ATDD 워크플로우 (Cucumber 시나리오 우선)
- `/create-pull-request` - PR 생성 가이드 (테스트 필수)
- `/code-review-excellence` - 코드 리뷰 가이드
- `/debugging-strategies` - 디버깅 전략 가이드
- `/skill-creator` - 새 스킬 생성 가이드

### Skill 사용 규칙 (필수)
1. **아키텍처 관련 작업 시 반드시 `/architecture-patterns` skill 참조**
2. **기능 구현 시 `/tdd-workflow` skill 참조** (Cucumber 시나리오 우선 작성)
3. **PR 생성 시 반드시 `/create-pull-request` skill 참조**
4. **Skill 내용과 다른 구조 제안 금지**: skill이 정의한 구조를 따름

## Role
- Java/Spring 생태계에 정통한 시니어 개발자
- 불확실한 요구사항은 추측하지 말고 반드시 질문
- 문서 기반 작업 (코드 변경 시 관련 문서 함께 수정)

## Tech Stack
- Java 17, Spring Boot 3.x, JPA, MySQL
- Kafka, JUnit 5, Cucumber, Testcontainers

## Package Structure

> 상세 내용은 `/architecture-patterns` skill 참조

```
{domain}/
├── domain/                      # 순수 POJO (No Spring, JPA)
│   ├── model/                   # Identity를 가진 도메인 모델
│   ├── vo/                      # Value Objects (불변)
│   └── exception/               # 도메인 규칙 위반 예외
├── application/
│   ├── port/in/                 # Inbound Port (*UseCase)
│   ├── port/out/                # Outbound Port (*Port)
│   ├── service/                 # UseCase 구현체
│   ├── dto/                     # Command, Response
│   └── exception/               # UseCase 실패 예외
├── adapter/
│   ├── in/web/                  # Controller
│   │   └── dto/                 # Request/Response DTO
│   └── out/
│       ├── persistence/         # JPA Entity, Repository 구현
│       └── {external}/          # 외부 API Adapter
│           └── exception/       # 외부 API 예외
└── infrastructure/
    └── config/                  # Spring Configuration
```

## Strict Rules

1. **Pure Domain**: `domain/` 패키지는 외부 프레임워크 의존 금지 (Spring, JPA, Lombok @Data)
2. **Constructor Injection**: `@Autowired` 필드 주입 금지
3. **DTO 사용**: Entity를 Controller에서 직접 반환 금지, `record` 타입 DTO 사용
4. **Testcontainers**: 테스트 DB는 MySQL Testcontainers 사용 (H2 금지)
5. **외부 API Mocking**: 외부 API(카카오 등)는 WireMock 사용

## Code Quality Rules

- **메서드 depth**: 최대 2 (중첩 if/for 지양)
- **메서드 길이**: 최대 20줄
- **중첩 null 체크**: `Optional` 사용
- **단일 책임**: 메서드는 한 가지 일만 수행

## Object-Oriented Design Principles

### SOLID 원칙

| 원칙 | 설명 | 위반 예시 |
|------|------|-----------|
| **SRP** (단일 책임) | 클래스/메서드는 하나의 책임만 가짐 | `validateAndCreate()` - 검증과 생성 분리 필요 |
| **OCP** (개방-폐쇄) | 확장에 열려있고 수정에 닫혀있음 | if-else 분기 대신 다형성 활용 |
| **LSP** (리스코프 치환) | 하위 타입은 상위 타입을 대체 가능 | - |
| **ISP** (인터페이스 분리) | 클라이언트별 인터페이스 분리 | 불필요한 메서드 의존 금지 |
| **DIP** (의존 역전) | 추상화에 의존, 구체화에 의존 금지 | Port/Adapter 패턴 준수 |

### Domain Model 생성 규칙

1. **정적 팩토리 메서드 사용**: 생성자 직접 호출 금지, 의미있는 팩토리 메서드 제공
2. **null 주입 금지**: 생성자에 null 전달 금지, 필수 필드는 반드시 값 제공
3. **불변 객체 선호**: 가능한 final 필드 사용, setter 지양

```java
// Good - 정적 팩토리 메서드
public class Match {
    public static Match create(Long hostId, Long locationId, ...) {
        // 검증 로직 포함 가능
        return new Match(null, hostId, locationId, ...);
    }

    public static Match withId(Long id, Long hostId, ...) {
        return new Match(id, hostId, ...);
    }
}

// Bad - 서비스에서 null 직접 주입
Match match = new Match(null, hostId, locationId, ...);
```

### 검증 로직 분리 원칙

1. **검증과 생성 분리**: `validateAndCreate()` 금지 → `validate()` + `create()` 분리
2. **서비스에서 if-else 검증 금지**: 도메인 객체가 스스로 검증하거나 Validator 객체 사용
3. **상태 검증은 도메인 객체 내부에서**: 객체가 자신의 상태를 검증

```java
// Good - 도메인 객체가 스스로 검증
public class Match {
    public void reactivate() {
        validateReactivatable();  // 내부 검증
        this.status = MatchStatus.RECRUITING;
    }

    private void validateReactivatable() {
        if (this.status != MatchStatus.CANCELLED) {
            throw new MatchCannotReactivateException(this.id);
        }
    }
}

// Bad - 서비스에서 if-else 분기 검증
public class MatchReactivator {
    public void reactivate(Match match) {
        if (match.getStatus() != MatchStatus.CANCELLED) {
            throw new MatchCannotReactivateException(match.getId());
        }
        match.setStatus(MatchStatus.RECRUITING);
    }
}
```

### Policy/Validator 설계 원칙

1. **단일 책임**: Validator는 검증만, 생성/변경은 별도 책임
2. **검증 결과 반환**: void 대신 검증 결과 객체 반환 권장
3. **도메인 로직은 도메인에**: Policy/Validator는 복잡한 교차 검증에만 사용

```java
// Good - 검증만 수행
public class MatchPolicyValidator {
    public void validate(CreateMatchCommand command) {
        validateTimeRange(command);
        validateMaxParticipants(command);
    }
}

// Bad - 검증 + 생성 혼합
public class MatchPolicyValidator {
    public Match validateAndCreate(CreateMatchCommand command) { ... }
}
```

## Exception Rules

> 금지: `RuntimeException`, `IllegalArgumentException`, `IllegalStateException` 직접 사용

```
BusinessException (추상)
├── DomainException (도메인 규칙 위반)
└── ApplicationException (유스케이스 실패)
```

| HTTP Status | 에러 코드 패턴 | 예시 |
|-------------|---------------|------|
| 404 | `*_NOT_FOUND` | `MATCH_NOT_FOUND` |
| 409 | `ALREADY_*`, `DUPLICATE_*` | `ALREADY_PARTICIPATING` |
| 403 | `NOT_HOST*`, `NOT_PARTICIPANT*` | `NOT_HOST` |
| 400 | `INVALID_*`, `*_EXCEEDED` | `INVALID_TIME_RANGE` |

```java
public class MatchNotFoundException extends DomainException {
    private static final String ERROR_CODE = "MATCH_NOT_FOUND";

    public MatchNotFoundException(Long id) {
        super(ERROR_CODE, "Match not found: " + id);
    }
}
```

## Documentation

```
docs/
├── spec/           # 기능 스펙, 스키마, 패키지 구조
│   ├── SPEC.md
│   ├── schema.md
│   └── mvp-features.md
└── api/            # API 명세
```

### 문서 작성 규칙
- **ASCII 다이어그램 금지**: 그림 형태의 텍스트(박스, 화살표 등) 사용 금지
- 구조 설명은 **테이블** 또는 **목록** 형태로 작성

## Test

- **Acceptance Test**: Cucumber + Testcontainers (`backend/src/test/java/com/hoops/acceptance/`)
- **Integration Test**: JUnit 5 + Testcontainers (`backend/src/test/java/com/hoops/integration/`)
- Cucumber 스텝 작성 전 기존 스텝 중복 확인 필수

### PR 생성 전 필수 테스트

> ⚠️ **PR 생성 전 반드시 모든 테스트 통과 필수**

```bash
./gradlew test
```

테스트 미통과 시 PR 생성 금지. CI에서 테스트 실패로 PR이 머지되지 않습니다.

## Workflow

1. 기능 구현 전 `/docs/spec/mvp-features.md` 확인
2. 사용자에게 구현할 기능 선택 요청
3. Cucumber 시나리오 작성 → 테스트 실패 확인 → 구현 → 리팩토링
4. **`./gradlew test` 실행하여 모든 테스트 통과 확인**
5. 문서 업데이트 후 커밋
6. PR 생성 (`/create-pull-request` skill 참조)
