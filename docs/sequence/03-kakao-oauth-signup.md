# 카카오 소셜 로그인 회원가입 플로우 (Kakao OAuth Signup Flow)

## 개요
카카오 OAuth 2.0을 활용한 소셜 로그인 및 회원가입 플로우를 정의합니다.
신규 회원은 닉네임 입력 후 가입이 완료되며, 기존 회원은 즉시 로그인됩니다.

## 시퀀스 다이어그램

```mermaid
sequenceDiagram
    actor User as 사용자
    participant Client as 클라이언트
    participant Controller as AuthController
    participant Service as AuthService
    participant Kakao as 카카오 API
    participant Repository as UserRepository
    participant DB as Database

    %% 1. 카카오 로그인 시작
    User->>Client: 카카오 로그인 버튼 클릭
    Client->>Controller: GET /api/auth/kakao
    Controller-->>Client: 카카오 인증 URL 반환
    Client->>Kakao: 카카오 로그인 페이지 리다이렉트

    %% 2. 카카오 인증 및 콜백
    User->>Kakao: 카카오 계정 로그인
    Kakao-->>Client: 인가코드(code) 콜백
    Client->>Controller: GET /api/auth/kakao/callback?code={code}
    Controller->>Service: processKakaoCallback(code)

    %% 3. 토큰 교환 및 사용자 정보 조회
    Service->>Kakao: POST /oauth/token
    Kakao-->>Service: Access Token 반환
    Service->>Kakao: GET /v2/user/me
    Kakao-->>Service: 카카오 사용자 정보 반환

    %% 4. 회원 조회
    Service->>Repository: findByKakaoId(kakaoId)
    Repository->>DB: SELECT user
    DB-->>Repository: 조회 결과

    alt 신규 회원
        Repository-->>Service: Optional.empty()
        Service-->>Controller: 202 + 임시토큰
        Controller-->>Client: 닉네임 입력 필요
        User->>Client: 닉네임 입력
        Client->>Controller: POST /api/auth/signup
        Controller->>Service: signup(tempToken, nickname)
        Service->>Repository: save(user)
        Repository->>DB: INSERT user
        DB-->>Repository: 저장 완료
        Service-->>Controller: JWT 토큰
        Controller-->>Client: 201 Created
    else 기존 회원
        Repository-->>Service: User 반환
        Service-->>Controller: JWT 토큰
        Controller-->>Client: 200 OK
    end
```

## 주요 단계

### 1. 카카오 인증 URL 요청
- **Endpoint**: `GET /api/auth/kakao`
- **Response**: 카카오 인증 페이지 URL
- **인증**: 불필요

### 2. 카카오 콜백 처리
- **Endpoint**: `GET /api/auth/kakao/callback?code={code}`
- **처리 내용**:
  1. 인가코드로 Access Token 교환
  2. Access Token으로 사용자 정보 조회
  3. kakaoId로 기존 회원 여부 확인
- **Response**:
  - 신규 회원: `202 Accepted` + 임시 토큰
  - 기존 회원: `200 OK` + JWT 토큰

### 3. 회원가입 완료 (신규 회원만)
- **Endpoint**: `POST /api/auth/signup`
- **Request**: 임시 토큰 + 닉네임
- **Validation**: 닉네임 2~20자, 중복 불가
- **Response**: `201 Created` + JWT 토큰

### 4. 토큰 갱신
- **Endpoint**: `POST /api/auth/refresh`
- **Request**: Refresh Token
- **Response**: 새로운 Access Token

## 아키텍처 레이어

| 레이어 | 컴포넌트 | 패키지 |
|--------|----------|--------|
| Adapter (In) | AuthController | `com.hoops.user.adapter.in.web` |
| Application | AuthService | `com.hoops.user.application.service` |
| Application | KakaoOAuthClient | `com.hoops.user.application.service` |
| Domain | User | `com.hoops.user.domain` |
| Adapter (Out) | UserRepositoryImpl | `com.hoops.user.adapter.out.adapter` |
| Infrastructure | JwtTokenProvider | `com.hoops.common.security` |

## 주요 예외

| 예외 | HTTP Status | 발생 조건 |
|------|-------------|-----------|
| `InvalidAuthCodeException` | 400 | 유효하지 않은 인가코드 |
| `KakaoApiException` | 502 | 카카오 API 호출 실패 |
| `InvalidTempTokenException` | 400 | 만료되거나 유효하지 않은 임시 토큰 |
| `DuplicateNicknameException` | 409 | 이미 사용 중인 닉네임 |
| `InvalidNicknameException` | 400 | 닉네임 형식 오류 (2~20자) |
| `InvalidRefreshTokenException` | 401 | 유효하지 않은 리프레시 토큰 |

## 추가 고려사항

### JWT 토큰 설계
- **Access Token**: 유효기간 30분
- **Refresh Token**: 유효기간 14일
- **Temp Token**: 유효기간 10분 (회원가입 완료 전까지만 유효)

### 보안
- Refresh Token: HttpOnly 쿠키 권장
- 카카오 API Key: 백엔드 환경변수로 관리
- Client Secret: 필요시 추가 보안 적용

### 환경 설정
```yaml
kakao:
  client-id: ${KAKAO_CLIENT_ID}
  redirect-uri: ${KAKAO_REDIRECT_URI}

jwt:
  secret: ${JWT_SECRET}
  access-token-expiry: 1800000      # 30분
  refresh-token-expiry: 1209600000  # 14일
```

## 관련 문서
- [인증 API 명세서](/docs/api/03-auth-api.md)
- [아키텍처 가이드](/docs/architecture/architecture.md)
- [컨벤션 가이드](/docs/convention/convention.md)
