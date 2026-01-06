# API Specification

## 개요
본 문서는 REST API 명세를 정의합니다. 실제 프로젝트 개발 시 각 엔드포인트를 상세히 작성합니다.

## 공통 사항

### Base URL
```
http://localhost:8080/api
```

### 공통 응답 형식
#### 성공 응답
- HTTP Status: 2xx
- Body: 리소스 또는 DTO

#### 에러 응답
- HTTP Status: 4xx, 5xx
- Body:
```json
{
  "errorCode": "ERROR_CODE",
  "message": "Error message",
  "timestamp": "2026-01-06T12:00:00"
}
```

### 인증 (추후 정의)
- 인증 방식: JWT / OAuth2 / 기타
- Header: `Authorization: Bearer {token}`

## API 엔드포인트

### [Domain Name] API
#### [기능명]
- **Method**: GET/POST/PUT/DELETE
- **Path**: `/path`
- **Description**: 기능 설명
- **Request**:
  - Headers: (필요시)
  - Path Parameters: (필요시)
  - Query Parameters: (필요시)
  - Body: (필요시)
- **Response**:
  - Success (200/201):
  - Error (400/404/500):

---

## 작성 가이드
1. 도메인별로 섹션 분리
2. CRUD 순서로 정리 (Create, Read, Update, Delete)
3. Request/Response 예시는 실제 개발 시 추가
4. 에러 케이스는 convention.md의 예외 체계 참고
