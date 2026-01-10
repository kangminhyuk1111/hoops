# API 명세서: 인증 (Authentication)

## 목차
1. [카카오 인증 URL 요청](#1-카카오-인증-url-요청)
2. [카카오 콜백 처리](#2-카카오-콜백-처리)
3. [회원가입 완료](#3-회원가입-완료)
4. [토큰 갱신](#4-토큰-갱신)

---

## 1. 카카오 인증 URL 요청

### 기본 정보

| 항목 | 내용 |
|------|------|
| **Endpoint** | `GET /api/auth/kakao` |
| **설명** | 카카오 OAuth 인증 페이지 URL을 반환합니다 |
| **인증** | 불필요 |

### Response

#### 200 OK

```json
{
  "authUrl": "https://kauth.kakao.com/oauth/authorize?client_id=xxx&redirect_uri=xxx&response_type=code"
}
```

---

## 2. 카카오 콜백 처리

### 기본 정보

| 항목 | 내용 |
|------|------|
| **Endpoint** | `GET /api/auth/kakao/callback` |
| **설명** | 카카오 인증 콜백을 처리하고 JWT 토큰을 발급합니다 |
| **인증** | 불필요 |

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| code | String | O | 카카오 인가코드 |

### Response

#### 200 OK (기존 회원)

```json
{
  "isNewUser": false,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "nickname": "농구왕",
    "email": "user@example.com",
    "profileImage": "https://k.kakaocdn.net/..."
  }
}
```

#### 202 Accepted (신규 회원)

```json
{
  "isNewUser": true,
  "tempToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "kakaoInfo": {
    "email": "user@example.com",
    "profileImage": "https://k.kakaocdn.net/..."
  }
}
```

**참고**: 신규 회원은 `tempToken`을 사용하여 회원가입 완료 API를 호출해야 합니다.

### Error Response

#### 400 Bad Request

**유효하지 않은 인가코드**
```json
{
  "timestamp": "2026-01-10T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_AUTH_CODE",
  "message": "유효하지 않은 인가코드입니다",
  "path": "/api/auth/kakao/callback"
}
```

#### 502 Bad Gateway

**카카오 API 호출 실패**
```json
{
  "timestamp": "2026-01-10T10:30:00",
  "status": 502,
  "error": "Bad Gateway",
  "code": "KAKAO_API_ERROR",
  "message": "카카오 API 호출에 실패했습니다",
  "path": "/api/auth/kakao/callback"
}
```

---

## 3. 회원가입 완료

### 기본 정보

| 항목 | 내용 |
|------|------|
| **Endpoint** | `POST /api/auth/signup` |
| **설명** | 닉네임을 입력받아 회원가입을 완료합니다 |
| **인증** | 임시 토큰 필요 |

### Request

```json
{
  "tempToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "nickname": "농구왕"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| tempToken | String | O | 콜백에서 받은 임시 토큰 |
| nickname | String | O | 사용자 닉네임 (2~20자) |

### Response

#### 201 Created

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "nickname": "농구왕",
    "email": "user@example.com",
    "profileImage": "https://k.kakaocdn.net/..."
  }
}
```

### Error Response

#### 400 Bad Request

**유효하지 않은 임시 토큰**
```json
{
  "timestamp": "2026-01-10T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_TEMP_TOKEN",
  "message": "만료되거나 유효하지 않은 임시 토큰입니다",
  "path": "/api/auth/signup"
}
```

**닉네임 형식 오류**
```json
{
  "timestamp": "2026-01-10T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_NICKNAME",
  "message": "닉네임은 2~20자 사이여야 합니다",
  "path": "/api/auth/signup"
}
```

#### 409 Conflict

**닉네임 중복**
```json
{
  "timestamp": "2026-01-10T10:30:00",
  "status": 409,
  "error": "Conflict",
  "code": "DUPLICATE_NICKNAME",
  "message": "이미 사용 중인 닉네임입니다",
  "path": "/api/auth/signup"
}
```

---

## 4. 토큰 갱신

### 기본 정보

| 항목 | 내용 |
|------|------|
| **Endpoint** | `POST /api/auth/refresh` |
| **설명** | Refresh Token으로 새로운 Access Token을 발급합니다 |
| **인증** | 불필요 (Refresh Token으로 인증) |

### Request

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| refreshToken | String | O | 리프레시 토큰 |

### Response

#### 200 OK

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**참고**: Refresh Token Rotation 적용 시 새로운 Refresh Token도 함께 반환됩니다.

### Error Response

#### 401 Unauthorized

**유효하지 않은 리프레시 토큰**
```json
{
  "timestamp": "2026-01-10T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "code": "INVALID_REFRESH_TOKEN",
  "message": "유효하지 않거나 만료된 리프레시 토큰입니다",
  "path": "/api/auth/refresh"
}
```

---

## 공통 에러 응답

### 500 Internal Server Error

```json
{
  "timestamp": "2026-01-10T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "서버 내부 오류가 발생했습니다",
  "path": "/api/auth/..."
}
```

---

## 관련 문서
- [카카오 OAuth 회원가입 시퀀스](/docs/sequence/03-kakao-oauth-signup.md)
- [아키텍처 가이드](/docs/architecture/architecture.md)
- [컨벤션 가이드](/docs/convention/convention.md)
