# 코드 규칙 위반 분석 노트

> 분석일: 2026-01-15
> 분석 대상: hoops 백엔드 프로젝트
> 기준 문서: CLAUDE.md, /docs/**/*.md

---

## 위반 요약

| 규칙 | 상태 | 위반 건수 |
|------|------|----------|
| No Mocking | ✅ 준수 | 0 |
| Pure Domain | ⚠️ 위반 | 1 |
| Constructor Injection | ✅ 준수 | 0 |
| DTO vs Entity | ✅ 준수 | 0 |
| Exception 규칙 | ⚠️ 위반 | 6 |
| Cross-Context 통신 | ✅ 준수 | 0 |
| Hexagonal Architecture | ✅ 준수 | 0 |
| 패키지 구조 일관성 | ⚠️ 불일치 | - |

**전체 준수율**: 75% (8개 규칙 중 2개 위반)

---

## 1. [심각] Pure Domain 위반 - BaseTimeEntity

### 위치
`/backend/src/main/java/com/hoops/common/domain/BaseTimeEntity.java`

### 문제
도메인 패키지에 JPA/Spring 의존성이 존재함

```java
// 현재 코드 (위반)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### 위반 규칙
> **Pure Domain**: `domain/` 패키지 내 클래스는 외부 라이브러리(Spring, JPA, JSON 등) 의존성이 전혀 없는 **Pure Java(POJO)**여야 한다.

### 영향
- AuthAccountEntity, MatchEntity, UserEntity 등이 이를 상속
- 도메인 계층이 인프라에 의존하는 구조

### 권장 수정
```
common/domain/BaseTimeEntity.java (삭제)
    ↓
common/infrastructure/persistence/BaseTimeEntity.java (이동)
```

---

## 2. [중간] Exception 규칙 위반 - IllegalArgumentException/IllegalStateException 직접 사용

### 위반 위치 (6건)

| 파일 | 라인 | 위반 코드 |
|------|------|----------|
| `user/.../UpdateUserProfileCommand.java` | - | `throw new IllegalArgumentException("userId는 필수입니다")` |
| `user/.../UpdateUserProfileCommand.java` | - | `throw new IllegalArgumentException("requesterId는 필수입니다")` |
| `common/security/JwtTokenProviderImpl.java` | - | `throw new IllegalArgumentException("Invalid userId in token")` |
| `common/security/JwtTokenProviderImpl.java` | - | `throw new IllegalArgumentException("Invalid userId in claims")` |
| `common/security/SecurityUtils.java` | - | `throw new IllegalStateException("인증되지 않은 사용자입니다")` |
| `common/security/SecurityUtils.java` | - | `throw new IllegalStateException("사용자 ID를 가져올 수 없습니다")` |

### 위반 규칙
> **절대 금지**: `RuntimeException`, `IllegalArgumentException`, `IllegalStateException` 직접 사용 금지

### 권장 수정

**UpdateUserProfileCommand.java**
```java
// Before (위반)
throw new IllegalArgumentException("userId는 필수입니다");

// After (준수)
throw new InvalidCommandException("USER_ID_REQUIRED", "userId는 필수입니다");
```

**SecurityUtils.java**
```java
// Before (위반)
throw new IllegalStateException("인증되지 않은 사용자입니다");

// After (준수)
throw new UnauthorizedException("UNAUTHENTICATED_USER", "인증되지 않은 사용자입니다");
```

---

## 3. [낮음] 패키지 구조 불일치

### 현황

| 항목 | CLAUDE.md 규칙 | match 도메인 | 다른 도메인 |
|------|----------------|-------------|------------|
| JPA Entity | `infrastructure/persistence/entity/` | `adapter/out/` | `infrastructure/` |
| DTO | `adapter/in/web/dto/` | `adapter/dto/` | 혼재 |
| Mapper | `infrastructure/mapper/` | `adapter/out/mapper/` | `infrastructure/mapper/` |

### 영향
- 프로젝트 내 일관성 부족
- 신규 개발자/도메인 구현 시 혼란 가능

### 권장 조치
1. CLAUDE.md 규칙을 실제 구조에 맞게 업데이트 (현실 반영)
2. 또는 모든 도메인을 동일한 구조로 리팩토링

---

## 준수 항목 (참고)

### No Mocking - 완벽 준수
- MySQL Testcontainers 사용
- `CucumberSpringConfiguration.java`: `MySQLContainer<>("mysql:8.0")`
- `ShedLockIntegrationTest.java`: Testcontainers 사용

### Constructor Injection - 완벽 준수
- 31개 클래스에서 `@RequiredArgsConstructor` 사용
- `@Autowired` 필드 주입: 0건

### DTO vs Entity - 완벽 준수
- Java 17 `record` 타입 사용
- Controller에서 Entity 직접 반환 없음

### Cross-Context 통신 - 준수
- Port를 통한 간접 통신 구현
- `MatchInfoProvider`, `MatchParticipationPort` 등

---

## 우선순위별 수정 권장

1. **즉시 수정**: Exception 규칙 위반 (6건)
2. **단기 수정**: BaseTimeEntity 위치 이동
3. **장기 검토**: 패키지 구조 일관성 정리

---

*이 문서는 프로젝트 빌드 전 자동 분석으로 생성되었습니다.*
