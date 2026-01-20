# Coding Convention

## Language

All code must be written in **English only**.

- Comments: English
- Log messages: English
- Exception messages: English
- Javadoc: English
- Variable/Method names: English

## Naming Convention

### Port (Interface)

| Type | Pattern | Example |
|------|---------|---------|
| Inbound Port | `{Action}UseCase` | `CreateMatchUseCase` |
| Outbound Port | `{Entity}Port` | `AuthAccountPort`, `UserPort` |
| External Service Port | `{Service}Client` | `KakaoOAuthClient` |

### Adapter (Implementation)

| Type | Pattern | Example |
|------|---------|---------|
| Port Implementation | `{Entity}PersistenceAdapter` | `AuthAccountPersistenceAdapter` |
| JPA Repository | `{Entity}JpaRepository` | `AuthAccountJpaRepository` |
| External Client Impl | `{Service}ClientAdapter` | `KakaoOAuthClientAdapter` |

### Domain

| Type | Pattern | Example |
|------|---------|---------|
| Entity | `{Name}` | `Match`, `User`, `AuthAccount` |
| Value Object | `{Name}` | `Email`, `Money` |
| Domain Service | `{Name}Service` | `MatchValidationService` |

### Application

| Type | Pattern | Example |
|------|---------|---------|
| UseCase Impl | `{Action}Service` | `CreateMatchService` |
| Command | `{Action}Command` | `CreateMatchCommand` |
| Query | `{Action}Query` | `GetMatchQuery` |

### Exception

| Type | Pattern | Example |
|------|---------|---------|
| Domain Exception | `{Entity}{Reason}Exception` | `MatchNotFoundException` |
| Application Exception | `{Action}{Reason}Exception` | `SignupFailedException` |

## Log Messages

```java
// Good
log.info("Kakao auth URL requested");
log.error("Failed to fetch user info: userId={}", userId);

// Bad
log.info("카카오 인증 URL 요청");
log.error("사용자 정보 조회 실패: userId={}", userId);
```

## Exception Messages

```java
// Good
throw new MatchNotFoundException("Match not found: " + matchId);

// Bad
throw new MatchNotFoundException("경기를 찾을 수 없습니다: " + matchId);
```

## Comments

```java
// Good
/**
 * Creates a new match with the given command.
 *
 * @param command match creation command
 * @return created match
 */

// Bad
/**
 * 주어진 커맨드로 새 경기를 생성합니다.
 */
```

## Bean Validation Messages

```java
// Good
@NotBlank(message = "Nickname is required")
@Size(min = 2, max = 20, message = "Nickname must be between 2 and 20 characters")

// Bad
@NotBlank(message = "닉네임은 필수입니다")
@Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다")
```
