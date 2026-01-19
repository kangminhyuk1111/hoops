---
name: architecture-patterns
description: Hoops 프로젝트 Hexagonal Architecture 가이드. Java/Spring 기반 도메인 설계 및 패키지 구조 정의.
---

# Hexagonal Architecture for Hoops

Hoops 프로젝트의 Hexagonal Architecture (Ports and Adapters) + DDD 구현 가이드.

## Package Structure

```
{domain}/
├── domain/                      # 순수 도메인 (Pure POJO, No Framework)
│   ├── model/                   # Identity를 가진 도메인 모델 (Entity)
│   ├── vo/                      # Value Objects (불변, 벤더 중립적)
│   ├── repository/              # Repository 인터페이스 (DDD)
│   └── exception/               # 도메인 규칙 위반 예외
│
├── application/                 # 애플리케이션 계층
│   ├── port/
│   │   ├── in/                  # Inbound Port (UseCase 인터페이스)
│   │   └── out/                 # Outbound Port (외부 서비스, ACL)
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
│       └── {external}/          # 외부 API Adapter
│           └── {provider}/      # 벤더별 구현 (kakao/, google/)
│               └── exception/   # 벤더별 예외
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
- **Value Object는 벤더 중립적으로 명명** (KakaoUserInfo ❌ → OAuthUserInfo ✅)

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

    public static AuthAccount createForKakao(Long userId, String providerId, String refreshToken) {
        return AuthAccount.builder()
                .userId(userId)
                .provider(AuthProvider.KAKAO)
                .providerId(providerId)
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
// domain/vo/OAuthUserInfo.java - Value Object (벤더 중립적)
package com.hoops.auth.domain.vo;

public record OAuthUserInfo(
    String providerId,
    String email,
    String nickname,
    String profileImage
) {
    public static OAuthUserInfo of(String providerId, String email, String nickname, String profileImage) {
        return new OAuthUserInfo(providerId, email, nickname, profileImage);
    }
}
```

```java
// domain/repository/AuthAccountRepository.java - DDD Repository (도메인 계층)
package com.hoops.auth.domain.repository;

import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.vo.AuthProvider;
import java.util.Optional;

public interface AuthAccountRepository {
    Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId);
    AuthAccount save(AuthAccount authAccount);
}
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

| 구분 | 위치 | 접미사 | 용도 | 예시 |
|-----|------|--------|------|------|
| Inbound | port/in/ | `*UseCase` | UseCase 인터페이스 | `SignupUseCase`, `OAuthLoginUseCase` |
| Outbound | port/out/ | `*Port` | 외부 서비스, ACL | `OAuthPort`, `JwtTokenPort`, `UserInfoPort` |
| Repository | domain/repository/ | `*Repository` | 영속성 (DDD) | `AuthAccountRepository` |

> **Repository vs Port**: Repository는 DDD 개념으로 `domain/repository/`에 위치. Port는 외부 서비스 통신용.

```java
// application/port/in/OAuthLoginUseCase.java - Inbound Port (벤더 중립적)
package com.hoops.auth.application.port.in;

import com.hoops.auth.application.dto.OAuthCallbackResult;
import com.hoops.auth.domain.vo.AuthProvider;

public interface OAuthLoginUseCase {
    String getAuthorizationUrl(AuthProvider provider);
    OAuthCallbackResult processCallback(AuthProvider provider, String code);
}
```

```java
// application/port/out/OAuthPort.java - Outbound Port (벤더 중립적)
package com.hoops.auth.application.port.out;

import com.hoops.auth.domain.vo.OAuthTokenInfo;
import com.hoops.auth.domain.vo.OAuthUserInfo;

public interface OAuthPort {
    String getAuthorizationUrl();
    OAuthTokenInfo getToken(String code);
    OAuthUserInfo getUserInfo(String accessToken);
}
```

```java
// application/service/OAuthLoginService.java - UseCase 구현체
package com.hoops.auth.application.service;

import com.hoops.auth.application.port.in.OAuthLoginUseCase;
import com.hoops.auth.application.port.out.OAuthPort;
import com.hoops.auth.domain.repository.AuthAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OAuthLoginService implements OAuthLoginUseCase {

    private final OAuthPort oauthPort;              // 외부 서비스 Port
    private final AuthAccountRepository authAccountRepository;  // 도메인 Repository

    @Override
    public OAuthCallbackResult processCallback(AuthProvider provider, String code) {
        OAuthUserInfo userInfo = fetchUserInfo(code);
        // UseCase 로직
    }
}
```

### 3. Adapter Layer (adapter/)

**목적**: 외부 세계와의 통신 담당

#### Inbound Adapter (adapter/in/web/)

```java
// adapter/in/web/AuthController.java
package com.hoops.auth.adapter.in.web;

import com.hoops.auth.application.port.in.OAuthLoginUseCase;
import com.hoops.auth.domain.vo.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuthLoginUseCase oauthLoginUseCase;

    @GetMapping("/kakao")
    public ResponseEntity<KakaoAuthUrlResponse> getKakaoAuthUrl() {
        String authUrl = oauthLoginUseCase.getAuthorizationUrl(AuthProvider.KAKAO);
        return ResponseEntity.ok(new KakaoAuthUrlResponse(authUrl));
    }
}
```

#### Outbound Adapter - Persistence (adapter/out/persistence/)

```java
// adapter/out/persistence/AuthAccountJpaAdapter.java - Repository 구현체
package com.hoops.auth.adapter.out.persistence;

