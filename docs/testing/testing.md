# Testing Guide

## 1. 핵심 원칙
- **No Mocking**: 실제 동작하는 코드를 테스트하며, Mock 사용을 지양한다.
- **Testcontainers 활용**: 실제 DB, Kafka 등의 인프라를 Docker Container로 구동하여 통합 테스트를 수행한다.
- **BDD 스타일**: Given-When-Then 패턴으로 테스트 코드를 작성하여 가독성을 높인다.

## 2. BDD 스타일 테스트 (Given-When-Then)

### 2.1 테스트 작성 규칙
- **Test Method Naming**: `should_DoSomething_When_GivenSomething` 형식 사용 (Snake Case 허용)
- **주석으로 구조화**: Given-When-Then 주석을 명시적으로 작성
- **의도 명확화**: 각 섹션에서 무엇을 준비하고, 실행하고, 검증하는지 명확히 표현

### 2.2 BDD 패턴 구조
- **Given**: 테스트에 필요한 데이터 및 상태 준비
- **When**: 테스트 대상 메서드 실행
- **Then**: 결과 검증

### 2.3 레이어별 테스트
- **Domain Layer 테스트**: 순수 단위 테스트 (POJO 검증)
- **Application Service 테스트**: 통합 테스트 (DB 연동 포함)
- **Controller 테스트**: API 통합 테스트 (HTTP 요청/응답 검증)

## 3. Testcontainers 사용법

### 3.1 의존성 추가 (build.gradle)
```groovy
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.testcontainers:testcontainers:1.19.0'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.0'
    testImplementation 'org.testcontainers:mysql:1.19.0'
    testImplementation 'org.testcontainers:kafka:1.19.0'
}
```

### 3.2 MySQL Testcontainer 설정
- `@Testcontainers` 어노테이션 사용
- `@Container` 어노테이션으로 컨테이너 정의
- `@DynamicPropertySource`로 Spring 프로퍼티 동적 설정
- `withReuse(true)` 옵션으로 컨테이너 재사용 (성능 향상)

### 3.3 Kafka Testcontainer 설정
- KafkaContainer 사용
- DockerImageName으로 이미지 지정
- `@DynamicPropertySource`로 bootstrap-servers 설정

### 3.4 복합 Testcontainer 설정
- 여러 컨테이너를 동시에 사용 가능 (MySQL + Kafka)
- 각 컨테이너별로 `@Container` 정의
- 단일 `@DynamicPropertySource`에서 모든 설정 통합

### 3.5 Testcontainer 최적화
- Base 클래스 패턴으로 공통 컨테이너 설정 공유
- `withReuse(true)` 옵션으로 테스트 클래스 간 컨테이너 재사용
- `@BeforeEach`로 각 테스트 전 초기화

## 4. 테스트 작성 시 주의사항

### 4.1 금지사항
- `@MockBean`, `@Mock` 사용 지양 (실제 객체 우선)
- H2 In-Memory DB 대신 실제 MySQL Container 사용
- Given-When-Then 주석 없이 테스트 작성
- 하나의 테스트에 여러 검증 로직 혼합 (Single Responsibility)

### 4.2 권장사항
- Testcontainers로 실제 인프라 환경 재현
- 테스트 메서드명은 의도를 명확히 표현 (`should_X_When_Y`)
- AssertJ 라이브러리 활용 (`assertThat()`)
- 테스트 데이터는 Given 섹션에서 명확히 준비
- 각 테스트는 독립적으로 실행 가능해야 함 (`@BeforeEach`로 초기화)

## 5. 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests UserServiceTest

# 통합 테스트만 실행 (Testcontainers 포함)
./gradlew integrationTest
```
