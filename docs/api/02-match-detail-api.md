# API 명세서: 경기 상세 조회 (Match Detail)

## 기본 정보

| 항목 | 내용 |
|------|------|
| **Endpoint** | `GET /api/matches/{matchId}` |
| **설명** | 특정 경기의 상세 정보를 조회합니다 |
| **인증** | 불필요 |
| **권한** | 모든 사용자 |

## Request

### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|----------|------|------|------|------|
| `matchId` | Long | O | 조회할 경기 ID | 1 |

### 요청 예시

```
GET /api/matches/1
```

## Response

### 200 OK

```json
{
  "id": 1,
  "hostId": 10,
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

### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 경기 고유 ID |
| `hostId` | Long | 주최자 사용자 ID |
| `hostNickname` | String | 주최자 닉네임 |
| `title` | String | 경기 제목 |
| `description` | String | 경기 설명 |
| `latitude` | BigDecimal | 경기 장소 위도 |
| `longitude` | BigDecimal | 경기 장소 경도 |
| `address` | String | 경기 장소 주소 |
| `matchDate` | String | 경기 날짜 (YYYY-MM-DD) |
| `startTime` | String | 시작 시간 (HH:mm:ss) |
| `endTime` | String | 종료 시간 (HH:mm:ss) |
| `maxParticipants` | Integer | 최대 참가 인원 |
| `currentParticipants` | Integer | 현재 참가 인원 |
| `status` | String | 경기 상태 |

### Match Status

| 상태 | 설명 |
|------|------|
| `PENDING` | 모집 중 |
| `CONFIRMED` | 확정됨 |
| `IN_PROGRESS` | 진행 중 |
| `ENDED` | 종료됨 |
| `CANCELLED` | 취소됨 |
| `FULL` | 정원 마감 |

## Error Response

### Error Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `errorCode` | String | 에러 코드 |
| `message` | String | 에러 메시지 |
| `timestamp` | String | 에러 발생 시간 (ISO 8601) |

### 404 Not Found

**존재하지 않는 경기 ID**
```json
{
  "errorCode": "MATCH_NOT_FOUND",
  "message": "해당 매치를 찾을 수 없습니다. (ID: 999)",
  "timestamp": "2026-01-10T10:30:00"
}
```

### 500 Internal Server Error

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "예상하지 못한 오류가 발생했습니다.",
  "timestamp": "2026-01-10T10:30:00"
}
```

## 성능 고려사항

- Primary Key 기반 조회로 O(1) 성능 보장
- hostNickname은 Match 엔티티에 비정규화되어 저장 (User 테이블 JOIN 불필요)

## 관련 문서
- [경기 목록 조회 API](/docs/api/01-match-list-api.md)
- [경기 생성 API](/docs/api/00-match-creation-api.md)
- [경기 상세 조회 시퀀스](/docs/sequence/02-match-detail.md)
- [아키텍처 가이드](/docs/architecture/architecture.md)
