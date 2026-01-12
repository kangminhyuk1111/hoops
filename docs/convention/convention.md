# Coding Convention

### 1. Naming
- **Class**: `PascalCase`
- **Method/Variable**: `camelCase`
- **Test Method**: `should_DoSomething_When_GivenSomething` (Snake Case 허용)
- **Package**:
  - Domain Model: `{domain}.domain.model`
  - JPA Entity: `{domain}.infrastructure.persistence.entity`
  - DTO: `{domain}.adapter.in.web.dto` (Java 17 `record` 사용)
  - Mapper: `{domain}.infrastructure.mapper` (예: `UserEntityMapper`)

### 2. Code Style
- **No Lombok**: Lombok 사용을 전면 금지한다. 모든 생성자, Getter, Setter는 명시적으로 작성한다.(왜? 인지 정의)
- **Constructor Injection**: 의존성 주입은 생성자 방식만 허용한다. `@Autowired` 필드 주입 금지.
- **DTO with Record**: DTO는 Java 17 `record` 타입을 사용한다.

### 3. Domain vs Entity Separation
- **Domain Model**: Pure Java (POJO), 외부 라이브러리 의존성 없음
- **JPA Entity**: JPA Annotation 허용, 단순 데이터 저장 목적
- **Mapper**: Entity ↔ Domain Model 변환 담당

### 4. Exception Handling

#### 4.1 예외 계층 구조
프로젝트는 계층별로 명확한 예외 체계를 가진다. `RuntimeException`을 직접 던지지 말고, 의미 있는 커스텀 예외를 사용한다.

```
RuntimeException
└── BusinessException (추상 클래스)
    ├── DomainException (도메인 규칙 위반)
    │   ├── InvalidUserNameException
    │   ├── DuplicateEmailException
    │   └── UserNotFoundException
    ├── ApplicationException (유스케이스 실패)
    │   ├── UserCreationFailedException
    │   └── InvalidCommandException
    └── InfrastructureException (인프라 오류)
        ├── DatabaseConnectionException
        └── ExternalApiException
```

#### 4.2 예외 클래스 정의
- **Base Exception**: BusinessException (errorCode 필드 포함)
- **Domain Exception**: 도메인 규칙 위반 시 발생
- **Application Exception**: 유스케이스 실행 실패 시 발생
- **Infrastructure Exception**: 외부 시스템 연동 실패 시 발생

#### 4.3 예외 처리 전략
- **Domain Layer**: 비즈니스 규칙 검증 시 즉시 예외 발생
- **Application Layer**: 유스케이스 실행 중 예외 포착 및 변환
- **Presentation Layer**: `@RestControllerAdvice`를 통한 전역 예외 처리

#### 4.4 예외 네이밍 규칙
- **Domain Exception**: `{Entity}{Reason}Exception` (예: `UserNotFoundException`, `InvalidEmailException`)
- **Application Exception**: `{UseCase}FailedException` (예: `UserCreationFailedException`)
- **Infrastructure Exception**: `{Technology}{Issue}Exception` (예: `DatabaseConnectionException`)
- **Error Code**: `UPPER_SNAKE_CASE` (예: `USER_NOT_FOUND`, `DUPLICATE_EMAIL`)

#### 4.5 예외 사용 가이드
**Good Practice**
- 명확한 예외 타입과 메시지 사용
- 원인 예외를 포함하여 재던지기
- 도메인 규칙 위반 시 즉시 예외 발생

**Bad Practice**
- `RuntimeException` 직접 사용 금지
- 예외 무시 금지
- 너무 포괄적인 예외 처리 금지
- 예외를 로그만 찍고 재던지지 않는 것 금지

#### 4.6 HTTP Status Code 매핑
| 예외 타입 | HTTP Status | 설명 |
|----------|-------------|------|
| `UserNotFoundException` | 404 NOT_FOUND | 리소스를 찾을 수 없음 |
| `DuplicateEmailException` | 400 BAD_REQUEST | 잘못된 요청 (중복) |
| `InvalidUserNameException` | 400 BAD_REQUEST | 유효하지 않은 입력 |
| `ApplicationException` | 500 INTERNAL_SERVER_ERROR | 유스케이스 실패 |
| `InfrastructureException` | 503 SERVICE_UNAVAILABLE | 외부 시스템 오류 |
| `Unexpected Exception` | 500 INTERNAL_SERVER_ERROR | 예상치 못한 오류 |

### 5. Implementation Tips (Anti-Patterns)
- **N+1 문제**: JPA 사용 시 `Fetch Join` 또는 `@BatchSize` 설정을 우선 고려할 것.
- **Stream API**: 가독성을 해치지 않는 선에서 Java Stream API를 적극 활용할 것.
- **메서드 책임 단일화**: 메서드는 하나의 책임만 가져야 하며, 20라인을 초과할 경우 분리를 고려한다.
- **Entity 직접 반환 금지**: Controller에서 JPA Entity나 Domain Model을 직접 반환하지 말고, 반드시 `record` DTO로 변환한다.