import com.hoops.auth.domain.repository.AuthAccountRepository;  // 도메인 Repository 구현
import com.hoops.auth.domain.model.AuthAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthAccountJpaAdapter implements AuthAccountRepository {

    private final SpringDataAuthAccountRepository repository;

    @Override
    public AuthAccount save(AuthAccount authAccount) {
        AuthAccountJpaEntity entity = toEntity(authAccount);
        AuthAccountJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private AuthAccount toDomain(AuthAccountJpaEntity entity) { /* ... */ }
    private AuthAccountJpaEntity toEntity(AuthAccount domain) { /* ... */ }
}
```

#### Outbound Adapter - External API (adapter/out/{external}/{provider}/)

**벤더별 구현체를 분리**하여 확장성 확보:

```
adapter/out/oauth/
├── kakao/
│   ├── KakaoOAuthAdapter.java    # implements OAuthPort
│   └── exception/
│       ├── InvalidAuthCodeException.java
│       └── KakaoApiException.java
└── google/                        # 향후 확장
    └── GoogleOAuthAdapter.java
```

```java
// adapter/out/oauth/kakao/KakaoOAuthAdapter.java
package com.hoops.auth.adapter.out.oauth.kakao;

import com.hoops.auth.application.port.out.OAuthPort;
import com.hoops.auth.domain.vo.OAuthTokenInfo;
import com.hoops.auth.domain.vo.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoOAuthAdapter implements OAuthPort {

    private final KakaoOAuthProperties properties;
    private final RestTemplate restTemplate;

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        Map<String, Object> response = requestUserInfo(accessToken);
        return toOAuthUserInfo(response);  // Kakao 응답 → 벤더 중립 VO 변환
    }

    private OAuthUserInfo toOAuthUserInfo(Map<String, Object> body) {
        String kakaoId = String.valueOf(body.get("id"));
        Map<String, Object> account = getNestedMap(body, "kakao_account");
        Map<String, Object> profile = getNestedMap(account, "profile");

        return OAuthUserInfo.of(
                kakaoId,
                (String) account.get("email"),
                (String) profile.get("nickname"),
                (String) profile.get("profile_image_url"));
    }
}
```

### 4. Infrastructure Layer (infrastructure/)

**목적**: 프레임워크 설정

```java
// infrastructure/config/AuthConfig.java
package com.hoops.auth.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KakaoOAuthProperties.class)
public class AuthConfig {
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
    └── (Adapter 예외도 가능)
```

| 위치 | 사용 시점 | 예시 |
|------|----------|------|
| `domain/exception/` | 도메인 규칙 위반 | 닉네임 형식 불일치 |
| `application/exception/` | UseCase 실패 | 중복 닉네임, 토큰 만료 |
| `adapter/out/{external}/{provider}/exception/` | 벤더별 API 실패 | 카카오 API 오류 |

## DTO 구분

| 위치 | 역할 | 예시 |
|------|------|------|
| `application/dto/` | UseCase Input/Output | SignupCommand, OAuthCallbackResult |
| `adapter/in/web/dto/` | HTTP Request/Response | SignupRequest, OAuthCallbackResponse |
| `domain/vo/` | 도메인 개념 (벤더 중립) | TokenPair, OAuthUserInfo |

## Dependency Direction

```
adapter/in/web → application/port/in → application/service
                                              ↓
                              domain/repository ← adapter/out/persistence
                              application/port/out ← adapter/out/{external}
                                              ↓
                                         domain/*
```

- 의존성은 항상 안쪽(domain)을 향함
- domain은 어떤 계층도 의존하지 않음
- application은 domain만 의존
- adapter는 application과 domain 의존

## 핵심 원칙

1. **벤더 중립적 도메인**: `KakaoUserInfo` ❌ → `OAuthUserInfo` ✅
2. **Repository는 도메인에**: `domain/repository/` (DDD 스타일)
3. **Port는 외부 서비스용**: `application/port/out/` (ACL, 외부 API)
4. **벤더별 Adapter 분리**: `adapter/out/oauth/kakao/`, `adapter/out/oauth/google/`
5. **Config는 Infrastructure에**: `infrastructure/config/`

## Checklist

새 도메인 생성 시:

- [ ] `domain/model/` - 도메인 모델 (순수 POJO)
- [ ] `domain/vo/` - Value Objects (벤더 중립적)
- [ ] `domain/repository/` - Repository 인터페이스 (DDD)
- [ ] `domain/exception/` - 도메인 예외
- [ ] `application/port/in/` - UseCase 인터페이스 (벤더 중립적)
- [ ] `application/port/out/` - 외부 서비스 Port (벤더 중립적)
- [ ] `application/service/` - UseCase 구현
- [ ] `application/dto/` - Command, Response
- [ ] `application/exception/` - UseCase 예외
- [ ] `adapter/in/web/` - Controller
- [ ] `adapter/in/web/dto/` - Request/Response DTO
- [ ] `adapter/out/persistence/` - JPA Entity, Repository 구현체
- [ ] `adapter/out/{external}/{provider}/` - 벤더별 Adapter
- [ ] `infrastructure/config/` - 필요시 설정 클래스
