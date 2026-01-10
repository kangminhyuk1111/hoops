# API 명세서: 경기 참가 신청 (Match Participation)

## 기본 정보

| 항목 | 내용 |
|------|------|
| **Endpoint** | `POST /api/matches/{matchId}/participations` |
| **설명** | 경기에 참가를 신청합니다 |
| **인증** | 필수 (JWT Token) |
| **권한** | 인증된 사용자 |

## Request

### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `matchId` | Long | Y | 참가할 경기 ID |

### Headers

| 헤더 | 필수 | 설명 |
|------|------|------|
| `Authorization` | Y | Bearer {JWT Token} |

### Request Body

없음 (사용자 ID는 JWT Token에서 자동 추출)

## Response

### 201 Created

```json
{
  "id": 1,
  "matchId": 123,
  "userId": 456,
  "status": "CONFIRMED",
  "joinedAt": "2026-01-10T14:30:00"
}
```

### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 참가 ID |
| `matchId` | Long | 경기 ID |
| `userId` | Long | 참가자 사용자 ID |
| `status` | String | 참가 상태 (CONFIRMED) |
| `joinedAt` | String | 참가 신청 시간 (ISO 8601) |

### Participation Status

| 상태 | 설명 |
|------|------|
| `PENDING` | 참가 대기 중 (처리 중간 상태) |
| `CONFIRMED` | 참가 확정 |
| `CANCELLED` | 참가 취소 |
| `MATCH_CANCELLED` | 경기 취소로 인한 참가 취소 |

## Error Response

### Error Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `errorCode` | String | 에러 코드 |
| `message` | String | 에러 메시지 |
| `timestamp` | String | 에러 발생 시간 (ISO 8601) |

### 400 Bad Request

**호스트가 자신의 경기에 참가 시도**
```json
{
  "errorCode": "HOST_CANNOT_PARTICIPATE",
  "message": "호스트는 자신의 경기에 참가할 수 없습니다. (matchId: 123, userId: 456)",
  "timestamp": "2026-01-10T14:30:00"
}
```

**경기 정원 초과**
```json
{
  "errorCode": "MATCH_FULL",
  "message": "경기 정원이 가득 찼습니다. (matchId: 123)",
  "timestamp": "2026-01-10T14:30:00"
}
```

**참가 불가능한 경기 상태**
```json
{
  "errorCode": "INVALID_MATCH_STATUS",
  "message": "참가할 수 없는 경기 상태입니다. (matchId: 123, status: CANCELLED)",
  "timestamp": "2026-01-10T14:30:00"
}
```

### 401 Unauthorized

**인증 토큰 없음 또는 유효하지 않음**
```json
{
  "errorCode": "UNAUTHORIZED",
  "message": "인증이 필요합니다.",
  "timestamp": "2026-01-10T14:30:00"
}
```

### 404 Not Found

**존재하지 않는 경기**
```json
{
  "errorCode": "PARTICIPATION_MATCH_NOT_FOUND",
  "message": "참가 신청할 경기를 찾을 수 없습니다. (matchId: 999)",
  "timestamp": "2026-01-10T14:30:00"
}
```

### 409 Conflict

**중복 참가 신청**
```json
{
  "errorCode": "ALREADY_PARTICIPATING",
  "message": "이미 참가 신청한 경기입니다. (matchId: 123, userId: 456)",
  "timestamp": "2026-01-10T14:30:00"
}
```

**동시성 충돌 (낙관적 락)**
```json
{
  "errorCode": "PARTICIPATION_CONFLICT",
  "message": "동시에 다른 사용자가 참가 신청하여 충돌이 발생했습니다. 다시 시도해주세요. (matchId: 123)",
  "timestamp": "2026-01-10T14:30:00"
}
```

### 500 Internal Server Error

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "예상하지 못한 오류가 발생했습니다.",
  "timestamp": "2026-01-10T14:30:00"
}
```

## 비즈니스 규칙

### 참가 조건
1. 인증된 사용자만 참가 신청 가능
2. 호스트는 자신의 경기에 참가 불가 (이미 참가자로 포함됨)
3. 경기 상태가 `PENDING` 또는 `CONFIRMED`인 경우만 참가 가능
4. 현재 참가자 수가 최대 참가자 수 미만인 경우만 참가 가능
5. 동일 경기에 중복 참가 불가

### 동시성 처리
- 낙관적 락(Optimistic Lock)을 사용하여 동시 참가 신청 시 충돌 방지
- 충돌 발생 시 최대 3회 자동 재시도 (100ms, 200ms, 400ms 간격)
- 재시도 실패 시 `409 Conflict` 응답

### 상태 전이
```
[참가 신청] → PENDING → [매치 검증 성공] → CONFIRMED
                     → [매치 검증 실패] → 삭제 (롤백)
```

## 예시

### cURL

```bash
curl -X POST "https://api.hoops.com/api/matches/123/participations" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### Response Example

```http
HTTP/1.1 201 Created
Content-Type: application/json
Location: /api/matches/123/participations/1

{
  "id": 1,
  "matchId": 123,
  "userId": 456,
  "status": "CONFIRMED",
  "joinedAt": "2026-01-10T14:30:00"
}
```

## 관련 문서
- [경기 참가 신청 시퀀스 다이어그램](/docs/sequence/04-match-participation.md)
- [경기 생성 API](/docs/api/00-match-creation-api.md)
- [경기 상세 조회 API](/docs/api/02-match-detail-api.md)
