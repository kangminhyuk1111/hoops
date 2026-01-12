# 알림 기능 테스트 가이드

## 1. 개요

알림 기능은 비동기 이벤트 기반으로 동작하므로, 테스트 시 특별한 접근 방법이 필요합니다.
이 문서에서는 Kafka를 통한 비동기 알림 전송을 테스트하는 방법을 설명합니다.

## 2. 알림 아키텍처

```
┌──────────────────┐      ┌─────────────┐      ┌────────────────────┐
│ ParticipationSvc │─────▶│    Kafka    │─────▶│ NotificationConsumer│
│ (Event Producer) │      │  (Message)  │      │                    │
└──────────────────┘      └─────────────┘      └─────────┬──────────┘
                                                         │
                                                         ▼
                                               ┌──────────────────┐
                                               │NotificationService│
                                               │                  │
                                               ├──────────────────┤
                                               │ 1. Save to DB    │
                                               │ 2. Send FCM/APNS │
                                               └──────────────────┘
```

### 2.1 이벤트 플로우

1. **이벤트 발생**: 사용자가 경기 참가 신청/취소 시 이벤트 발행
2. **Kafka 전송**: 이벤트가 Kafka 토픽으로 전송됨
3. **Consumer 처리**: NotificationConsumer가 메시지 수신
4. **알림 생성**: NotificationService가 알림 저장 및 푸시 전송

## 3. 테스트 전략

### 3.1 레이어별 테스트

| 레이어 | 테스트 대상 | 방법 |
|--------|-------------|------|
| Domain | Notification 도메인 모델 | 단위 테스트 (POJO) |
| Application | NotificationService | 통합 테스트 (DB 연동) |
| Infrastructure | Kafka Producer/Consumer | Testcontainers |
| E2E | 전체 플로우 | Cucumber 인수 테스트 |

### 3.2 Testcontainers를 활용한 Kafka 테스트

Kafka 메시지 발행 및 소비를 실제 환경과 동일하게 테스트합니다.

```java
@Testcontainers
@SpringBootTest
class NotificationKafkaIntegrationTest {

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    ).withReuse(true);

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void should_CreateNotification_When_ParticipationEventReceived() {
        // Given
        ParticipationCreatedEvent event = new ParticipationCreatedEvent(
                1L,  // participationId
                1L,  // matchId
                1L,  // userId
                "테스트 경기"
        );

        // When
        kafkaTemplate.send("participation-events", event);

        // Then
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    List<Notification> notifications =
                            notificationRepository.findByUserId(1L);
                    assertThat(notifications).isNotEmpty();
                    assertThat(notifications.get(0).getType())
                            .isEqualTo(NotificationType.PARTICIPATION_CREATED);
                });
    }
}
```

### 3.3 비동기 테스트 패턴

Kafka 메시지 처리는 비동기이므로 `Awaitility` 라이브러리를 활용합니다.

```java
// build.gradle
testImplementation 'org.awaitility:awaitility:4.2.0'

// 테스트 코드
import static org.awaitility.Awaitility.await;

await()
    .atMost(Duration.ofSeconds(10))
    .pollInterval(Duration.ofMillis(500))
    .untilAsserted(() -> {
        // 비동기 처리 결과 검증
    });
```

## 4. 푸시 알림 (FCM) 테스트

실제 FCM 서버 대신 Mock을 사용하여 테스트합니다.

### 4.1 WireMock을 활용한 FCM Mock

```java
@Testcontainers
@SpringBootTest
class FcmNotificationTest {

    @Container
    static final WireMockContainer WIREMOCK = new WireMockContainer(
            "wiremock/wiremock:2.35.0"
    );

    @DynamicPropertySource
    static void fcmProperties(DynamicPropertyRegistry registry) {
        registry.add("fcm.api.url", () -> WIREMOCK.getBaseUrl());
    }

    @Test
    void should_SendPushNotification_When_NotificationCreated() {
        // Given: FCM 성공 응답 스텁
        WIREMOCK.stubFor(post(urlPathEqualTo("/v1/projects/test/messages:send"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"name\": \"projects/test/messages/123\"}")));

        // When
        notificationService.sendPush(notification);

        // Then
        WIREMOCK.verify(postRequestedFor(urlPathEqualTo("/v1/projects/test/messages:send"))
                .withRequestBody(containsString("\"title\":\"참가 알림\"")));
    }
}
```

