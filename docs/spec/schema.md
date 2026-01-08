# Database Schema

## 개요
본 문서는 Hoops 프로젝트의 전체 데이터베이스 스키마를 정의합니다.

---

## ERD (Entity Relationship Diagram)

```
┌─────────────────┐
│   users         │
├─────────────────┤
│ id (PK)         │
│ email           │
│ nickname        │
│ profile_image   │
│ rating          │
│ created_at      │
│ updated_at      │
└─────────────────┘
         │
         │ 1
         │
         │ N
┌─────────────────┐         ┌──────────────────┐
│ auth_accounts   │         │   locations      │
├─────────────────┤         ├──────────────────┤
│ id (PK)         │         │ id (PK)          │
│ user_id (FK)    │         │ user_id (FK)     │
│ provider        │         │ alias            │
│ provider_id     │         │ latitude         │
│ password_hash   │         │ longitude        │
│ created_at      │         │ address          │
│ updated_at      │         │ created_at       │
└─────────────────┘         │ updated_at       │
                            └──────────────────┘
         │                           │
         │ 1                         │ (참조용)
         │                           │
         │ N                         │
┌─────────────────────────────┐     │
│        matches              │     │
├─────────────────────────────┤     │
│ id (PK)                     │     │
│ host_id (FK) -> users       │     │
│ title                       │     │
│ description                 │     │
│ latitude  (역정규화)         │◄────┘
│ longitude (역정규화)         │
│ address                     │
│ match_date                  │
│ start_time                  │
│ end_time                    │
│ max_participants            │
│ current_participants        │
│ status (ENUM)               │
│ created_at                  │
│ updated_at                  │
└─────────────────────────────┘
         │
         │ 1
         │
         │ N
┌─────────────────────────────┐
│    participations           │
├─────────────────────────────┤
│ id (PK)                     │
│ match_id (FK) -> matches    │
│ user_id (FK) -> users       │
│ status (ENUM)               │
│ joined_at                   │
│ created_at                  │
│ updated_at                  │
└─────────────────────────────┘
         │
         │
         │
┌─────────────────────────────┐
│     notifications           │
├─────────────────────────────┤
│ id (PK)                     │
│ user_id (FK) -> users       │
│ type (ENUM)                 │
│ title                       │
│ message                     │
│ related_match_id (FK)       │
│ is_read                     │
│ created_at                  │
└─────────────────────────────┘
```

---

## 도메인별 테이블 정의

### 1. User 도메인

#### 1.1. users (사용자)
**설명**: 비즈니스 로직에서 사용하는 사용자 정보

| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|-------|------|------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | Primary Key |
| email | VARCHAR(255) | NO | - | 이메일 (고유) |
| nickname | VARCHAR(50) | NO | - | 닉네임 (고유) |
| profile_image | VARCHAR(500) | YES | NULL | 프로필 이미지 URL |
| rating | DECIMAL(3,2) | NO | 0.00 | 사용자 평점 (0.00 ~ 5.00) |
| total_matches | INT | NO | 0 | 총 참가 경기 수 |
| created_at | DATETIME | NO | CURRENT_TIMESTAMP | 생성일시 |
| updated_at | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스**:
- PRIMARY KEY: `id`
- UNIQUE INDEX: `email`
- UNIQUE INDEX: `nickname`

**제약조건**:
- CHECK: `rating >= 0.00 AND rating <= 5.00`

---

### 2. Auth 도메인

#### 2.1. auth_accounts (인증 계정)
**설명**: 인증 관련 정보 (JWT, OAuth 2.0)

| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|-------|------|------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | Primary Key |
| user_id | BIGINT | NO | - | users.id 참조 |
| provider | VARCHAR(50) | NO | - | 인증 제공자 (LOCAL, GOOGLE, KAKAO, NAVER) |
| provider_id | VARCHAR(255) | YES | NULL | OAuth 제공자의 사용자 ID |
| password_hash | VARCHAR(255) | YES | NULL | 비밀번호 해시 (LOCAL만 사용) |
| refresh_token | VARCHAR(500) | YES | NULL | Refresh Token |
| created_at | DATETIME | NO | CURRENT_TIMESTAMP | 생성일시 |
| updated_at | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스**:
- PRIMARY KEY: `id`
- UNIQUE INDEX: `user_id, provider` (복합 인덱스)
- INDEX: `provider, provider_id`

