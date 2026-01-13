# API 명세서: 경기 복구 (Match Reactivate)

## 기본 정보

| 항목 | 내용 |
|------|------|
| **Endpoint** | `POST /api/matches/{matchId}/reactivate` |
| **설명** | 취소된 경기를 복구합니다 |
| **인증** | 필수 (JWT Token) |
| **권한** | 경기 호스트만 가능 |

## 비즈니스 규칙

| 규칙 | 설명 |
|------|------|
| **복구 가능 시간** | 취소 후 1시간 이내에만 복구 가능 |
| **경기 날짜** | 경기 날짜가 아직 지나지 않은 경우에만 복구 가능 |
| **호스트 권한** | 경기를 생성한 호스트만 복구 가능 |
| **참가자 데이터** | 기존 참가자 데이터는 그대로 유지됨 |

## Request

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `matchId` | Long | 복구할 경기 ID |

**Request Body**: 없음

## Response

### 200 OK

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
  "currentParticipants": 3,
  "status": "PENDING"
}
```

**참고**:
- 복구 후 상태는 `PENDING`으로 변경됩니다
- `currentParticipants`는 취소 전 값을 유지합니다

## Error Response

### Error Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `errorCode` | String | 에러 코드 |
| `message` | String | 에러 메시지 |
| `timestamp` | String | 에러 발생 시간 (ISO 8601) |

### 400 Bad Request

**취소되지 않은 경기 복구 시도**
```json
{
  "errorCode": "MATCH_CANNOT_REACTIVATE",
  "message": "경기를 복구할 수 없습니다. (matchId: 123, 사유: 취소된 경기만 복구할 수 있습니다)",
  "timestamp": "2026-01-09T10:30:00"
}
```

**복구 가능 시간 초과 (1시간)**
```json
{
  "errorCode": "MATCH_CANNOT_REACTIVATE",
  "message": "경기를 복구할 수 없습니다. (matchId: 123, 사유: 복구 가능 시간(1시간)이 지났거나 경기 날짜가 이미 지났습니다)",
  "timestamp": "2026-01-09T10:30:00"
}
```

**경기 날짜가 이미 지난 경우**
```json
{
  "errorCode": "MATCH_CANNOT_REACTIVATE",
  "message": "경기를 복구할 수 없습니다. (matchId: 123, 사유: 복구 가능 시간(1시간)이 지났거나 경기 날짜가 이미 지났습니다)",
  "timestamp": "2026-01-09T10:30:00"
}
```

### 403 Forbidden

**호스트가 아닌 사용자 복구 시도**
```json
{
  "errorCode": "NOT_MATCH_HOST",
  "message": "경기 호스트만 이 작업을 수행할 수 있습니다. (matchId: 123, userId: 789)",
  "timestamp": "2026-01-09T10:30:00"
}
```

### 404 Not Found

**존재하지 않는 경기**
```json
{
  "errorCode": "MATCH_NOT_FOUND",
  "message": "경기를 찾을 수 없습니다. (matchId: 999)",
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

## 사용 예시

### curl

```bash
curl -X POST "https://api.hoops.com/api/matches/123/reactivate" \
  -H "Authorization: Bearer {access_token}" \
  -H "Content-Type: application/json"
```

### JavaScript (fetch)

```javascript
const response = await fetch('/api/matches/123/reactivate', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
});

const match = await response.json();
console.log(`경기가 복구되었습니다. 상태: ${match.status}`);
```

## 관련 문서
- [경기 취소 API](/docs/api/00-match-creation-api.md)
- [아키텍처 가이드](/docs/architecture/architecture.md)
