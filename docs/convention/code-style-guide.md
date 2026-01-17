# Code Style Guide

> 이 문서는 hoops 프로젝트의 코드 작성 원칙입니다.
> 모든 코드는 이 원칙을 따라 작성되어야 합니다.

---

## 핵심 원칙

1. 메서드는 10줄을 넘지 않는다
2. Private 메서드를 쓰지 않는다 - 객체에게 위임한다
3. 코드는 영어 문장처럼 읽혀야 한다
4. 하나의 메서드는 하나의 일만 한다

---

## 1. Service 레이어 작성법

### 원칙
- Service 메서드는 **오케스트레이션만** 담당한다
- 로직을 직접 구현하지 않고, 객체에게 **위임**한다
- 메서드를 읽으면 **무슨 일이 일어나는지 한눈에** 파악된다

### Bad Example
```java
@Service
public class MatchCreator {

    public Match createMatch(CreateMatchCommand command) {
        // 호스트 검증
        User host = userRepository.findById(command.hostId())
                .orElseThrow(() -> new UserNotFoundException(command.hostId()));

        // 위치 검증
        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new LocationNotFoundException(command.locationId()));

        // 시간 검증
        if (command.startTime().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new InvalidStartTimeException();
        }

        // 중복 호스팅 검증
        List<Match> existingMatches = matchRepository.findByHostId(command.hostId());
        for (Match match : existingMatches) {
            if (match.getStatus() == MatchStatus.PENDING || match.getStatus() == MatchStatus.IN_PROGRESS) {
                LocalDateTime existingStart = LocalDateTime.of(match.getMatchDate(), match.getStartTime());
                LocalDateTime existingEnd = LocalDateTime.of(match.getMatchDate(), match.getEndTime());
                LocalDateTime newStart = LocalDateTime.of(command.matchDate(), command.startTime());
                LocalDateTime newEnd = LocalDateTime.of(command.matchDate(), command.endTime());

                if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                    throw new OverlappingHostingException();
                }
            }
        }

        // Match 생성
        Match match = new Match(
                null,
                null,
                host.getId(),
                host.getNickname(),
                command.title(),
                command.description(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAddress(),
                command.matchDate(),
                command.startTime(),
                command.endTime(),
                command.maxParticipants(),
                1,
                MatchStatus.PENDING,
                null
        );

        return matchRepository.save(match);
    }
}
```

**문제점:**
- 40줄이 넘는 메서드
- 검증 로직이 Service에 직접 구현됨
- Private 메서드로 분리해도 여전히 Service가 로직을 알고 있음
- 읽기 어려움

### Good Example
```java
@Service
@RequiredArgsConstructor
public class MatchCreator {

    private final MatchRepository matchRepository;
    private final HostInfoProvider hostInfoProvider;
    private final LocationInfoProvider locationInfoProvider;
    private final MatchPolicyValidator policyValidator;

    public Match createMatch(CreateMatchCommand command) {
        HostInfo host = hostInfoProvider.getHostInfo(command.hostId());
        LocationInfo location = locationInfoProvider.getLocationInfo(command.locationId());

        policyValidator.validateCreation(command, host);

        Match match = Match.create(command, host, location);
        return matchRepository.save(match);
    }
}
```

**개선점:**
- 6줄로 축소
- 각 객체가 자신의 책임을 수행
- 영어처럼 읽힘: "호스트 정보를 가져오고, 위치 정보를 가져오고, 정책을 검증하고, 경기를 생성하고, 저장한다"

---

## 2. Validator 작성법

### 원칙
- 검증 로직은 **Validator 객체**에게 위임한다
- Validator 내부에서도 **private 메서드를 쓰지 않는다**
- 각 검증 규칙은 **개별 Validator**로 분리하거나, **도메인 객체에게 위임**한다

### Bad Example
```java
@Component
public class MatchPolicyValidator {

    public void validateCreation(CreateMatchCommand command, HostInfo host) {
        validateStartTime(command);
        validateNoOverlappingHosting(command, host);
        validateMaxParticipants(command);
    }

    private void validateStartTime(CreateMatchCommand command) {
        LocalDateTime startDateTime = LocalDateTime.of(command.matchDate(), command.startTime());
        if (startDateTime.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new InvalidStartTimeException();
        }
    }

    private void validateNoOverlappingHosting(CreateMatchCommand command, HostInfo host) {
        // 복잡한 중복 검증 로직...
    }

    private void validateMaxParticipants(CreateMatchCommand command) {
        if (command.maxParticipants() < 4 || command.maxParticipants() > 20) {
            throw new InvalidMaxParticipantsException();
        }
    }
}
```

**문제점:**
- Private 메서드 사용
- Validator가 모든 검증 로직을 직접 구현