**제약조건**:
- FOREIGN KEY: `user_id` REFERENCES `users(id)` ON DELETE CASCADE
- CHECK: `provider IN ('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER')`
- CHECK: `(provider = 'LOCAL' AND password_hash IS NOT NULL) OR (provider != 'LOCAL' AND provider_id IS NOT NULL)`

---

### 3. Location 도메인

#### 3.1. locations (지역)
**설명**: 사용자가 생성한 경기 지역 정보 (별칭 포함)

| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|-------|------|------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | Primary Key |
| user_id | BIGINT | NO | - | 생성한 사용자 ID |
| alias | VARCHAR(100) | NO | - | 지역 별칭 (예: "우리 동네 농구장") |
| latitude | DECIMAL(10,8) | NO | - | 위도 |
| longitude | DECIMAL(11,8) | NO | - | 경도 |
| address | VARCHAR(500) | YES | NULL | 주소 (선택) |
| created_at | DATETIME | NO | CURRENT_TIMESTAMP | 생성일시 |
| updated_at | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스**:
- PRIMARY KEY: `id`
- INDEX: `user_id`
- SPATIAL INDEX: `latitude, longitude` (공간 인덱스 - 위치 기반 검색 최적화)

**제약조건**:
- FOREIGN KEY: `user_id` REFERENCES `users(id)` ON DELETE CASCADE
- CHECK: `latitude >= -90 AND latitude <= 90`
- CHECK: `longitude >= -180 AND longitude <= 180`

---

### 4. Match 도메인

#### 4.1. matches (경기)
**설명**: 농구 경기 정보 (Location 역정규화)

| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|-------|------|------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | Primary Key |
| host_id | BIGINT | NO | - | 경기 주최자 (users.id) |
| title | VARCHAR(200) | NO | - | 경기 제목 |
| description | TEXT | YES | NULL | 경기 설명 |
| latitude | DECIMAL(10,8) | NO | - | 위도 (Location에서 복사) |
| longitude | DECIMAL(11,8) | NO | - | 경도 (Location에서 복사) |
| address | VARCHAR(500) | YES | NULL | 주소 |
| match_date | DATE | NO | - | 경기 날짜 |
| start_time | TIME | NO | - | 시작 시간 |
| end_time | TIME | NO | - | 종료 시간 |
| max_participants | INT | NO | - | 최대 참가 인원 |
| current_participants | INT | NO | 0 | 현재 참가 인원 |
| status | VARCHAR(50) | NO | PENDING | 경기 상태 (PENDING, CONFIRMED, IN_PROGRESS, ENDED, CANCELLED, FULL) |
| created_at | DATETIME | NO | CURRENT_TIMESTAMP | 생성일시 |
| updated_at | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스**:
- PRIMARY KEY: `id`
- INDEX: `host_id`
- INDEX: `status`
- INDEX: `match_date, start_time`
- SPATIAL INDEX: `latitude, longitude` (위치 기반 검색)
- INDEX: `status, match_date, start_time` (복합 인덱스 - 스케쥴러용)

**제약조건**:
- FOREIGN KEY: `host_id` REFERENCES `users(id)` ON DELETE RESTRICT
- CHECK: `max_participants > 0`
- CHECK: `current_participants >= 0 AND current_participants <= max_participants`
- CHECK: `status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'ENDED', 'CANCELLED', 'FULL')`
- CHECK: `latitude >= -90 AND latitude <= 90`
- CHECK: `longitude >= -180 AND longitude <= 180`

---

### 5. Participation 도메인

#### 5.1. participations (참가)
**설명**: 경기 참가 정보 (Host 포함)

| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|-------|------|------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | Primary Key |
| match_id | BIGINT | NO | - | matches.id 참조 |
| user_id | BIGINT | NO | - | users.id 참조 |
| status | VARCHAR(50) | NO | PENDING | 참가 상태 (PENDING, CONFIRMED, CANCELLED, MATCH_CANCELLED) |
| joined_at | DATETIME | NO | CURRENT_TIMESTAMP | 참가 신청 일시 |
| created_at | DATETIME | NO | CURRENT_TIMESTAMP | 생성일시 |
| updated_at | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스**:
- PRIMARY KEY: `id`
- UNIQUE INDEX: `match_id, user_id` (중복 참가 방지)
- INDEX: `user_id`
- INDEX: `status`
- INDEX: `match_id, status` (복합 인덱스)

