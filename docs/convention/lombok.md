# Lombok 사용 가이드

## 개요

Lombok을 적극 사용하되, 아래 주의사항을 반드시 숙지한다.

---

## 권장 어노테이션

| 어노테이션 | 용도 | 비고 |
|-----------|------|------|
| `@Getter` | Getter 생성 | 필요한 필드에만 적용 권장 |
| `@RequiredArgsConstructor` | final 필드 생성자 | DI에 활용 |
| `@Builder` | 빌더 패턴 | 메서드 레벨 권장 |
| `@Slf4j` | 로거 주입 | |
| `@ToString` | toString 생성 | 연관관계 필드 제외 필수 |

---

## 주의사항

### 1. @Setter 사용 금지

```java
// X - 사용 금지
@Setter
public class User {
    private String name;
}

// O - 명시적 메서드 사용
public class User {
    private String name;

    public void changeName(String name) {
        this.name = name;
    }
}
```

**이유**: 무분별한 상태 변경을 허용하여 객체 불변성을 해침.

### 2. @EqualsAndHashCode 주의

JPA Entity에서는 반드시 `of`로 필드를 명시한다.

```java
// X - 순환 참조, 지연 로딩 문제
@EqualsAndHashCode
public class Match { }

// O - ID 필드만 사용
@EqualsAndHashCode(of = "id")
public class Match {
    private Long id;
}
```

### 3. @Data 사용 금지

`@Data`는 `@Setter`, `@EqualsAndHashCode`를 포함하므로 사용하지 않는다.

```java
// X - 사용 금지
@Data
public class User { }

// O - 필요한 것만 명시
@Getter
@RequiredArgsConstructor
public class User { }
```

### 4. @Builder 사용 패턴

#### 도메인 클래스 (Domain Model)

도메인 클래스에서는 **클래스 레벨 @Builder + @AllArgsConstructor** 조합을 사용한다.

```java
// O - 도메인 클래스 권장 패턴
@Getter
@Builder
@AllArgsConstructor
public class User {
    private final Long id;
    private final String email;
    private final String nickname;
}
```

**이유**:
- `@Builder`만 사용하면 package-private 생성자가 생성되어 Mapper에서 접근 불가
- `@AllArgsConstructor`로 public 생성자를 유지하면서 Builder 패턴도 활용 가능
- 테스트 코드에서 Builder로 가독성 높은 테스트 데이터 생성 가능

```java
// 테스트 코드에서의 활용
User user = User.builder()
        .email("test@example.com")
        .nickname("테스트유저")
        .rating(BigDecimal.valueOf(3.0))
        .build();
```

#### 일반 클래스

도메인 외 클래스에서는 메서드 레벨 Builder를 권장한다.

```java
// O - 메서드 레벨 Builder
public class SomeClass {
    private String name;

    @Builder
    private SomeClass(String name) {
        this.name = name;
    }
}
```

### 5. @ToString 연관관계 제외

JPA Entity에서 연관관계 필드는 반드시 제외한다.

```java
// X - 순환 참조, N+1 문제
@ToString
public class Match {
    private List<Participation> participations;
}

// O - 연관관계 제외
@ToString(exclude = "participations")
public class Match {
    private List<Participation> participations;
}
```

### 6. @AllArgsConstructor 단독 사용 지양

`@AllArgsConstructor`를 단독으로 사용하면 필드 순서 변경 시 버그 발생 가능.

```java
// X - 단독 사용 지양
@AllArgsConstructor
public class User {
    private String email;
    private String name;  // 필드 순서 변경 시 기존 코드에서 버그 발생
}

// O - @Builder와 함께 사용 (도메인 클래스)
@Getter
@Builder
@AllArgsConstructor
public class User {
    private final String email;
    private final String name;
}

// O - DI용으로는 @RequiredArgsConstructor 사용
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
}
```

---

## DTO에서의 사용

DTO는 Java 17 `record`를 우선 사용한다. Lombok이 필요한 경우에만 사용.

```java
// 권장 - record 사용
public record UserResponse(Long id, String name) { }

// Lombok 필요 시
@Getter
@RequiredArgsConstructor
public class UserResponse {
    private final Long id;
    private final String name;
}
```

---

## 참고

- [Lombok 사용법과 주의사항](https://hyoj.github.io/blog/java/basic/lombok/)
- [Lombok 사용 시 유의사항](https://eottabom.github.io/post/lombok-using-points/)
- [Why You Should Stop Using Lombok in 2025](https://medium.com/@cleanCompile/why-you-should-stop-using-lombok-in-2025-5fe3cc44b7c9)