### Good Example - 방법 1: 개별 Validator 분리
```java
@Component
@RequiredArgsConstructor
public class MatchPolicyValidator {

    private final StartTimeValidator startTimeValidator;
    private final OverlappingHostingValidator overlappingValidator;
    private final MaxParticipantsValidator maxParticipantsValidator;

    public void validateCreation(CreateMatchCommand command, HostInfo host) {
        startTimeValidator.validate(command);
        overlappingValidator.validate(command, host);
        maxParticipantsValidator.validate(command);
    }
}

@Component
public class StartTimeValidator {

    public void validate(CreateMatchCommand command) {
        LocalDateTime startDateTime = command.getStartDateTime();
        LocalDateTime minimumStartTime = LocalDateTime.now().plusHours(1);

        if (startDateTime.isBefore(minimumStartTime)) {
            throw new InvalidStartTimeException();
        }
    }
}
```

### Good Example - 방법 2: 도메인 객체에게 위임
```java
@Component
@RequiredArgsConstructor
public class MatchPolicyValidator {

    private final MatchRepository matchRepository;

    public void validateCreation(CreateMatchCommand command, HostInfo host) {
        command.validateStartTime();
        command.validateMaxParticipants();

        List<Match> hostMatches = matchRepository.findActiveByHostId(host.id());
        Match.validateNoOverlapping(hostMatches, command.getTimeRange());
    }
}

// Command에 검증 로직 포함
public record CreateMatchCommand(
    Long hostId,
    Long locationId,
    String title,
    LocalDate matchDate,
    LocalTime startTime,
    LocalTime endTime,
    Integer maxParticipants
) {
    public void validateStartTime() {
        if (getStartDateTime().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new InvalidStartTimeException();
        }
    }

    public void validateMaxParticipants() {
        if (maxParticipants < 4 || maxParticipants > 20) {
            throw new InvalidMaxParticipantsException();
        }
    }

    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(matchDate, startTime);
    }

    public TimeRange getTimeRange() {
        return new TimeRange(matchDate, startTime, endTime);
    }
}
```

---

## 3. Domain 객체 작성법

### 원칙
- 도메인 객체는 **자신의 상태를 스스로 검증**한다
- 도메인 객체는 **자신의 행동을 스스로 수행**한다
- **빈약한 도메인(Anemic Domain)**을 피한다

### Bad Example
```java
// 빈약한 도메인 - getter/setter만 있음
@Getter
@Builder
public class Participation {
    private Long id;
    private Long matchId;
    private Long userId;
    private ParticipationStatus status;
}

// Service에서 모든 로직 처리
public class ParticipationService {

    public void cancel(Long participationId) {
        Participation participation = repository.findById(participationId);

        // Service가 도메인 로직을 알고 있음
        if (participation.getStatus() != ParticipationStatus.PENDING
            && participation.getStatus() != ParticipationStatus.CONFIRMED) {
            throw new InvalidStatusException();
        }

        participation.setStatus(ParticipationStatus.CANCELLED);
        repository.save(participation);
    }
}
```

### Good Example
```java
// 풍부한 도메인 - 자신의 행동을 알고 있음
@Getter
@Builder
public class Participation {
    private final Long id;
    private final Long matchId;
    private final Long userId;
    private final ParticipationStatus status;

    public static Participation createPending(Long matchId, Long userId) {
        return Participation.builder()
                .matchId(matchId)
                .userId(userId)
                .status(ParticipationStatus.PENDING)
                .build();
    }

    public Participation cancel() {
        validateCancellable();
        return withStatus(ParticipationStatus.CANCELLED);
    }

    public Participation approve() {
        validateApprovable();
        return withStatus(ParticipationStatus.CONFIRMED);
    }

    public boolean canCancel() {
        return status == ParticipationStatus.PENDING
            || status == ParticipationStatus.CONFIRMED;
    }

    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
    }

    private void validateCancellable() {
        if (!canCancel()) {
            throw new InvalidParticipationStatusException(id, status.name());
        }
    }

    private void validateApprovable() {
        if (status != ParticipationStatus.PENDING) {
            throw new InvalidParticipationStatusException(id, status.name());
        }
    }

    private Participation withStatus(ParticipationStatus newStatus) {
        return Participation.builder()
                .id(this.id)
                .matchId(this.matchId)
                .userId(this.userId)
                .status(newStatus)
                .build();
    }
}

// Service는 오케스트레이션만
public class ParticipationService {

    public void cancel(CancelCommand command) {
        Participation participation = repository.findById(command.participationId());

        Participation cancelled = participation.cancel();
        repository.save(cancelled);
    }
}
```

**참고:** 도메인 객체 내부의 private 메서드는 허용됩니다.
- Service의 private 메서드 -> 객체로 추출
- Domain의 private 메서드 -> 자기 자신의 내부 구현 (허용)

---

## 4. 메서드 네이밍

### 원칙
- 메서드명만 읽어도 **무슨 일을 하는지** 알 수 있어야 한다
- **약어를 사용하지 않는다**
- **동사로 시작**한다

### Bad Example
```java
void proc(Long id);
boolean chk(Match m);
Match getM(Long id);
void validateAndProcessAndSave(Command cmd);
```

### Good Example
```java
void cancelParticipation(Long participationId);
boolean canParticipate(Match match);
Match findMatchById(Long matchId);
void approveParticipation(ApproveCommand command);
```

### 동사 선택 가이드