### 4.2 FCM Sender Mock 패턴

프로덕션 코드와 테스트 코드를 분리하여 FCM 전송을 제어합니다.

```java
// 인터페이스 정의
public interface PushNotificationSender {
    void send(Notification notification);
}

// 프로덕션 구현
@Profile("!test")
@Component
public class FcmNotificationSender implements PushNotificationSender {
    // 실제 FCM 전송 로직
}

// 테스트용 구현
@Profile("test")
@Component
public class MockNotificationSender implements PushNotificationSender {
    private final List<Notification> sentNotifications = new ArrayList<>();

    @Override
    public void send(Notification notification) {
        sentNotifications.add(notification);
    }

    public List<Notification> getSentNotifications() {
        return sentNotifications;
    }

    public void clear() {
        sentNotifications.clear();
    }
}
```

## 5. Cucumber 인수 테스트

전체 알림 플로우를 BDD 스타일로 테스트합니다.

### 5.1 시나리오 예시

```gherkin
# language: ko
기능: 알림 전송

  시나리오: 경기 참가 시 알림 전송
    먼저 사용자 "테스터"가 로그인되어 있다
    그리고 경기 "주말 농구"가 존재한다
    만일 해당 경기에 참가 신청한다
    그러면 응답 코드는 200 이다
    그리고 "경기에 참가 신청되었습니다" 알림이 생성된다
    그리고 알림이 사용자에게 전송된다
```

### 5.2 Step 정의

```java
@그리고("{string} 알림이 생성된다")
public void 알림이_생성된다(String expectedMessage) {
    await().atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                List<Notification> notifications =
                        notificationRepository.findByUserId(testUser.getId());
                assertThat(notifications)
                        .extracting(Notification::getMessage)
                        .anyMatch(msg -> msg.contains(expectedMessage));
            });
}

@그리고("알림이 사용자에게 전송된다")
public void 알림이_사용자에게_전송된다() {
    // MockNotificationSender를 통해 전송 여부 확인
    assertThat(mockNotificationSender.getSentNotifications())
            .isNotEmpty();
}
```

## 6. 로컬 개발 환경에서 테스트

### 6.1 Docker Compose로 Kafka 실행

```yaml
# docker-compose.yml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

### 6.2 수동 테스트 절차

1. Docker Compose로 Kafka 실행
   ```bash
   docker-compose up -d
   ```

2. 애플리케이션 실행
   ```bash
   ./gradlew bootRun
   ```

3. 경기 참가 API 호출
   ```bash
   curl -X POST http://localhost:8080/api/matches/1/participations \
        -H "Authorization: Bearer {token}"
   ```

4. 알림 목록 확인
   ```bash
   curl http://localhost:8080/api/notifications \
        -H "Authorization: Bearer {token}"
   ```

5. Kafka 메시지 확인 (선택사항)
   ```bash
   docker exec -it kafka kafka-console-consumer \
        --bootstrap-server localhost:9092 \
        --topic participation-events \
        --from-beginning
   ```

## 7. 디버깅 팁

### 7.1 Kafka Consumer 로그 활성화

```yaml
# application-test.yml
logging:
  level:
    org.springframework.kafka: DEBUG
    org.apache.kafka: INFO
```

### 7.2 테스트 실패 시 체크리스트

1. **Kafka 컨테이너 실행 확인**: Testcontainers 로그 확인
2. **토픽 생성 여부**: auto-create 설정 또는 수동 생성
3. **Serializer/Deserializer**: 이벤트 직렬화 설정 확인
4. **Consumer Group ID**: 테스트별 고유 그룹 ID 사용
5. **비동기 타이밍**: Awaitility 타임아웃 조정

### 7.3 일반적인 문제 해결

| 문제 | 원인 | 해결 |
|------|------|------|
| Consumer가 메시지를 받지 못함 | auto.offset.reset 설정 | `earliest`로 설정 |
| 직렬화 오류 | 타입 불일치 | trusted packages 설정 |
| 테스트 타임아웃 | 비동기 처리 지연 | await 타임아웃 증가 |

## 8. 참고 자료

- [Spring Kafka 공식 문서](https://docs.spring.io/spring-kafka/reference/)
- [Testcontainers Kafka 모듈](https://www.testcontainers.org/modules/kafka/)
- [Awaitility 사용법](https://github.com/awaitility/awaitility)
