# API 명세서: 경기 목록 조회 (Match List)

## 기본 정보

| 항목 | 내용 |
|------|------|
| **Endpoint** | `GET /api/matches` |
| **설명** | 위치 기반으로 주변 농구 경기를 조회합니다 |
| **인증** | 불필요 |
| **권한** | 모든 사용자 |

## Request

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|----------|------|------|------|------|
| `latitude` | BigDecimal | O | 조회 중심 위도 | 37.5665 |
| `longitude` | BigDecimal | O | 조회 중심 경도 | 126.9780 |
| `distance` | BigDecimal | O | 조회 반경 (km) | 5 |

### 요청 예시

```
GET /api/matches?latitude=37.5665&longitude=126.9780&distance=5
```

## Response

### 200 OK

```json
[
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
  },
  {
    "id": 2,
    "hostId": 20,
    "hostNickname": "hoops_master",
    "title": "평일 저녁 경기",
    "description": "퇴근 후 가볍게 한 게임!",
    "latitude": 37.5700,
    "longitude": 126.9800,
    "address": "서울특별시 종로구 종로 1",
    "matchDate": "2026-01-16",
    "startTime": "19:00:00",
    "endTime": "21:00:00",
    "maxParticipants": 8,
    "currentParticipants": 5,
    "status": "CONFIRMED"
  }
]
```

**참고**:
- 결과는 조회 중심 위치에서 가까운 순으로 정렬됩니다
- `distance` 파라미터는 km 단위이며, 내부적으로 m로 변환됩니다 (예: 5km → 5000m)
- 빈 배열(`[]`)이 반환될 수 있습니다 (반경 내 경기가 없는 경우)

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

### 500 Internal Server Error

```json
{
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "예상하지 못한 오류가 발생했습니다.",
  "timestamp": "2026-01-10T10:30:00"
}
```

## 관련 문서
- [경기 상세 조회 API](/docs/api/02-match-detail-api.md)
- [경기 생성 API](/docs/api/00-match-creation-api.md)
- [아키텍처 가이드](/docs/architecture/architecture.md)