**제약조건**:
- FOREIGN KEY: `match_id` REFERENCES `matches(id)` ON DELETE CASCADE
- FOREIGN KEY: `user_id` REFERENCES `users(id)` ON DELETE CASCADE
- CHECK: `status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'MATCH_CANCELLED')`

---

### 6. Notification 도메인

#### 6.1. notifications (알림)
**설명**: 사용자 알림 정보

| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|-------|------|------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | Primary Key |
| user_id | BIGINT | NO | - | 알림 수신자 (users.id) |
| type | VARCHAR(50) | NO | - | 알림 타입 (PARTICIPATION_CREATED, PARTICIPATION_CANCELLED, MATCH_UPCOMING, MATCH_CANCELLED, MATCH_FULL) |
| title | VARCHAR(200) | NO | - | 알림 제목 |
| message | TEXT | NO | - | 알림 내용 |
| related_match_id | BIGINT | YES | NULL | 관련 경기 ID (선택) |
| is_read | BOOLEAN | NO | FALSE | 읽음 여부 |
| created_at | DATETIME | NO | CURRENT_TIMESTAMP | 생성일시 |

**인덱스**:
- PRIMARY KEY: `id`
- INDEX: `user_id, is_read` (복합 인덱스 - 읽지 않은 알림 조회)
- INDEX: `created_at`
- INDEX: `related_match_id`

**제약조건**:
- FOREIGN KEY: `user_id` REFERENCES `users(id)` ON DELETE CASCADE
- FOREIGN KEY: `related_match_id` REFERENCES `matches(id)` ON DELETE SET NULL
- CHECK: `type IN ('PARTICIPATION_CREATED', 'PARTICIPATION_CANCELLED', 'MATCH_UPCOMING', 'MATCH_CANCELLED', 'MATCH_FULL')`

---

## 주요 설계 결정 사항

### 1. Location 역정규화
- Match 테이블에 `latitude`, `longitude`를 직접 보유
- **이유**:
  - Location이 변경되어도 과거 Match의 위치 정보는 불변이어야 함
  - 조회 성능 향상 (JOIN 불필요)
  - Location은 사용자 편의를 위한 "템플릿" 역할

### 2. Host도 Participation에 포함
- Match 생성 시 Host는 자동으로 `participations` 테이블에 추가됨
- **이유**:
  - 일관된 참가자 관리
  - 알림 발송 로직 단순화
  - 참가자 수 계산 일관성

### 3. 상태(Status) 관리
**Match 상태**:
- `PENDING`: 생성됨, 확정 대기
- `CONFIRMED`: 확정됨
- `IN_PROGRESS`: 진행 중
- `ENDED`: 종료됨
- `CANCELLED`: 취소됨
- `FULL`: 정원 마감

**Participation 상태**:
- `PENDING`: 신청 대기
- `CONFIRMED`: 참가 확정
- `CANCELLED`: 참가 취소 (사용자가 취소)
- `MATCH_CANCELLED`: 경기 취소됨 (Host가 경기 취소)

### 4. 인덱스 전략
- **위치 기반 검색**: SPATIAL INDEX 사용
- **복합 인덱스**: 자주 함께 조회되는 컬럼 (예: `status, match_date, start_time`)
- **UNIQUE 제약**: 중복 방지 (예: `match_id, user_id` - 중복 참가 방지)

### 5. Soft Delete vs Hard Delete
- 현재는 Hard Delete 사용
- 추가 요구사항 발생 시 `deleted_at` 컬럼 추가하여 Soft Delete 전환 가능

---

## 다음 단계
1. DDL 스크립트 작성 (테이블 생성 SQL)
2. 초기 데이터 마이그레이션 전략 수립
3. JPA Entity 설계 (`infrastructure/persistence/entity` 패키지)
4. Domain Model 설계 (`domain/model` 패키지)
