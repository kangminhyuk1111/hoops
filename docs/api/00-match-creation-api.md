# API 명세서: 경기 생성 (Match Creation)

## 기본 정보

| 항목 | 내용 |
|------|------|
| **Endpoint** | `POST /api/matches` |
| **설명** | 새로운 농구 경기를 생성합니다 |
| **인증** | 필수 (JWT Token) |
| **권한** | 인증된 사용자 |

## Request

```json
{
  "locationId": 1,
  "title": "주말 농구 경기",
  "description": "주말 오후 경기입니다. 초보자도 환영합니다!",
  "matchDate": "2026-01-15",
  "startTime": "14:00:00",
  "endTime": "16:00:00",
  "maxParticipants": 10
}
```

**참고**: `hostId`는 JWT Token에서 자동 추출되므로 Request에 포함하지 않습니다.

## Response

### 201 Created

```json
{
  "id": 123,
  "hostId": 456,
  "hostNickname": "basketball_lover",
  "title": "주말 농구 경기",
  "description": "주말 오후 경기입니다. 초보자도 환영합니다!",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "address": "서울특별시 중구 세종대로 110",
  "matchDate": "2026-01-15",
  "startTime": "14:00:00",
  "endTime": "16:00:00",
  "maxParticipants": 10,
  "currentParticipants": 1,
  "status": "PENDING"
}
```

**참고**:
- `currentParticipants`는 1로 시작합니다 (호스트 포함)
- `hostNickname`은 경기 생성 시 Match 엔티티에 저장됩니다

## Error Response

### Error Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `errorCode` | String | 에러 코드 |
| `message` | String | 에러 메시지 |
| `timestamp` | String | 에러 발생 시간 (ISO 8601) |

### Validation Error Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `errorCode` | String | 에러 코드 (VALIDATION_FAILED) |
| `message` | String | 에러 메시지 |
| `errors` | Object | 필드별 에러 메시지 |

### 400 Bad Request

**Validation 실패**
```json
{
  "errorCode": "VALIDATION_FAILED",
  "message": "입력값 검증에 실패했습니다",
  "errors": {
    "title": "경기 제목은 필수입니다"
  }
}
```

**유효하지 않은 날짜/시간**
```json
{
  "errorCode": "INVALID_MATCH_DATE",
  "message": "경기 날짜는 과거일 수 없습니다. (날짜: 2024-01-01)",
  "timestamp": "2026-01-09T10:30:00"
}
```

**종료 시간이 시작 시간보다 빠름**
```json
{
  "errorCode": "INVALID_TIME_RANGE",
  "message": "시작 시간은 종료 시간보다 빨라야 합니다. (시작: 16:00, 종료: 14:00)",
  "timestamp": "2026-01-09T10:30:00"
}
```

**최소 참가 인원 미달**
```json
{
  "errorCode": "INVALID_MAX_PARTICIPANTS",
  "message": "최대 참가 인원은 최소 4명 이상이어야 합니다. (입력값: 2)",
  "timestamp": "2026-01-09T10:30:00"
}
```

### 404 Not Found

**존재하지 않는 Location**
```json
{
  "errorCode": "LOCATION_NOT_FOUND",
  "message": "존재하지 않는 장소입니다. (ID: 999)",
  "timestamp": "2026-01-09T10:30:00"
}
```

**존재하지 않는 User**
```json
{
  "errorCode": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다. (userId: 999)",
  "timestamp": "2026-01-09T10:30:00"
}
```

### 500 Internal Server Error

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "예상하지 못한 오류가 발생했습니다.",
  "timestamp": "2026-01-09T10:30:00"
}
```

## 관련 문서
- [경기 생성 시퀀스 다이어그램](/docs/sequence/00-match-creation)
- [아키텍처 가이드](/docs/architecture/architecture.md)
- [데이터베이스 스키마](/src/main/resources/db/schema.sql)
