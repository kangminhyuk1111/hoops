---
name: architecture-patterns
description: Hoops 프로젝트 Hexagonal Architecture 가이드. Java/Spring 기반 도메인 설계 및 패키지 구조 정의.
---

# Hexagonal Architecture Guidelines for Hoops Project

You are an expert Java/Spring backend developer working on the Hoops project. You must follow hexagonal architecture (ports and adapters) with domain-driven design principles. These guidelines are mandatory for all code generation, review, and refactoring tasks.

## Core Principles

The dependency direction must always flow inward: adapter → application → domain. The domain layer is the core and must have zero external dependencies. Never violate this direction under any circumstances.

## Layer Definitions and Constraints

### Domain Layer

Location: `{domain}/domain/`

The domain layer contains pure business logic and domain concepts. This layer must remain completely isolated from frameworks and infrastructure concerns.

**Permitted elements**: Pure Java POJOs, Lombok annotations limited to `@Getter`, `@Builder`, and `@AllArgsConstructor` only.

**Prohibited elements**: Spring annotations, JPA annotations, Lombok `@Data`, any framework dependencies, any reference to application or adapter layer.

The domain layer contains five subpackages:
- `model/` - Entities with identity
- `vo/` - Immutable value objects
- `repository/` - Repository interfaces following DDD pattern
- `policy/` - Cross-entity validation logic
- `exception/` - Domain rule violation exceptions

### Application Layer

Location: `{domain}/application/`

The application layer orchestrates domain objects to fulfill use cases. This layer may depend only on the domain layer.

**Permitted elements**: Spring `@Service` and `@Transactional` annotations only.

**Prohibited elements**: JPA annotations, HTTP-related code, direct external API calls, any reference to adapter layer.

The application layer contains five subpackages:
- `port/in/` - Inbound port interfaces defining use cases
- `port/out/` - Outbound port interfaces for external services
- `service/` - Use case implementations
- `dto/` - Command and response objects for use case input/output
- `exception/` - Use case failure exceptions

### Adapter Layer

Location: `{domain}/adapter/`

The adapter layer connects the application to the external world. This layer may depend on both application and domain layers.

**Permitted elements**: All framework annotations including Spring MVC, JPA, and external library integrations.

The adapter layer is divided into inbound and outbound sections:
- Inbound adapters in `adapter/in/web/` contain controllers and web DTOs
- Outbound adapters in `adapter/out/persistence/` contain JPA entities and repository implementations
- External service adapters in `adapter/out/{external}/{provider}/` contain vendor-specific implementations organized by provider

### Infrastructure Layer

Location: `{domain}/infrastructure/`

The infrastructure layer contains framework configuration only.
- `config/` - Spring configuration classes

## Naming Conventions

### Interface and Class Naming

| Type | Suffix | Location | Example |
|------|--------|----------|---------|
| Inbound ports | `*UseCase` | `application/port/in/` | `OAuthLoginUseCase`, `CreateMatchUseCase` |
| Outbound ports | `*Port` | `application/port/out/` | `OAuthPort`, `JwtTokenPort`, `NotificationPort` |
| Domain repositories | `*Repository` | `domain/repository/` | `AuthAccountRepository`, `MatchRepository` |
| Persistence adapters | `*JpaAdapter` | `adapter/out/persistence/` | `AuthAccountJpaAdapter` |
| JPA entities | `*JpaEntity` | `adapter/out/persistence/` | `AuthAccountJpaEntity`, `MatchJpaEntity` |

### Vendor-Neutral Naming

Domain and application layer objects must never contain vendor-specific names. Use generic names that describe the concept, not the implementation.

| Incorrect | Correct |
|-----------|---------|
| `KakaoUserInfo` | `OAuthUserInfo` |
| `KakaoOAuthPort` | `OAuthPort` |
| `GoogleTokenResponse` | `OAuthTokenInfo` |

Vendor-specific names are permitted only in the adapter layer under the appropriate provider directory.
Example: `adapter/out/oauth/kakao/KakaoOAuthAdapter`

## Domain Model Design Rules

### Factory Method Pattern

Domain models must use private constructors with static factory methods. Direct constructor calls are prohibited.

- `create()` - For creating new entities without an id. This method must perform validation before construction.
- `reconstitute()` - For restoring entities from the database with an existing id. This method must not perform validation as data is assumed valid.

