# Auth 도메인 리팩토링

> 날짜: 2026-01-17

---

## 변경 요약

| 항목 | Before | After |
|------|--------|-------|
| AuthService.processCallback() | 38줄 | 9줄 |
| AuthService.signup() | 44줄 | 7줄 |
| TestLogin | 존재 | 제거 |
| IllegalStateException 사용 | 2곳 | 0곳 |
| UserRepository 직접 접근 | O | X (Port 사용) |
| Validator 위치 | service 패키지 | validator 패키지 |

---

## 1. 패키지 구조 변경

### Before
```
auth/
├── application/
│   └── service/
│       ├── AuthService.java
│       └── SignupValidator.java  ← service 패키지 내 위치
```

### After
```
auth/
├── application/
│   ├── service/
│   │   └── AuthService.java
│   ├── validator/
│   │   └── SignupValidator.java  ← validator 패키지로 분리
│   └── port/
│       └── out/
│           └── UserInfoPort.java  ← 신규 Port
├── infrastructure/
│   └── adapter/
│       └── AuthUserInfoAdapter.java  ← Port 구현체
```

---

## 2. Cross-Context 통신 개선

### 문제점
Auth 컨텍스트가 User 컨텍스트의 `UserRepository`에 직접 접근

```java
// Before - 직접 의존
private final UserRepository userRepository;
```

### 해결
Port를 통한 간접 접근

```java
// After - Port를 통한 접근
private final UserInfoPort userInfoPort;
```

### UserInfoPort 정의

```java
public interface UserInfoPort {
    Optional<User> findById(Long userId);
    User save(User user);
    boolean existsByNickname(String nickname);
}
```

### AuthUserInfoAdapter 구현

```java
@Component
@RequiredArgsConstructor
public class AuthUserInfoAdapter implements UserInfoPort {
    private final UserRepository userRepository;

    // Port 메서드 구현...
}
```

---

## 3. TestLogin 제거

### 제거된 파일
- `auth/application/port/in/TestLoginUseCase.java`

### 수정된 파일
- `AuthService.java` - TestLoginUseCase 구현 제거
- `AuthController.java` - /test-login 엔드포인트 제거
- `User.java` - createTestUser() 메서드 제거

### 이유
- 비즈니스 로직에 테스트 코드가 존재하는 것은 부적절
- 프론트엔드 의존성도 함께 제거 필요

---

## 4. 최종 AuthService

```java
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService implements KakaoLoginUseCase, SignupUseCase, RefreshTokenUseCase {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthAccountRepository authAccountRepository;
    private final UserInfoPort userInfoPort;           // Port 사용
    private final SignupValidator signupValidator;     // validator 패키지

    @Override
    public String getKakaoAuthUrl() {
        return kakaoOAuthClient.getAuthorizationUrl();
    }

    @Override
    public KakaoCallbackResult processCallback(String code) {
        KakaoUserInfo kakaoUserInfo = fetchKakaoUserInfo(code);

        Optional<AuthAccount> existingAccount = authAccountRepository
                .findByProviderAndProviderId(AuthProvider.KAKAO, kakaoUserInfo.kakaoId());

        return existingAccount
                .map(this::processExistingUserLogin)
                .orElseGet(() -> processNewUserCallback(kakaoUserInfo));
    }

    @Override
    public AuthResult signup(SignupCommand command) {
        Map<String, Object> claims = signupValidator.validateAndExtractClaims(command);

        User savedUser = createAndSaveNewUser(claims, command.nickname());
        TokenResult tokens = jwtTokenProvider.createTokens(savedUser.getId());
        createAndSaveAuthAccount(claims, savedUser.getId(), tokens.refreshToken());

        return new AuthResult(tokens.accessToken(), tokens.refreshToken(), UserInfo.from(savedUser));
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResult refresh(String refreshToken) {
        return jwtTokenProvider.refreshTokens(refreshToken);
    }

    // private helper methods...
}
```

---

## 5. 파일 변경 목록

### 신규 파일
| 파일 | 설명 |
|------|------|
| `auth/application/port/out/UserInfoPort.java` | User 정보 접근 Port |
| `auth/application/validator/SignupValidator.java` | 회원가입 검증 (위치 변경) |
| `auth/application/exception/UserNotFoundForAuthException.java` | 커스텀 예외 |
| `auth/infrastructure/adapter/AuthUserInfoAdapter.java` | UserInfoPort 구현체 |

### 수정 파일
| 파일 | 변경 내용 |
|------|----------|
| `AuthService.java` | TestLogin 제거, Port 사용 |
| `AuthController.java` | /test-login 엔드포인트 제거 |
| `AuthAccount.java` | 팩토리 메서드 추가 |
| `User.java` | createTestUser 제거, createNew 추가 |
| `UserInfo.java` | from() 메서드 추가 |

### 삭제 파일
| 파일 | 이유 |
|------|------|
| `TestLoginUseCase.java` | 비즈니스 로직에서 테스트 코드 제거 |
| `auth/application/service/SignupValidator.java` | validator 패키지로 이동 |

---

## 6. 의존성 다이어그램

```
AuthController
    │
    ▼
AuthService
    ├── KakaoOAuthClient (Port)
    ├── JwtTokenProvider (Port)
    ├── AuthAccountRepository
    ├── UserInfoPort ────────► AuthUserInfoAdapter ────► UserRepository
    └── SignupValidator
            └── UserInfoPort
            └── JwtTokenProvider
```

---

## 7. 테스트 결과

```
BUILD SUCCESSFUL
모든 테스트 통과
```

---

## 8. Private 메서드 정리 (2차 리팩터링)

> 날짜: 2026-01-17

### 변경 요약

| 항목 | Before | After |
|------|--------|-------|
| AuthService 줄 수 | 117줄 | 46줄 |
| Private 메서드 | 6개 | 0개 |
| Handler/Processor | 0개 | 2개 |

### 신규 파일

**KakaoCallbackHandler.java**
- 위치: `auth/application/service/`
- 역할: 카카오 콜백 처리 전담
- 메서드: `handle()`, `fetchKakaoUserInfo()`, `handleExistingUser()`, `handleNewUser()`, `findUserByAuthAccount()`

**SignupProcessor.java**
- 위치: `auth/application/service/`
- 역할: 회원가입 처리 전담
- 메서드: `process()`, `createUser()`, `createAuthAccount()`

### 최종 AuthService

AuthService는 이제 오케스트레이션만 담당:
- `getKakaoAuthUrl()` → kakaoOAuthClient 위임
- `processCallback()` → KakaoCallbackHandler.handle() 위임
- `signup()` → SignupProcessor.process() 위임
- `refresh()` → jwtTokenProvider 위임

### 의존성 구조

```
AuthService
├── KakaoOAuthClient
├── JwtTokenProvider
├── KakaoCallbackHandler
│   ├── KakaoOAuthClient
│   ├── JwtTokenProvider
│   ├── AuthAccountRepository
│   └── UserInfoPort
└── SignupProcessor
    ├── JwtTokenProvider
    ├── UserInfoPort
    ├── AuthAccountRepository
    └── SignupValidator
```

---

## 9. 남은 고려사항

### 프론트엔드
TestLogin 제거로 인해 프론트엔드의 테스트 로그인 기능도 함께 제거해야 합니다.
