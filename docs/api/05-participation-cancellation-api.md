# API 명세서: 경기 참가 취소 (Participation Cancellation)

## 기본 정보

| 항목 | 내용 |
|------|------|
| **Endpoint** | `DELETE /api/matches/{matchId}/participations/{participationId}` |
| **설명** | 경기 참가를 취소합니다 |
| **인증** | 필수 (JWT Token) |
| **권한** | 본인만 취소 가능 |

## Request

### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `matchId` | Long | Y | 경기 ID |
| `participationId` | Long | Y | 취소할 참가 ID |

### Headers

| 헤더 | 필수 | 설명 |
|------|------|------|
| `Authorization` | Y | Bearer {JWT Token} |

### Request Body

없음

## Response

### 204 No Content

참가 취소 성공 시 응답 본문 없이 204 상태 코드 반환

```http
HTTP/1.1 204 No Content
```

## Error Response

### Error Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `errorCode` | String | 에러 코드 |
| `message` | String | 에러 메시지 |
| `timestamp` | String | 에러 발생 시간 (ISO 8601) |

### 400 Bad Request

**취소 불가능한 참가 상태**
```json
{
  "errorCode": "INVALID_PARTICIPATION_STATUS",
  "message": "취소할 수 없는 참가 상태입니다. (participationId: 1, status: CANCELLED)",
  "timestamp": "2026-01-10T14:30:00"
}
```

**이미 시작된 경기**
```json
{
  "errorCode": "MATCH_ALREADY_STARTED",
  "message": "이미 시작된 경기는 참가를 취소할 수 없습니다. (matchId: 123)",
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

### 403 Forbidden

**본인의 참가가 아님**
```json
{
  "errorCode": "NOT_PARTICIPANT",
  "message": "본인의 참가만 취소할 수 있습니다. (participationId: 1, userId: 456)",
  "timestamp": "2026-01-10T14:30:00"
}
```

### 404 Not Found

**존재하지 않는 참가 정보**
```json
{
  "errorCode": "PARTICIPATION_NOT_FOUND",
  "message": "참가 정보를 찾을 수 없습니다. (participationId: 999)",
  "timestamp": "2026-01-10T14:30:00"
}
```

**존재하지 않는 경기**
```json
{
  "errorCode": "PARTICIPATION_MATCH_NOT_FOUND",
  "message": "경기를 찾을 수 없습니다. (matchId: 999)",
  "timestamp": "2026-01-10T14:30:00"
}
```

### 409 Conflict

**동시성 충돌 (낙관적 락)**
```json
{
  "errorCode": "CANCELLATION_CONFLICT",
  "message": "동시에 다른 요청이 처리되어 충돌이 발생했습니다. 다시 시도해주세요. (matchId: 123)",
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

### 취소 조건
1. 인증된 사용자만 취소 가능
2. 본인의 참가만 취소 가능
3. `CONFIRMED` 상태인 참가만 취소 가능
4. 이미 `CANCELLED` 상태인 참가는 재취소 불가
5. 경기 시작 전에만 취소 가능

### 취소 시 처리 사항
1. 참가 상태를 `CONFIRMED` → `CANCELLED`로 변경
2. 매치의 `currentParticipants`를 1 감소
3. 매치 상태가 `FULL`이었다면 `PENDING`으로 변경 (자리 생김)
4. 호스트에게 참가 취소 알림 발송 (비동기)

### 동시성 처리
- 낙관적 락(Optimistic Lock)을 사용하여 동시 요청 시 충돌 방지
- 충돌 발생 시 최대 3회 자동 재시도
- 재시도 실패 시 `409 Conflict` 응답

### 보상 트랜잭션
- Match 상태 변경 후 Participation 저장 실패 시 Match 상태 복원
- 전체 작업의 원자성 보장

### 상태 전이

**Participation 상태**
```
CONFIRMED → [취소 요청] → CANCELLED
```

**Match 상태 (취소로 인한 변경)**
```
FULL → [참가자 취소] → PENDING (자리 생김)
PENDING → [참가자 취소] → PENDING (유지)
```

## 예시

### cURL

```bash
curl -X DELETE "https://api.hoops.com/api/matches/123/participations/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Response Example

```http
HTTP/1.1 204 No Content
```

### 에러 응답 예시

```http
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "errorCode": "NOT_PARTICIPANT",
  "message": "본인의 참가만 취소할 수 있습니다. (participationId: 1, userId: 456)",
  "timestamp": "2026-01-10T14:30:00"
}
```

## 시나리오별 예시

### 1. 정상 취소
- 사용자가 본인의 CONFIRMED 상태 참가를 취소
- 경기가 아직 시작 전
- 결과: 204 No Content

### 2. 이미 취소된 참가
- 사용자가 CANCELLED 상태의 참가를 재취소 시도
- 결과: 400 Bad Request (INVALID_PARTICIPATION_STATUS)

### 3. 타인의 참가 취소 시도
- 사용자가 다른 사람의 참가를 취소 시도
- 결과: 403 Forbidden (NOT_PARTICIPANT)

### 4. FULL 경기에서 취소
- 정원이 가득 찬 경기에서 참가 취소
- 매치 상태가 FULL → PENDING으로 변경
- 다른 사용자가 참가 가능해짐

## 관련 문서
- [경기 참가 취소 시퀀스 다이어그램](/docs/sequence/05-participation-cancellation.md)
- [경기 참가 신청 API](/docs/api/04-participation-api.md)
- [경기 상세 조회 API](/docs/api/02-match-detail-api.md)