```java
public class Match {
    private final Long id;
    private final Long hostId;
    private MatchStatus status;

    private Match(Long id, Long hostId, MatchStatus status) {
        this.id = id;
        this.hostId = hostId;
        this.status = status;
    }

    public static Match create(Long hostId, LocalDateTime matchDate) {
        validateMatchDate(matchDate);
        return new Match(null, hostId, MatchStatus.RECRUITING);
    }

    public static Match reconstitute(Long id, Long hostId, MatchStatus status) {
        return new Match(id, hostId, status);
    }
}
```

### Tell, Don't Ask Principle

Domain objects must perform their own validation and state changes internally. Do not query object state from outside to make decisions. Request behavior instead.

```java
// Prohibited pattern: asking then deciding externally
if (match.getStatus() != MatchStatus.CANCELLED) {
    throw new MatchCannotReactivateException(match.getId());
}
if (!match.getHostId().equals(userId)) {
    throw new NotMatchHostException(match.getId());
}
match.setStatus(MatchStatus.RECRUITING);

// Required pattern: telling the object to perform behavior
match.reactivate(userId);

// Inside Match class
public void reactivate(Long requestUserId) {
    validateHost(requestUserId);
    validateReactivatable();
    this.status = MatchStatus.RECRUITING;
}
```

### Immutability Rules

- Value objects must be completely immutable with all fields declared as `final`. Use Java record types for value objects when possible.
- Entity identifiers must be immutable. State changes must occur through behavior methods, never through setters.
- When an immutable object needs modification, provide `with*()` methods that return a new instance.

```java
public AuthAccount withRefreshToken(String newToken) {
    return new AuthAccount(this.id, this.userId, this.provider, newToken);
}
```

- Collection fields must return defensive copies or unmodifiable views to prevent external modification.

### Validation Location Rules

| Validation Type | Location | Example |
|-----------------|----------|---------|
| Single object state | Domain Model internal | `match.reactivate(userId)` |
| Cross-entity validation | `domain/policy/` | Time range, overlap check |
| Requires external dependency | `application/service/` | DB lookup required |

## Exception Design

### Exception Hierarchy

All custom exceptions must extend from `BusinessException` which is abstract.
- `DomainException` extends `BusinessException` for domain rule violations
- `ApplicationException` extends `BusinessException` for use case failures

Never throw `RuntimeException`, `IllegalArgumentException`, or `IllegalStateException` directly. Always create specific exception classes.

### Exception Location Rules

| Location | Usage | Example |
|----------|-------|---------|
| `domain/exception/` | Domain rule violations | `InvalidNicknameException`, `MatchAlreadyFullException` |
| `application/exception/` | Use case failures | `DuplicateNicknameException`, `InvalidTempTokenException` |
| `adapter/out/{external}/{provider}/exception/` | Vendor-specific API failures | `KakaoApiException`, `InvalidAuthCodeException` |

### Error Code Naming Patterns

| HTTP Status | Pattern | Example |
|-------------|---------|---------|
| 404 | `*_NOT_FOUND` | `MATCH_NOT_FOUND`, `USER_NOT_FOUND` |
| 409 | `ALREADY_*`, `DUPLICATE_*` | `ALREADY_PARTICIPATING`, `DUPLICATE_NICKNAME` |
| 403 | `NOT_*` | `NOT_HOST`, `NOT_PARTICIPANT` |
| 400 | `INVALID_*`, `*_EXCEEDED` | `INVALID_MATCH_TIME`, `CANCEL_TIME_EXCEEDED` |

## DTO Design Rules

### DTO Types and Locations

| Type | Suffix | Location | Example |
|------|--------|----------|---------|
| Use case input | `*Command` | `application/dto/` | `CreateMatchCommand`, `SignupCommand` |
| Use case output | `*Result` | `application/dto/` | `OAuthCallbackResult`, `MatchDetailResult` |
| HTTP request | `*Request` | `adapter/in/web/dto/` | `CreateMatchRequest`, `SignupRequest` |
| HTTP response | `*Response` | `adapter/in/web/dto/` | `MatchDetailResponse`, `AuthUrlResponse` |

### DTO Implementation Rules

- All DTOs must be implemented as Java record types for immutability and conciseness.
- Never expose domain entities directly from controllers. Always convert to response DTOs.
- Never accept domain entities as controller parameters. Always use request DTOs and convert to commands.

## Transaction Rules

- Apply `@Transactional` annotation only at the use case implementation level in the service layer.
- Use `@Transactional(readOnly = true)` for all query operations to optimize performance.
- Never apply `@Transactional` in domain layer or adapter layer.
- Declare propagation level explicitly when non-default behavior is required.

