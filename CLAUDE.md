# CLAUDE.md

## Skills

- `/architecture-patterns` - Hexagonal Architecture 가이드 (패키지 구조, 계층 규칙)
- `/clean-code` - Self-validating entity, Tell Don't Ask, 트랜잭션 전략
- `/tdd-workflow` - ATDD 워크플로우 (Cucumber 시나리오 우선)
- `/create-pull-request` - PR 생성 가이드
- `/code-review-excellence` - 코드 리뷰 가이드
- `/debugging-strategies` - 디버깅 전략 가이드
- `/skill-creator` - 새 스킬 생성 가이드

### Skill 사용 규칙

1. 아키텍처/패키지 구조 → `/architecture-patterns`
2. 도메인 모델 설계 → `/clean-code`
3. 기능 구현 → `/tdd-workflow`
4. PR 생성 → `/create-pull-request`
5. Skill 내용과 다른 구조 제안 금지

## Role

- 불확실한 요구사항은 추측하지 말고 질문
- 코드 변경 시 관련 문서 함께 수정
- Skill 변경/생성 시 `docs/trigger/skills-trigger.md` 업데이트

## Tech Stack

Java 17, Spring Boot 3.x, JPA, MySQL, Kafka, JUnit 5, Cucumber, Testcontainers

## Package Structure

> 상세: `/architecture-patterns` skill 참조

```
{domain}/
├── domain/
│   ├── model/           # Entity (Identity 보유)
│   ├── vo/              # Value Object (불변)
│   └── exception/       # 도메인 규칙 위반 예외
├── application/
│   ├── port/in/         # *UseCase 인터페이스
│   ├── port/out/        # *Port 인터페이스
│   ├── service/         # UseCase 구현체
│   ├── dto/             # Command, Result
│   └── exception/       # UseCase 실패 예외
├── adapter/
│   ├── in/web/          # Controller, Request/Response DTO
│   └── out/
│       ├── persistence/ # JPA Entity, Repository 구현
│       └── {external}/  # 외부 API Adapter
└── infrastructure/
    └── config/          # Spring Configuration
```

## Strict Rules

1. `domain/` 패키지는 Spring, JPA 의존 금지
2. `@Autowired` 필드 주입 금지 → 생성자 주입
3. Entity 직접 반환 금지 → `record` DTO 사용
4. 테스트 DB는 MySQL Testcontainers (H2 금지)
5. 외부 API는 WireMock 사용

## Exception Rules

> `RuntimeException`, `IllegalArgumentException` 직접 사용 금지

```
BusinessException (추상)
├── DomainException      # 도메인 규칙 위반
└── ApplicationException # UseCase 실패
```

에러 코드 패턴: `*_NOT_FOUND`(404), `ALREADY_*`(409), `NOT_*`(403), `INVALID_*`(400)

## Test

- Acceptance Test: Cucumber + Testcontainers
- PR 생성 전 `./gradlew test` 통과 필수

## Workflow

1. `/docs/spec/mvp-features.md` 확인
2. Cucumber 시나리오 작성 → 테스트 실패 확인 → 구현
3. `./gradlew test` 통과 확인
4. PR 생성
