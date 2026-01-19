---
name: architecture-patterns
description: Hoops 프로젝트 Hexagonal Architecture 가이드. Java/Spring 기반 도메인 설계 및 패키지 구조 정의.
---

# Hexagonal Architecture for Hoops

Hoops 프로젝트의 Hexagonal Architecture (Ports and Adapters) 구현 가이드.

## Package Structure

```
{domain}/
├── domain/                      # 순수 도메인 (Pure POJO, No Framework)
│   ├── model/                   # Identity를 가진 도메인 모델
│   ├── vo/                      # Value Objects (불변)
│   └── exception/               # 도메인 규칙 위반 예외
│
├── application/                 # 애플리케이션 계층
│   ├── port/
│   │   ├── in/                  # Inbound Port (UseCase 인터페이스)
│   │   └── out/                 # Outbound Port (*Port 접미사)
│   ├── service/                 # UseCase 구현체
│   ├── dto/                     # Command, Response (UseCase I/O)
│   └── exception/               # UseCase 실패 예외
│
├── adapter/                     # 어댑터 계층
│   ├── in/
│   │   └── web/                 # Controller
│   │       └── dto/             # Request/Response DTO
│   └── out/
│       ├── persistence/         # JPA Entity, Repository 구현
│       └── {external}/          # 외부 API Adapter (oauth, payment 등)
│           └── exception/       # 외부 API 예외
│
└── infrastructure/
    └── config/                  # Spring Configuration
```

## Layer Rules

### 1. Domain Layer (domain/)

**목적**: 비즈니스 규칙과 도메인 개념 표현

**규칙**:
- Spring, JPA, Lombok(@Data) 등 외부 프레임워크 의존 금지
- 순수 Java POJO로 구성
- Lombok @Getter, @Builder, @AllArgsConstructor는 허용

```java
// domain/model/AuthAccount.java - Identity를 가진 도메인 모델
package com.hoops.auth.domain.model;

import com.hoops.auth.domain.vo.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthAccount {
    private final Long id;
    private final Long userId;
    private final AuthProvider provider;
    private final String providerId;
    private final String refreshToken;

    public static AuthAccount createForKakao(Long userId, String kakaoId, String refreshToken) {
        return AuthAccount.builder()
                .userId(userId)
                .provider(AuthProvider.KAKAO)
                .providerId(kakaoId)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthAccount withRefreshToken(String newRefreshToken) {
        return AuthAccount.builder()
                .id(this.id)
                .userId(this.userId)
                .provider(this.provider)
                .providerId(this.providerId)
                .refreshToken(newRefreshToken)
                .build();
    }
}
```

```java
// domain/vo/TokenPair.java - Value Object (불변)
package com.hoops.auth.domain.vo;

public record TokenPair(
    String accessToken,
    String refreshToken
) {}
```

```java
// domain/exception/InvalidNicknameException.java - 도메인 규칙 위반
package com.hoops.auth.domain.exception;

import com.hoops.common.exception.DomainException;

public class InvalidNicknameException extends DomainException {
    private static final String ERROR_CODE = "INVALID_NICKNAME";

    public InvalidNicknameException(String nickname) {
        super(ERROR_CODE, "Invalid nickname: " + nickname);
    }
}
```

### 2. Application Layer (application/)

**목적**: UseCase 정의 및 구현, 도메인 객체 조합

#### Port 명명 규칙

| 구분 | 위치 | 접미사 | 예시 |
|-----|------|--------|------|
| Inbound | port/in/ | `*UseCase` | `SignupUseCase`, `KakaoLoginUseCase` |
| Outbound | port/out/ | `*Port` | `AuthAccountPort`, `JwtTokenPort` |

```java
// application/port/in/SignupUseCase.java - Inbound Port
package com.hoops.auth.application.port.in;

import com.hoops.auth.application.dto.AuthResult;
import com.hoops.auth.application.dto.SignupCommand;

public interface SignupUseCase {
    AuthResult signup(SignupCommand command);
}
```

```java
// application/port/out/AuthAccountPort.java - Outbound Port
package com.hoops.auth.application.port.out;

import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.vo.AuthProvider;
import java.util.Optional;

public interface AuthAccountPort {
    Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId);
    AuthAccount save(AuthAccount authAccount);
}
```

```java
// application/dto/SignupCommand.java - UseCase Input
package com.hoops.auth.application.dto;

public record SignupCommand(
    String tempToken,
    String nickname
) {}
```

```java
// application/service/SignupService.java - UseCase 구현체
package com.hoops.auth.application.service;

import com.hoops.auth.application.port.in.SignupUseCase;
import com.hoops.auth.application.port.out.AuthAccountPort;
import com.hoops.auth.application.port.out.JwtTokenPort;
import com.hoops.auth.application.port.out.UserInfoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SignupService implements SignupUseCase {

    private final JwtTokenPort jwtTokenPort;
    private final UserInfoPort userInfoPort;
    private final AuthAccountPort authAccountPort;

    @Override
    public AuthResult signup(SignupCommand command) {
        // UseCase 로직
    }
}
```

```java
// application/exception/DuplicateNicknameException.java - UseCase 실패 예외
package com.hoops.auth.application.exception;

import com.hoops.common.exception.ApplicationException;

public class DuplicateNicknameException extends ApplicationException {
    private static final String ERROR_CODE = "DUPLICATE_NICKNAME";

    public DuplicateNicknameException(String nickname) {
        super(ERROR_CODE, "Nickname already exists: " + nickname);
    }
}
```

