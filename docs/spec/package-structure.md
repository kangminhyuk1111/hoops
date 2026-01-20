# Package Structure

> 기준: `/architecture-patterns` skill

## Standard Domain Package Structure

모든 도메인은 아래 구조를 따른다.

```
{domain}/
├── domain/                           # 순수 POJO (No Spring, JPA)
│   ├── model/                        # Identity를 가진 도메인 모델
│   │   └── {Entity}.java
│   ├── vo/                           # Value Objects (불변)
│   │   └── {ValueObject}.java
│   ├── repository/                   # DDD Repository (인터페이스)
│   │   └── {Entity}Repository.java
│   ├── exception/                    # 도메인 규칙 위반 예외
│   │   └── {Rule}Exception.java
│   └── policy/                       # 도메인 정책 검증 (순수 POJO)
│       └── {Entity}PolicyValidator.java
│
├── application/                      # 애플리케이션 계층
│   ├── port/
│   │   ├── in/                       # Inbound Port (*UseCase)
│   │   │   └── {Action}UseCase.java
│   │   └── out/                      # Outbound Port (*Port, 외부 서비스)
│   │       └── {Service}Port.java
│   ├── dto/                          # UseCase I/O
│   │   ├── {Action}Command.java      # Input
│   │   └── {Info}.java               # Output/Data Transfer
│   ├── service/                      # UseCase 구현체
│   │   └── {Action}Service.java
│   └── exception/                    # UseCase 실패 예외
│       └── {Entity}{Reason}Exception.java
│
├── adapter/
│   ├── in/
│   │   └── web/                      # REST Controller
│   │       ├── {Entity}Controller.java
│   │       └── dto/                  # HTTP Request/Response
│   │           ├── {Action}Request.java
│   │           └── {Entity}Response.java
│   └── out/
│       ├── persistence/              # JPA 영속성 관련
│       │   ├── {Entity}JpaEntity.java
│       │   ├── {Entity}JpaAdapter.java       # Repository 구현
│       │   ├── SpringData{Entity}Repository.java
│       │   └── {Entity}Mapper.java
│       └── {external}/               # 외부 API Adapter
│           └── {Service}Adapter.java
│
└── infrastructure/
    └── config/                       # Spring Configuration
        └── {Domain}Config.java       # Bean 등록 등
```

## 핵심 원칙

### 1. Repository vs Port

| 구분 | 위치 | 용도 | 예시 |
|------|------|------|------|
| Repository | `domain/repository/` | 영속성 추상화 (DDD) | `MatchRepository` |
| Port | `application/port/out/` | 외부 서비스/ACL | `HostInfoPort`, `OAuthPort` |

### 2. DTO 위치

| 위치 | 역할 | 예시 |
|------|------|------|
| `application/dto/` | UseCase Input/Output | `CreateMatchCommand`, `HostInfo` |
| `adapter/in/web/dto/` | HTTP Request/Response | `CreateMatchRequest`, `MatchResponse` |
| `domain/vo/` | 도메인 개념 (불변) | `MatchStatus`, `TokenPair` |

### 3. 예외 분류

| 위치 | 용도 | 예시 |
|------|------|------|
| `domain/exception/` | 도메인 규칙 위반 | `InvalidTimeRangeException` |
| `application/exception/` | UseCase 실패 | `MatchNotFoundException`, `NotMatchHostException` |

## Layer Rules

| Layer | Allowed Dependencies | Forbidden |
|-------|---------------------|-----------|
| domain/ | Pure Java only | Spring, JPA, Lombok(@Data) |
| application/ | domain only | adapter, infrastructure |
| adapter/in/ | application/port/in | adapter/out, domain directly |
| adapter/out/ | domain/repository, application/port/out | adapter/in |
| infrastructure/ | Spring Framework | domain, application |

## Naming Convention

