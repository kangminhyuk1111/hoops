# Package Structure

> 기준: `/architecture-patterns` skill

## Standard Domain Package Structure

모든 도메인은 아래 구조를 따른다.

```
{domain}/
├── domain/                        # Pure Domain (POJO only)
│   ├── model/                     # Domain Models
│   │   ├── {Entity}.java          # Entity
│   │   └── {ValueObject}.java     # Value Object (optional)
│   └── port/                      # Outbound Port (from domain)
│       └── {Entity}Port.java      # Repository/Gateway interface
│
├── application/                   # Application Layer
│   ├── port/in/                   # Inbound Port
│   │   └── {Action}UseCase.java   # UseCase Interface
│   ├── dto/                       # UseCase Input/Output
│   │   ├── {Action}Command.java   # Input (Command)
│   │   └── {Action}Result.java    # Output (Result)
│   ├── service/                   # UseCase Implementation
│   │   └── {Action}Service.java
│   └── exception/                 # Application Exception
│       └── {Entity}{Reason}Exception.java
│
├── adapter/
│   ├── in/web/                    # REST Controller
│   │   ├── {Entity}Controller.java
│   │   └── dto/                   # HTTP Request/Response
│   │       ├── {Action}Request.java
│   │       └── {Entity}Response.java
│   └── out/persistence/           # JPA Implementation
│       ├── entity/
│       │   └── {Entity}Entity.java
│       ├── repository/
│       │   ├── {Entity}PersistenceAdapter.java  # Port implementation
│       │   └── {Entity}JpaRepository.java       # Spring Data JPA
│       └── mapper/
│           └── {Entity}Mapper.java
│
└── infrastructure/                # Framework concerns (NOT adapter)
    └── config/                    # Configuration classes
        └── {Feature}Config.java
```

## 핵심 구분

| 위치 | 목적 | 예시 |
|------|------|------|
| `domain/port/` | Outbound interface (추상화) | `UserPort`, `MatchPort` |
| `application/dto/` | UseCase Input/Output | `LoginCommand`, `LoginResult` |
| `adapter/in/web/dto/` | HTTP Request/Response | `LoginRequest`, `UserResponse` |
| `infrastructure/config/` | 설정 클래스 | `KakaoOAuthConfig`, `JwtConfig` |

## Layer Rules

| Layer | Allowed Dependencies | Forbidden |
|-------|---------------------|-----------|
| domain/ | Pure Java only | Spring, JPA, Lombok(@Data) |
| application/ | domain, port interfaces | adapter, infrastructure |
| adapter/in/ | application/port/in | adapter/out, domain directly |
| adapter/out/ | domain/port, domain/model | adapter/in |
| infrastructure/ | Spring Framework | domain, application |

## Naming Convention

| Type | Pattern | Example |
|------|---------|---------|
| Domain Model | `{Entity}` | `Match`, `User` |
| Outbound Port | `{Entity}Port` | `MatchPort`, `UserPort` |
| UseCase Interface | `{Action}UseCase` | `CreateMatchUseCase` |
| UseCase Impl | `{Action}Service` | `CreateMatchService` |
| Command | `{Action}Command` | `CreateMatchCommand` |
| Result | `{Action}Result` | `LoginResult` |
| Persistence Adapter | `{Entity}PersistenceAdapter` | `MatchPersistenceAdapter` |
| JPA Repository | `{Entity}JpaRepository` | `MatchJpaRepository` |
| JPA Entity | `{Entity}Entity` | `MatchEntity` |
| HTTP Request | `{Action}Request` | `CreateMatchRequest` |
| HTTP Response | `{Entity}Response` | `MatchResponse` |
| Config | `{Feature}Config` | `KakaoOAuthConfig` |

## Example: Auth Domain

```
auth/
├── domain/
│   ├── model/
│   │   ├── AuthAccount.java
│   │   └── AuthProvider.java
│   └── port/
│       └── AuthAccountPort.java
│
├── application/
│   ├── port/in/
│   │   ├── KakaoLoginUseCase.java
│   │   ├── SignupUseCase.java
│   │   └── RefreshTokenUseCase.java
│   ├── dto/
│   │   ├── KakaoUserInfo.java
│   │   ├── SignupCommand.java
│   │   ├── TokenResult.java
│   │   └── LoginResult.java
│   ├── service/
│   │   ├── KakaoCallbackHandler.java
│   │   ├── SignupProcessor.java
│   │   └── TokenRefresher.java
│   └── exception/
│       └── InvalidAuthCodeException.java
│
├── adapter/
│   ├── in/web/
│   │   ├── AuthController.java
│   │   └── dto/
│   │       ├── SignupRequest.java
│   │       └── TokenResponse.java
│   └── out/
│       ├── persistence/
│       │   ├── entity/
│       │   │   └── AuthAccountEntity.java
│       │   ├── repository/
│       │   │   ├── AuthAccountPersistenceAdapter.java
│       │   │   └── AuthAccountJpaRepository.java
│       │   └── mapper/
│       │       └── AuthAccountMapper.java
│       └── oauth/
│           └── KakaoOAuthClientImpl.java
│
└── infrastructure/
    └── config/
        ├── KakaoOAuthConfig.java
        └── JwtConfig.java
```

## Common Package

공통 모듈은 `common/` 패키지에 위치한다.

```
common/
├── exception/        # Base Exception Classes
│   ├── BusinessException.java
│   ├── DomainException.java
│   └── ApplicationException.java
├── security/         # Security Configuration
└── response/         # Common Response (ApiResponse, etc.)
```