| 의도 | 동사 | 예시 |
|------|------|------|
| 조회 (단건) | find, get | `findMatchById()`, `getHostInfo()` |
| 조회 (목록) | findAll, list | `findAllByStatus()`, `listParticipants()` |
| 생성 | create | `createMatch()`, `createParticipation()` |
| 수정 | update | `updateProfile()`, `updateMatchStatus()` |
| 삭제 | delete, remove | `deleteMatch()`, `removeParticipant()` |
| 검증 | validate | `validateCreation()`, `validatePermission()` |
| 상태 확인 | is, can, has | `isOwner()`, `canCancel()`, `hasStarted()` |
| 변환 | to, from | `toResponse()`, `fromEntity()` |

---

## 5. 변수 네이밍

### 원칙
- **의미가 명확한 이름**을 사용한다
- **약어를 사용하지 않는다**
- **타입을 이름에 포함하지 않는다**

### Bad Example
```java
Match m;
User u;
List<Match> matchList;
String str;
int cnt;
double lat, lng;
LocalDateTime dt;
```

### Good Example
```java
Match match;
User host;
List<Match> matches;
String nickname;
int participantCount;
double latitude, longitude;
LocalDateTime startDateTime;
```

---

## 6. 조건문 작성법

### 원칙
- 조건문은 **메서드로 추출**하여 의도를 명확히 한다
- **부정 조건보다 긍정 조건**을 선호한다
- **Early Return**을 활용한다

### Bad Example
```java
public void process(Match match, User user) {
    if (match != null && match.getStatus() == MatchStatus.PENDING
        && user != null && match.getHostId().equals(user.getId())
        && !match.getMatchDate().isBefore(LocalDate.now())) {
        // 처리 로직
    } else {
        throw new InvalidStateException();
    }
}
```

### Good Example
```java
public void process(Match match, User user) {
    validateProcessable(match, user);
    // 처리 로직
}

private void validateProcessable(Match match, User user) {
    if (!match.isPending()) {
        throw new InvalidMatchStatusException();
    }
    if (!match.isHostedBy(user)) {
        throw new NotHostException();
    }
    if (match.hasAlreadyPassed()) {
        throw new MatchAlreadyPassedException();
    }
}

// Match 도메인에 의미있는 메서드 추가
public class Match {
    public boolean isPending() {
        return status == MatchStatus.PENDING;
    }

    public boolean isHostedBy(User user) {
        return hostId.equals(user.getId());
    }

    public boolean hasAlreadyPassed() {
        return matchDate.isBefore(LocalDate.now());
    }
}
```

---

## 7. 예외 처리

### 원칙
- `RuntimeException`, `IllegalStateException`, `IllegalArgumentException` **직접 사용 금지**
- 비즈니스 예외는 **도메인별 Exception 클래스**를 생성한다
- 예외 메시지는 **문제와 해결 방법**을 포함한다

### Bad Example
```java
if (user == null) {
    throw new RuntimeException("User not found");
}

if (status != Status.PENDING) {
    throw new IllegalStateException("Invalid status");
}
```

### Good Example
```java
// 예외 클래스 정의
public class UserNotFoundException extends DomainException {
    private static final String ERROR_CODE = "USER_NOT_FOUND";

    public UserNotFoundException(Long userId) {
        super(ERROR_CODE, String.format("사용자를 찾을 수 없습니다. userId=%d", userId));
    }
}

public class InvalidParticipationStatusException extends DomainException {
    private static final String ERROR_CODE = "INVALID_PARTICIPATION_STATUS";

    public InvalidParticipationStatusException(Long participationId, String currentStatus) {
        super(ERROR_CODE,
            String.format("참가 상태가 올바르지 않습니다. participationId=%d, currentStatus=%s",
                participationId, currentStatus));
    }
}

// 사용
User user = userRepository.findById(userId)
    .orElseThrow(() -> new UserNotFoundException(userId));
```

---

## 8. 코드 구조 요약

```
Service (오케스트레이션)
    |
    +-- Validator (검증 위임)
    |       +-- 개별 Validator 또는 Domain에 위임
    |
    +-- Domain (비즈니스 로직)
    |       +-- 자신의 상태와 행동을 알고 있음
    |
    +-- Repository (저장)
```

### Service 메서드 템플릿
```java
public Result doSomething(Command command) {
    // 1. 필요한 데이터 조회 (1-2줄)
    Entity entity = repository.findById(command.id());

    // 2. 검증 (1줄)
    validator.validate(command, entity);

    // 3. 비즈니스 로직 실행 - 도메인에게 위임 (1줄)
    Entity result = entity.doAction(command);

    // 4. 저장 (1줄)
    return repository.save(result);
}
```

---

## 체크리스트

코드 작성 후 다음을 확인하세요:

- [ ] 메서드가 10줄을 넘지 않는가?
- [ ] Service에 private 메서드가 없는가?
- [ ] 메서드명만 읽어도 무슨 일을 하는지 알 수 있는가?
- [ ] 조건문이 의미 있는 메서드로 추출되어 있는가?
- [ ] 도메인 객체가 자신의 행동을 알고 있는가?
- [ ] 커스텀 Exception을 사용하고 있는가?