| Type | Pattern | Example |
|------|---------|---------|
| Domain Model | `{Entity}` | `Match`, `User`, `AuthAccount` |
| Value Object | `{Concept}` | `MatchStatus`, `TokenPair` |
| Repository Interface | `{Entity}Repository` | `MatchRepository` |
| Outbound Port | `{Service}Port` | `HostInfoPort`, `OAuthPort` |
| UseCase Interface | `{Action}UseCase` | `CreateMatchUseCase` |
| UseCase Impl | `{Action}Service` or `{Actor}` | `MatchCreator`, `OAuthLoginService` |
| Command | `{Action}Command` | `CreateMatchCommand` |
| JPA Entity | `{Entity}JpaEntity` | `MatchJpaEntity` |
| JPA Adapter | `{Entity}JpaAdapter` | `MatchJpaAdapter` |
| Spring Data | `SpringData{Entity}Repository` | `SpringDataMatchRepository` |
| HTTP Request | `{Action}Request` | `CreateMatchRequest` |
| HTTP Response | `{Entity}Response` | `MatchResponse` |
| Config | `{Domain}Config` | `MatchConfig`, `AuthConfig` |

## Example: Match Domain

```
match/
├── domain/
│   ├── model/
│   │   └── Match.java
│   ├── vo/
│   │   └── MatchStatus.java
│   ├── repository/
│   │   └── MatchRepository.java
│   ├── exception/
│   │   ├── InvalidTimeRangeException.java
│   │   └── InvalidMaxParticipantsException.java
│   └── policy/
│       └── MatchPolicyValidator.java
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateMatchUseCase.java
│   │   │   └── MatchQueryUseCase.java
│   │   └── out/
│   │       ├── HostInfoPort.java
│   │       └── LocationInfoPort.java
│   ├── dto/
│   │   ├── HostInfo.java
│   │   └── LocationInfo.java
│   ├── service/
│   │   ├── MatchCreator.java
│   │   └── MatchFinder.java
│   └── exception/
│       ├── MatchNotFoundException.java
│       └── NotMatchHostException.java
│
├── adapter/
│   ├── in/web/
│   │   ├── MatchController.java
│   │   └── dto/
│   │       ├── CreateMatchRequest.java
│   │       └── MatchResponse.java
│   └── out/
│       ├── persistence/
│       │   ├── MatchJpaEntity.java
│       │   ├── MatchJpaAdapter.java
│       │   ├── SpringDataMatchRepository.java
│       │   └── MatchMapper.java
│       ├── LocationInfoAdapter.java
│       └── UserHostInfoAdapter.java
│
└── infrastructure/
    └── config/
        └── MatchConfig.java
```

## Example: Auth Domain

```
auth/
├── domain/
│   ├── model/
│   │   └── AuthAccount.java
│   ├── vo/
│   │   ├── AuthProvider.java
│   │   ├── OAuthUserInfo.java
│   │   └── TokenPair.java
│   ├── repository/
│   │   └── AuthAccountRepository.java
│   └── exception/
│       └── InvalidNicknameException.java
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── OAuthLoginUseCase.java
│   │   │   └── SignupUseCase.java
│   │   └── out/
│   │       ├── OAuthPort.java
│   │       ├── JwtTokenPort.java
│   │       └── UserInfoPort.java
│   ├── dto/
│   │   ├── SignupCommand.java
│   │   └── OAuthCallbackResult.java
│   ├── service/
│   │   ├── OAuthLoginService.java
│   │   └── SignupService.java
│   └── exception/
│       ├── DuplicateNicknameException.java
│       └── InvalidTempTokenException.java
│
├── adapter/
│   ├── in/web/
│   │   ├── AuthController.java
│   │   └── dto/
│   │       ├── SignupRequest.java
│   │       └── TokenResponse.java
│   └── out/
│       ├── persistence/
│       │   ├── AuthAccountJpaEntity.java
│       │   ├── AuthAccountJpaAdapter.java
│       │   └── SpringDataAuthAccountRepository.java
│       └── oauth/kakao/
│           └── KakaoOAuthAdapter.java
│
└── infrastructure/
    └── config/
        └── AuthConfig.java
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
