# Architecture: Hexagonal & DDD

## 1. 핵심 원칙
- **Domain 격리**: 도메인 로직은 프레임워크, DB, 외부 라이브러리로부터 완전히 독립되어야 한다.
- **의존성 역전**: 모든 의존성은 Domain을 향해야 하며, Infrastructure는 Domain에 정의된 Port를 구현한다.
- **Entity 분리**: Domain Model과 JPA Entity는 별도 패키지로 분리하며, Mapper를 통해 변환한다.

## 2. Package Structure (실제 구조)
```
src/main/java/{domain_name}/
├── domain/
│   ├── model/                    # Pure POJO (비즈니스 로직)
│   │   ├── User.java
│   │   └── UserId.java          # Value Object
│   ├── service/                  # Domain Service
│   │   └── UserDomainService.java
│   └── exception/                # Domain Exception
│       └── UserNotFoundException.java
│
├── application/
│   ├── port/
│   │   ├── in/                   # Inbound Port (Use Case Interface)
│   │   │   └── CreateUserUseCase.java
│   │   └── out/                  # Outbound Port (Repository Interface)
│   │       └── LoadUserPort.java
│   └── service/                  # Application Service (Use Case 구현)
│       └── UserService.java
│
├── adapter/
│   ├── in/
│   │   └── web/                  # REST Controller
│   │       ├── UserController.java
│   │       └── dto/
│   │           ├── UserRequest.java   # record
│   │           └── UserResponse.java  # record
│   └── out/
│       └── persistence/
│           └── UserPersistenceAdapter.java
│
└── infrastructure/
    ├── persistence/
    │   ├── entity/               # JPA Entity (Annotation 허용)
    │   │   └── UserEntity.java
    │   └── repository/           # Spring Data JPA Repository
    │       └── UserJpaRepository.java
    └── mapper/                   # Entity ↔ Domain Mapper
        └── UserEntityMapper.java
```

## 3. Layer Definitions

### 3.1 Domain Layer
- **책임**: 핵심 비즈니스 로직, 비즈니스 규칙 검증
- **제약사항**:
  - Spring, JPA, JSON 등 외부 라이브러리 의존성 금지
  - Infrastructure 계층 참조 금지
  - Pure Java (POJO)만 허용
  - Value Object, Entity, Domain Service 포함

### 3.2 Application Layer
- **책임**: Use Case 구현, 트랜잭션 관리, 도메인 객체 오케스트레이션
- **구성요소**:
  - **Inbound Port**: Use Case 인터페이스 (Service가 구현)
  - **Outbound Port**: Repository, External Service 인터페이스 (Adapter가 구현)
  - **Application Service**: Use Case 구현체

### 3.3 Infrastructure Layer
- **책임**: 외부 시스템 연동 (DB, Kafka, External API)
- **구성요소**:
  - **JPA Entity**: DB 테이블 매핑 (@Entity 허용)
  - **JPA Repository**: Spring Data JPA Interface
  - **Mapper**: Entity ↔ Domain Model 변환
  - **Adapter**: Outbound Port 구현체

### 3.4 Presentation Layer (Adapter In)
- **책임**: HTTP 요청/응답 처리, DTO 변환
- **제약사항**:
  - 비즈니스 로직 포함 금지
  - Entity 직접 반환 금지
  - `record` DTO 사용
  - Use Case 호출만 담당

## 4. Dependency Rule
```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│        (adapter/in/web)             │
│    ┌─────────────────────────┐     │
│    │   Application Layer      │     │
│    │  (application/service)   │     │
│    │  ┌──────────────────┐   │     │
│    │  │  Domain Layer    │   │     │
│    │  │  (domain/model)  │   │     │
│    │  └──────────────────┘   │     │
│    └─────────────────────────┘     │
│                                     │
│    Infrastructure Layer             │
│    (infrastructure/persistence)     │
└─────────────────────────────────────┘

의존성 방향: 외부 → 내부 (Domain)
```

- **규칙**:
  1. Domain은 다른 레이어를 참조하지 않는다.
  2. Application은 Domain만 참조한다.
  3. Infrastructure는 Application/Domain의 Interface(Port)를 구현한다.
  4. Presentation은 Application의 Use Case를 호출한다.

## 5. 금지사항 (Forbidden Patterns)
1. Controller에서 직접 Repository 호출
2. Domain에서 JPA Annotation 사용
3. Service에서 Entity 직접 반환
4. Mapper 없이 Entity ↔ Domain 직접 변환
5. Domain에서 Infrastructure 참조