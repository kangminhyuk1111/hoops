# Security 규칙

## 공개 엔드포인트 추가 시

새로운 공개 API를 추가할 때 반드시 `SecurityConfig.java`의 `permitAll()`에 해당 경로를 추가한다.

```java
.authorizeHttpRequests(authorize -> authorize
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/health").permitAll()
    .requestMatchers("/actuator/**").permitAll()
    // 새로운 공개 API는 여기에 추가
    .anyRequest().authenticated()
)
```

## 현재 공개 엔드포인트

| 경로 | 메서드 |
|------|--------|
| `/api/auth/**` | ALL |
| `/api/health` | GET |
| `/actuator/**` | GET |
| `/api/matches/**` | GET |
| `/api/locations/**` | GET |
