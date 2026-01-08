# Database Migration Scripts

## 개요
Hoops 프로젝트의 데이터베이스 스키마 관리 스크립트입니다.

---

## 파일 구조

```
src/main/resources/db/
├── migration/                    # Flyway 마이그레이션 스크립트
│   ├── V1__init_schema.sql      # 초기 스키마 생성
│   └── V2__insert_sample_data.sql # 샘플 데이터 삽입
├── schema.sql                    # 전체 스키마 (개발용)
├── drop.sql                      # 스키마 삭제 (초기화용)
└── README.md                     # 이 파일
```

---

## 사용 방법

### 1. Flyway를 사용하는 경우 (권장)

**설정 파일 (application.yml)**:
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

**실행**:
```bash
# Spring Boot 실행 시 자동으로 마이그레이션 실행됨
./gradlew bootRun
```

**Flyway 명령어**:
```bash
# 마이그레이션 상태 확인
./gradlew flywayInfo

# 마이그레이션 실행
./gradlew flywayMigrate

# 마이그레이션 롤백 (Flyway Teams 필요)
./gradlew flywayUndo

# 마이그레이션 초기화
./gradlew flywayClean
```

---

### 2. Flyway를 사용하지 않는 경우

**스키마 생성**:
```bash
mysql -u root -p hoops < src/main/resources/db/schema.sql
```

**스키마 삭제 (초기화)**:
```bash
mysql -u root -p hoops < src/main/resources/db/drop.sql
```

**샘플 데이터 삽입**:
```bash
mysql -u root -p hoops < src/main/resources/db/migration/V2__insert_sample_data.sql
```

---

## 마이그레이션 파일 네이밍 규칙

Flyway는 다음 네이밍 규칙을 따릅니다:

```
V{version}__{description}.sql
```

**예시**:
- `V1__init_schema.sql` - 버전 1, 초기 스키마
- `V2__insert_sample_data.sql` - 버전 2, 샘플 데이터
- `V3__add_user_rating_column.sql` - 버전 3, user 테이블에 rating 컬럼 추가

**규칙**:
- `V` + 버전 번호 + `__` (언더스코어 2개) + 설명
- 버전 번호는 순차적으로 증가
- 한번 적용된 마이그레이션은 수정 불가

---

## 데이터베이스 생성

**MySQL 접속 후**:
```sql
CREATE DATABASE hoops DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**사용자 생성 및 권한 부여**:
```sql
CREATE USER 'hoops_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON hoops.* TO 'hoops_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 주요 테이블 목록

| 테이블명 | 도메인 | 설명 |
|---------|-------|------|
| users | User | 사용자 정보 |
| auth_accounts | Auth | 인증 계정 (JWT, OAuth) |
| locations | Location | 사용자 생성 지역 정보 |
| matches | Match | 경기 정보 |
| participations | Participation | 경기 참가 정보 |
| notifications | Notification | 사용자 알림 |

---

## 샘플 데이터 설명

`V2__insert_sample_data.sql`에는 다음 샘플 데이터가 포함되어 있습니다:

- **사용자**: 5명 (Host, Player1~3, Newbie)
- **인증 계정**: LOCAL(3개), GOOGLE(1개), KAKAO(1개)
- **지역**: 4개 (강남, 잠실, 홍대, 부산)
- **경기**: 4개 (확정, 마감, 대기, 취소)
- **참가**: 여러 참가 내역
- **알림**: 5개 알림 샘플

---

## 트러블슈팅

### 1. Flyway 마이그레이션 실패 시
```bash
# Flyway 히스토리 확인
./gradlew flywayInfo

# Flyway 완전 초기화 (주의: 모든 데이터 삭제)
./gradlew flywayClean
./gradlew flywayMigrate
```

### 2. 외래키 제약조건 에러
- `drop.sql` 실행 시 `SET FOREIGN_KEY_CHECKS = 0;` 사용

### 3. MySQL 버전 호환성
- MySQL 5.7 이상 권장
- SPATIAL INDEX는 InnoDB 엔진 필요 (MySQL 5.7.5+)

---

## 다음 단계

1. **build.gradle.kts**에 Flyway 의존성 추가:
```kotlin
dependencies {
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
}
```

2. **application.yml**에 Flyway 설정 추가

3. 애플리케이션 실행 시 자동 마이그레이션 확인
