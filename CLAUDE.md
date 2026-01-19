# CLAUDE.md

## Skills
- `/architecture-patterns` - Hoops 프로젝트 Hexagonal Architecture 가이드
- `/debugging-strategies` - 디버깅 전략 가이드
- `/code-review-excellence` - 코드 리뷰 가이드

### Skill 사용 규칙 (필수)
1. **아키텍처 관련 작업 시 반드시 `/architecture-patterns` skill 참조**
2. **Skill 내용과 다른 구조 제안 금지**: skill이 정의한 구조를 따름

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

## Test

- **Acceptance Test**: Cucumber + Testcontainers (`backend/src/test/java/com/hoops/acceptance/`)
- **Integration Test**: JUnit 5 + Testcontainers (`backend/src/test/java/com/hoops/integration/`)
- Cucumber 스텝 작성 전 기존 스텝 중복 확인 필수

## Workflow

1. 기능 구현 전 `/docs/spec/mvp-features.md` 확인
2. 사용자에게 구현할 기능 선택 요청
3. Cucumber 시나리오 작성 -> 테스트 실패 확인 -> 구현 -> 리팩토링
4. 문서 업데이트 후 커밋