## Dependency Injection Rules

- Use constructor injection exclusively. Never use `@Autowired` field injection or setter injection.
- Apply `@RequiredArgsConstructor` from Lombok to generate constructors.
- Declare all injected dependencies as `private final` fields.

## Code Quality Standards

| Metric | Limit |
|--------|-------|
| Method nesting depth | Maximum 2 levels |
| Method length | Maximum 20 lines |
| Class length | Maximum 200 lines |
| Method parameters | Maximum 5 |

- Use `Optional` for return types that may be absent. Never return null from methods.
- Return empty collections instead of null for collection return types.
- Refactor nested conditionals and loops into separate methods.
- Extract complex logic into well-named private methods.
- Split large classes by responsibility.
- Use command objects for methods requiring more parameters.

## Adapter Implementation Patterns

### Persistence Adapter Pattern

Persistence adapters must implement domain repository interfaces and handle all JPA entity conversions internally.

```java
@Repository
@RequiredArgsConstructor
public class AuthAccountJpaAdapter implements AuthAccountRepository {
    private final SpringDataAuthAccountRepository jpaRepository;

    @Override
    public AuthAccount save(AuthAccount authAccount) {
        AuthAccountJpaEntity entity = toJpaEntity(authAccount);
        AuthAccountJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private AuthAccountJpaEntity toJpaEntity(AuthAccount domain) { /* ... */ }
    private AuthAccount toDomain(AuthAccountJpaEntity entity) { /* ... */ }
}
```

### External Service Adapter Pattern

External service adapters must implement outbound port interfaces and convert vendor-specific responses to domain value objects.

Organize adapters by external service type and then by provider.
Example: `adapter/out/oauth/kakao/`, `adapter/out/oauth/google/`, `adapter/out/payment/toss/`

```java
@Component
@RequiredArgsConstructor
public class KakaoOAuthAdapter implements OAuthPort {
    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        KakaoUserResponse response = callKakaoApi(accessToken);
        return toOAuthUserInfo(response);
    }

    private OAuthUserInfo toOAuthUserInfo(KakaoUserResponse response) { /* ... */ }
}
```

## Prohibited Patterns Summary

- Never allow dependencies to flow outward from domain to application or adapter layers.
- Never use framework annotations in the domain layer except permitted Lombok annotations.
- Never use `@Autowired` field injection anywhere in the codebase.
- Never throw generic exceptions like `RuntimeException` or `IllegalArgumentException`.
- Never expose JPA entities outside the persistence adapter.
- Never use vendor-specific names in domain or application layers.
- Never call external services directly from domain or application layers without going through ports.
- Never use setters for state changes in domain entities.
- Never return null from methods when `Optional` or empty collection is appropriate.
- Never place `@Transactional` in domain layer or adapter layer.

## Package Structure Reference

```
{domain}/
├── domain/
│   ├── model/
│   ├── vo/
│   ├── repository/
│   ├── policy/
│   └── exception/
├── application/
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   ├── service/
│   ├── dto/
│   └── exception/
├── adapter/
│   ├── in/
│   │   └── web/
│   │       └── dto/
│   └── out/
│       ├── persistence/
│       └── {external}/
│           └── {provider}/
│               └── exception/
└── infrastructure/
    └── config/
```

## Implementation Checklist

When creating a new domain feature, verify each component exists in the correct location.

### Domain layer checklist
- [ ] Domain model in `domain/model/` with factory methods
- [ ] Value objects in `domain/vo/` as records
- [ ] Repository interface in `domain/repository/`
- [ ] Policy classes in `domain/policy/` if cross-entity validation needed
- [ ] Domain exceptions in `domain/exception/`

### Application layer checklist
- [ ] UseCase interface in `application/port/in/`
- [ ] Outbound port interface in `application/port/out/` if external service needed
- [ ] Service implementation in `application/service/`
- [ ] Command and result DTOs in `application/dto/`
- [ ] Application exceptions in `application/exception/`

### Adapter layer checklist
- [ ] Controller in `adapter/in/web/`
- [ ] Request and response DTOs in `adapter/in/web/dto/`
- [ ] JPA entity and adapter in `adapter/out/persistence/`
- [ ] External service adapter in `adapter/out/{external}/{provider}/` if needed

### Infrastructure layer checklist
- [ ] Configuration class in `infrastructure/config/` if needed
