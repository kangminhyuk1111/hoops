# Issue #67 분석 노트: 환경 분리 (로컬/Docker/프로덕션)

## 현재 상태 분석

### 프로파일 파일 현황

| 파일 | 위치 | 용도 | 문제점 |
|------|------|------|--------|
| `application.yml` | main/resources | 기본값 (로컬 개발용으로 사용 중) | 공통 설정과 로컬 설정이 혼재 |
| `application-prod.yml` | main/resources | 프로덕션 환경 | `kafka.enabled: false` (검토 필요) |
| `application-test.yml` | test/resources | 테스트 환경 | H2 사용 (별도 이슈) |
| `application-docker.yml` | **없음** | Docker 환경 | **누락됨** |
| `application-local.yml` | **없음** | 로컬 개발 | **누락됨** |

### 현재 프로파일 활성화 설정

| 환경 | 설정 위치 | 활성화 프로파일 | 실제 사용 파일 |
|------|-----------|----------------|---------------|
| 로컬 개발 | 없음 (기본값) | 없음 | `application.yml` |
| Docker | `docker-compose.yml` | `docker` | `application.yml` (docker 프로파일 없음) |
| 프로덕션 | `user-data.sh` | `docker` | `application.yml` (docker 프로파일 없음) |

### 주요 문제점

1. **docker 프로파일 누락**: `SPRING_PROFILES_ACTIVE=docker` 설정이 있지만 `application-docker.yml`이 없음
2. **application.yml 역할 혼재**: 공통 설정과 로컬 개발 설정이 섞여 있음
3. **환경별 데이터소스 URL 불일치**:
   - 로컬: `localhost:3306`
   - Docker: `mysql:3306` (컨테이너 이름)
   - 현재는 docker-compose에서 환경변수로 덮어쓰는 방식

4. **Kafka 설정 불일치**:
   - 로컬: `localhost:9092`
   - Docker: `kafka:29092` (docker-compose 환경변수)
   - 프로덕션: `kafka.enabled: false` (검토 필요)

---

## 해결 방안

### 1단계: 파일 구조 재설계

```
backend/src/main/resources/
├── application.yml           # 공통 설정 (환경 무관)
├── application-local.yml     # 로컬 개발 (localhost)
├── application-docker.yml    # Docker 환경 (컨테이너명)
└── application-prod.yml      # 프로덕션 (환경변수)
```

### 2단계: 각 파일의 책임 분리

**application.yml (공통 설정)**
- `spring.application.name`
- `server.port`
- JPA 공통 설정 (dialect 제외)
- Kafka serializer/deserializer 설정
- Actuator/Prometheus 설정
- JWT 토큰 만료 시간 (기본값)

**application-local.yml (로컬 개발)**
- `spring.datasource.url: jdbc:mysql://localhost:3306/hoops`
- `spring.kafka.bootstrap-servers: localhost:9092`
- 하드코딩된 개발용 credentials
- DEBUG 레벨 로깅

**application-docker.yml (Docker 환경)**
- `spring.datasource.url: jdbc:mysql://mysql:3306/hoops`
- `spring.kafka.bootstrap-servers: kafka:29092`
- 환경변수 기반 credentials
- INFO 레벨 로깅

**application-prod.yml (프로덕션)**
- 모든 설정 환경변수 기반
- Kafka 활성화 여부 검토
- WARN 레벨 로깅

### 3단계: 프로파일 활성화 변경

| 환경 | 변경 전 | 변경 후 |
|------|---------|---------|
| 로컬 개발 | 없음 | `local` |
| Docker | `docker` | `docker` |
| 프로덕션 EC2 | `docker` | `prod` |

---

## 구현 작업 목록

1. [x] `application.yml` 리팩토링 (공통 설정만 유지)
2. [x] `application-local.yml` 생성 (로컬 개발 설정)
3. [x] `application-docker.yml` 생성 (Docker 환경 설정)
4. [x] `application-prod.yml` 수정 (Kafka disabled 유지)
5. [x] `docker-compose.yml` - `SPRING_PROFILES_ACTIVE=docker` 유지
6. [x] `terraform/scripts/user-data.sh` 수정 (`SPRING_PROFILES_ACTIVE=prod`)
7. [x] `application-test.yml` 수정 (cors.allowed-origins 추가)
8. [x] 테스트 통과 확인

---

## 확인된 사항

1. **Kafka 사용 여부**: 현재 Kafka를 사용하지 않음 → `kafka.enabled: false` 유지
2. **프로파일 이름 결정**:
   - 프로덕션: `prod`
   - 로컬 개발: `local`
   - 테스트: `test`
