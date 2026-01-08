```
src/test/
├── java/com/hoops/acceptance/
│   ├── AcceptanceTest.java              # Cucumber 테스트 러너
│   ├── config/
│   │   └── CucumberSpringConfiguration.java  # Spring + Testcontainers 설정
│   ├── adapter/
│   │   ├── TestAdapter.java            # 테스트용 HTTP 클라이언트 인터페이스
│   │   ├── RestTestAdapter.java        # TestRestTemplate 구현체
│   │   └── TestResponse.java           # 응답 검증용 record
│   └── steps/
│       └── HealthCheckStepDefs.java    # 헬스 체크 Step Definitions
└── resources/
    ├── features/
    │   └── health-check.feature        # Gherkin 시나리오 (한글)
    └── application-test.yml            # 테스트 프로파일 설정
```