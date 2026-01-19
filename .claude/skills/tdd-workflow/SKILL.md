---
name: tdd-workflow
description: Acceptance Test-Driven Development workflow for Java/Spring. Prioritizes Cucumber scenarios with 90%+ scenario coverage.
---

# Acceptance Test-Driven Development (ATDD)

## Philosophy

Acceptance tests are the primary testing strategy. They validate real user behavior and provide confidence during refactoring. Unit tests are optional, reserved for complex domain logic only.

## Test Strategy

| Layer | Tool | Coverage Target | When to Use |
|-------|------|-----------------|-------------|
| Acceptance | Cucumber + Testcontainers | 90%+ scenarios | Always |
| Unit | JUnit 5 | Complex logic only | Optional |

## ATDD Workflow

### Step 1: Write Cucumber Scenario

```gherkin
# language: ko
@auth @smoke
시나리오: 기존 회원이 카카오 로그인한다
  먼저 이미 가입된 회원이 있다
  만일 해당 회원이 카카오 인증을 완료한다
  그러면 응답 상태 코드는 200 이다
  그리고 응답에 액세스 토큰이 포함되어 있다
```

### Step 2: Run Test (Must Fail)

```bash
./gradlew test --tests "*AcceptanceTest"
# Expected: Test fails (step definitions missing or logic not implemented)
```

### Step 3: Implement Step Definitions

```java
@먼저("이미 가입된 회원이 있다")
public void existing_user_exists() {
    // Setup test data
}
```

### Step 4: Implement Production Code

Follow the architecture pattern:
1. Domain model
2. UseCase interface + implementation
3. Adapter (Controller, Repository)

### Step 5: Run Test (Must Pass)

```bash
./gradlew test --tests "*AcceptanceTest"
# Expected: All tests pass
```

### Step 6: Refactor

Improve code while keeping tests green.

## Scenario Tagging Strategy

| Tag | Purpose | Example |
|-----|---------|---------|
| `@smoke` | Critical path, run frequently | Login, core features |
| `@auth` | Authentication domain | Login, signup, token refresh |
| `@match` | Match domain | Create, cancel, status update |
| `@edge-case` | Boundary conditions | Invalid input, error scenarios |
| `@slow` | Long-running tests | Skip in local dev |

### Running Tagged Tests

```bash
# Smoke tests only (fast feedback)
./gradlew test -Dcucumber.filter.tags="@smoke"

# Specific domain
./gradlew test -Dcucumber.filter.tags="@auth"

# Exclude slow tests
./gradlew test -Dcucumber.filter.tags="not @slow"
```

## When to Write Unit Tests

Only write unit tests for:

1. **Complex calculations** - Rating algorithms, distance calculations
2. **Pure domain logic** - Value object validation, business rules
3. **Utility functions** - Date parsing, string manipulation

```java
// Example: Complex domain logic worth unit testing
class RatingCalculatorTest {
    @Test
    void calculates_weighted_average_rating() {
        var calculator = new RatingCalculator();
        var result = calculator.calculate(List.of(5, 4, 3), List.of(0.5, 0.3, 0.2));
        assertThat(result).isEqualTo(BigDecimal.valueOf(4.2));
    }
}
```

## Step Definition Best Practices

### Reuse Existing Steps

Before creating new steps, check existing ones:

```bash
# Search for existing step definitions
grep -r "@먼저\|@만일\|@그러면\|@그리고" src/test/java/com/hoops/acceptance/steps/
```

### Use SharedTestContext

Share state between step definitions:

```java
public class MyStepDefs {
    private final SharedTestContext context;

    public MyStepDefs(SharedTestContext context) {
        this.context = context;
    }

    @먼저("사용자가 로그인되어 있다")
    public void user_is_logged_in() {
        String token = context.getAccessToken();
        // Use token
    }
}
```

### Avoid Time-Dependent Tests

```java
// BAD: Breaks near midnight
LocalTime endTime = LocalTime.now().plusHours(1);

// GOOD: Fixed safe values
LocalTime endTime = LocalTime.of(23, 59);
```

## Test Data Management

### Use Unique Identifiers

```java
String email = "test" + System.currentTimeMillis() + "@example.com";
String kakaoId = "kakao-" + UUID.randomUUID().toString().substring(0, 8);
```

### Clean State Per Scenario

Cucumber's `@ScenarioScope` ensures fresh context per scenario.

## CI/CD Integration

```yaml
# GitHub Actions
- name: Run Smoke Tests
  run: ./gradlew test -Dcucumber.filter.tags="@smoke"

- name: Run All Tests
  run: ./gradlew test
```

## Coverage Measurement

Measure scenario coverage, not line coverage:

```bash
# Count total scenarios
grep -c "시나리오:" src/test/resources/features/*.feature

# Count scenarios per domain
grep -l "시나리오:" src/test/resources/features/*.feature | xargs -I {} sh -c 'echo {} && grep -c "시나리오:" {}'
```

## Feature File Organization

```
src/test/resources/features/
├── auth/
│   ├── login.feature
│   ├── signup.feature
│   └── token-refresh.feature
├── match/
│   ├── create-match.feature
│   ├── cancel-match.feature
│   └── match-status-scheduler.feature
└── participation/
    ├── join-match.feature
    └── cancel-participation.feature
```

## Checklist

Before PR:

- [ ] All new features have Cucumber scenarios
- [ ] Scenarios cover happy path and edge cases
- [ ] Step definitions reuse existing steps where possible
- [ ] Tests pass locally: `./gradlew test`
- [ ] No time-dependent test logic
- [ ] Appropriate tags applied (@smoke, @domain)