### 3. Adapter Layer (adapter/)

**목적**: 외부 세계와의 통신 담당

#### Inbound Adapter (adapter/in/web/)

```java
// adapter/in/web/AuthController.java
package com.hoops.auth.adapter.in.web;

import com.hoops.auth.adapter.in.web.dto.SignupRequest;
import com.hoops.auth.adapter.in.web.dto.AuthResponse;
import com.hoops.auth.application.port.in.SignupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignupUseCase signupUseCase;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResult result = signupUseCase.signup(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
    }
}
```

```java
// adapter/in/web/dto/SignupRequest.java - HTTP Request DTO
package com.hoops.auth.adapter.in.web.dto;

import com.hoops.auth.application.dto.SignupCommand;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
    @NotBlank String tempToken,
    @NotBlank String nickname
) {
    public SignupCommand toCommand() {
        return new SignupCommand(tempToken, nickname);
    }
}
```

#### Outbound Adapter (adapter/out/)

```java
// adapter/out/persistence/AuthAccountJpaEntity.java - JPA Entity
package com.hoops.auth.adapter.out.persistence;

import com.hoops.auth.domain.vo.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AuthAccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private String providerId;

    private String refreshToken;
}
```

```java
// adapter/out/persistence/AuthAccountJpaAdapter.java - Port 구현체
package com.hoops.auth.adapter.out.persistence;

import com.hoops.auth.application.port.out.AuthAccountPort;
import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.vo.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthAccountJpaAdapter implements AuthAccountPort {

    private final SpringDataAuthAccountRepository repository;

    @Override
    public Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return repository.findByProviderAndProviderId(provider, providerId)
                .map(this::toDomain);
    }

    @Override
    public AuthAccount save(AuthAccount authAccount) {
        AuthAccountJpaEntity entity = toEntity(authAccount);
        AuthAccountJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private AuthAccount toDomain(AuthAccountJpaEntity entity) {
        return AuthAccount.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .provider(entity.getProvider())
                .providerId(entity.getProviderId())
                .refreshToken(entity.getRefreshToken())
                .build();
    }

    private AuthAccountJpaEntity toEntity(AuthAccount domain) {
        return AuthAccountJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .provider(domain.getProvider())
                .providerId(domain.getProviderId())
                .refreshToken(domain.getRefreshToken())
                .build();
    }
}
```

```java
// adapter/out/oauth/exception/KakaoApiException.java - 외부 API 예외
package com.hoops.auth.adapter.out.oauth.exception;

import com.hoops.common.exception.ApplicationException;

public class KakaoApiException extends ApplicationException {
    private static final String ERROR_CODE = "KAKAO_API_ERROR";

    public KakaoApiException(String message) {
        super(ERROR_CODE, message);
    }
}
```

### 4. Infrastructure Layer (infrastructure/)

**목적**: 프레임워크 설정

```java
// infrastructure/config/AuthConfig.java
package com.hoops.auth.infrastructure.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {
    // Spring 설정
}
```

## Exception Hierarchy

```
BusinessException (추상)
├── DomainException (도메인 규칙 위반)
│   └── InvalidNicknameException
└── ApplicationException (UseCase/외부 API 실패)
    ├── DuplicateNicknameException
    ├── InvalidTempTokenException
    └── KakaoApiException
```

| 위치 | 사용 시점 | 예시 |
|------|----------|------|
| `domain/exception/` | 도메인 규칙 위반 | 닉네임 형식 불일치 |
| `application/exception/` | UseCase 실패 | 중복 닉네임, 토큰 만료 |
| `adapter/out/{external}/exception/` | 외부 API 실패 | 카카오 API 오류 |

## DTO 구분

| 위치 | 역할 | 예시 |
|------|------|------|
| `application/dto/` | UseCase Input/Output | SignupCommand, AuthResult |
| `adapter/in/web/dto/` | HTTP Request/Response | SignupRequest, AuthResponse |
| `domain/vo/` | 도메인 개념 표현 | TokenPair, KakaoUserInfo |

## Dependency Direction

```
adapter/in/web → application/port/in → application/service
                                              ↓
                                       application/port/out
                                              ↓
                                       adapter/out/*
                                              ↓
                                         domain/*
```

- 의존성은 항상 안쪽(domain)을 향함
- domain은 어떤 계층도 의존하지 않음
- application은 domain만 의존
- adapter는 application과 domain 의존

## Checklist

새 도메인 생성 시:

- [ ] `domain/model/` - 도메인 모델 (순수 POJO)
- [ ] `domain/vo/` - Value Objects
- [ ] `domain/exception/` - 도메인 예외
- [ ] `application/port/in/` - UseCase 인터페이스
- [ ] `application/port/out/` - Outbound Port (*Port 접미사)
- [ ] `application/service/` - UseCase 구현
- [ ] `application/dto/` - Command, Response
- [ ] `application/exception/` - UseCase 예외
- [ ] `adapter/in/web/` - Controller
- [ ] `adapter/in/web/dto/` - Request/Response DTO
- [ ] `adapter/out/persistence/` - JPA Entity, Adapter
- [ ] `infrastructure/config/` - 필요시 설정 클래스
