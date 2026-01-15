# [Deep Dive] 분산 환경에서 스케줄러 중복 실행 문제 해결하기

> 작성일: 2026-01-14
> 작성자: 협업 Deep Dive (Human + AI)
> 프로젝트: hoops - 농구 경기 매칭 플랫폼

---

## Context: 비즈니스 상황

hoops 서비스는 경기 시작/종료 시간이 되면 자동으로 상태를 변경하는 **스케줄러**를 운영합니다.

```
PENDING/CONFIRMED → IN_PROGRESS (경기 시작 시간)
IN_PROGRESS → ENDED (경기 종료 시간)
```

현재는 **단일 서버**로 운영 중이지만, 서비스 성장에 따라 **다중 인스턴스로 확장**해야 할 상황입니다.

---

## Problem: 무엇이 문제인가?

### 현재 구현

```java
@Scheduled(fixedRate = 60000) // 1분마다 실행
public void updateMatchStatuses() {
    updateMatchStatusUseCase.startMatches();
    updateMatchStatusUseCase.endMatches();
}
```

### 단일 서버에서는 문제 없음

```
[Server A] ── 09:00:00 ── startMatches() ── Match #1, #2, #3 처리 ✅
```

### 다중 인스턴스에서 발생하는 문제

```
[Server A] ── 09:00:00 ── startMatches() ── Match #1 조회 ───┐
                                                              │ 동시 실행!
[Server B] ── 09:00:00 ── startMatches() ── Match #1 조회 ───┘
```

### 예상되는 피해

| 문제 | 영향 |
|------|------|
| 경기 상태 중복 변경 | 로그 중복, 불필요한 DB 트랜잭션 |
| 알림 중복 발송 | 사용자에게 같은 알림 2번 발송 |
| DB 부하 증가 | 쿼리 2배 실행 |

---

## Deep Dive: 협업 탐구 과정

### 질문 1: 현재 코드에 멱등성이 보장되는가?

#### 멱등성(Idempotency)이란?

> **같은 연산을 여러 번 수행해도 결과가 동일한 것**
>
> 수학적으로: `f(f(x)) = f(x)`

#### 현재 코드 분석

**조회 쿼리:**
```sql
SELECT m FROM MatchEntity m
WHERE m.status IN [PENDING, CONFIRMED, FULL]  -- 처리 대상 상태만 조회
AND (m.matchDate < :date OR (m.matchDate = :date AND m.startTime <= :time))
```

**상태 변경 로직:**
```java
public void startMatch() {
    if (canTransitionToInProgress()) {  // PENDING, CONFIRMED, FULL일 때만
        this.status = MatchStatus.IN_PROGRESS;
    }
}
```

#### 순차 실행 시 (멱등성 검증)

| 실행 | 조회 결과 | 처리 | 최종 상태 |
|------|----------|------|----------|
| 1회차 | Match #1 (PENDING) | IN_PROGRESS로 변경 | IN_PROGRESS |
| 2회차 | 없음 (이미 IN_PROGRESS) | 처리 없음 | IN_PROGRESS |
| N회차 | 없음 | 처리 없음 | IN_PROGRESS |

**결론: 현재 코드는 멱등하다** ✅

멱등성 보장 이유: 쿼리 조건이 "처리 대상 상태"만 조회하므로, 이미 처리된 경기는 재조회되지 않음.

#### 왜 멱등해야 하는가?

**1. 재시도 안전성**
```
스케줄러 실행 → 네트워크 오류 → 재시도
→ 멱등하면: 같은 결과
→ 멱등 아니면: 데이터 손상 가능
```

**2. 장애 복구**
```
5개 경기 처리 중 3번째에서 서버 죽음
→ 멱등하면: 재시작 시 남은 2개만 처리
→ 멱등 아니면: 1~3번 다시 처리 → 중복/오류
```

**3. 분산 환경 최소 안전장치**
```
스케줄러 락 없이 2개 인스턴스 동시 실행
→ 멱등하면: 최종 결과는 동일 (중복 실행은 발생)
→ 멱등 아니면: 데이터 불일치
```

#### 그러나: 멱등성 ≠ Race Condition 해결

**멱등성이 보장하는 것 (순차 실행):**
```
T1: Server A 실행 → Match #1 처리
T2: Server A 완료
T3: Server B 실행 → Match #1 이미 처리됨 → 스킵 ✅
```

**멱등성이 보장하지 못하는 것 (동시 실행):**
```
시간    Server A                    Server B
────────────────────────────────────────────────────
T1      findMatchesToStart()
        → Match #1 (PENDING) 조회

T2                                  findMatchesToStart()
                                    → Match #1 (PENDING) 조회 ← 아직 PENDING!

T3      match.startMatch()
        → IN_PROGRESS로 변경

T4      save() → DB 저장            match.startMatch()
                                    → IN_PROGRESS로 변경

T5                                  save() → DB 저장 (중복!)
```

#### 핵심 정리

| 속성 | 현재 상태 | 효과 |
|------|----------|------|
| **멱등성** | ✅ 보장됨 | 순차 재실행 시 안전 |
| **Race Condition 방지** | ❌ 미보장 | 동시 실행 시 중복 발생 |
| **필요한 해결책** | 스케줄러 락 (ShedLock) | 동시 실행 자체를 방지 |

> **멱등성은 "안전망"이고, 스케줄러 락은 "예방책"이다.**

---

### 질문 2: Race Condition으로 인한 실제 피해는?

**피해 분석:**

| 피해 | 현재 상태 | 비고 |
|------|----------|------|
| 로그 중복 | ⚠️ 발생 | 같은 경기에 "시작 상태로 변경" 로그 2번 |
| DB 트랜잭션 낭비 | ⚠️ 발생 | 불필요한 UPDATE 쿼리 실행 |
| @Version 충돌 예외 | ✅ 무해 | 첫 번째가 성공했다는 의미, 예외 처리 불필요 |
| 알림 중복 발송 | 🔮 잠재적 | 현재는 없지만, 향후 추가 시 문제 |

**핵심 인사이트:**

> `@Version`으로 인한 `OptimisticLockingFailureException`은 "이미 다른 인스턴스에서 처리 완료"를 의미한다.
> 따라서 예외 자체는 문제가 아니며, **별도 예외 처리가 불필요**하다.

**그러나 여전히 문제인 이유:**
- 불필요한 DB 트랜잭션 발생 (리소스 낭비)
- 로그 분석 시 혼란 (같은 경기가 여러 번 처리된 것처럼 보임)
- 향후 알림/이벤트 발행 추가 시 중복 발생 위험

---

### 질문 2.5: Phantom Read 문제는 없는가?

#### Phantom Read란?

> 한 트랜잭션 내에서 **같은 쿼리를 두 번 실행**했을 때, 첫 번째에 없던 행이 두 번째에 나타나는 현상

```
트랜잭션 A                          트랜잭션 B
─────────────────────────────────────────────────────
SELECT * FROM matches
WHERE status = 'PENDING'
→ Match #1, #2 조회
                                    INSERT Match #3 (PENDING)
                                    COMMIT

SELECT * FROM matches
WHERE status = 'PENDING'
→ Match #1, #2, #3 조회  ← Phantom!
```

#### 현재 코드 분석

```java
@Transactional
public int startMatches() {
    // 1. 조회 (한 번만 실행)
    List<Match> matchesToStart = matchRepository.findMatchesToStart(...);

    // 2. 순회하며 처리
    for (Match match : matchesToStart) {
        match.startMatch();
        matchRepository.save(match);
    }
    return count;
}
```

**결론: 전통적인 Phantom Read 문제 없음** ✅

- 한 트랜잭션 내에서 **같은 쿼리를 두 번 실행하지 않음**
- 한 번 조회 → 결과 순회 → 종료

#### 검토한 시나리오들

**시나리오 1: 조회-처리 사이 새 경기 INSERT**

```
스케줄러 트랜잭션                    다른 트랜잭션
─────────────────────────────────────────────────────
findMatchesToStart()
→ Match #1, #2 조회
                                    INSERT Match #3 (시작 시간 지남)
                                    COMMIT
Match #1, #2 처리
COMMIT

→ Match #3은 다음 스케줄러 실행 시 처리됨 (1분 후)
```

**영향**: 없음 - 비즈니스적으로 문제 없음

**시나리오 2: 조회-처리 사이 상태 변경 (Lost Update 위험)**

```
스케줄러 트랜잭션                    호스트 트랜잭션
─────────────────────────────────────────────────────
findMatchesToStart()
→ Match #1 (PENDING, version=1) 조회
                                    Match #1 취소 요청
                                    → CANCELLED, version=2
                                    COMMIT
match.startMatch()
save() 시도 (version=1 기대)
→ OptimisticLockException! ✅
```

**@Version이 보호**: Lost Update 방지됨

#### 최종 분석 결과

| 문제 유형 | 현재 상태 | 비고 |
|----------|----------|------|
| **Phantom Read** | ⚪ 해당 없음 | 같은 쿼리 두 번 실행 안 함 |
| **Lost Update** | ✅ @Version으로 방지 | 낙관적 락 적용됨 |
| **중복 실행** | ⚠️ 스케줄러 락 필요 | Race Condition 문제 |

> **Phantom Read는 현재 코드 구조상 문제 없음.**
> **진짜 문제는 Race Condition이며, 이는 ShedLock으로 해결.**

---

### 질문 3: 해결 방안 비교

#### 용어 정리

| 용어 | 정의 | 예시 |
|------|------|------|
| **분산 락 (Distributed Lock)** | 여러 노드가 **공유 자원** 접근을 조율 | Redis Lock, ZooKeeper, etcd |
| **스케줄러 락 (Scheduler Lock)** | 여러 인스턴스에서 **스케줄러 중복 실행** 방지 | ShedLock |
| **DB 락** | 트랜잭션 내 **레코드 동시 접근** 제어 | SELECT FOR UPDATE, @Version |

#### 접근 방식 1: 락 기반 (Lock-based)

여러 인스턴스가 **동일한 작업을 경쟁**하고, 락을 획득한 인스턴스만 실행하는 방식.

```
[Instance A] ──┐
               ├── 락 획득 경쟁 ── 하나만 실행
[Instance B] ──┘
```

| 방안 | 유형 | 장점 | 단점 | 적합한 상황 |
|------|------|------|------|------------|
| **ShedLock** | 스케줄러 락 | 구현 간단, DB만 있으면 됨 | 단일 실행만 보장 | MySQL 있는 소규모 서비스 |
| **Redisson** | 분산 락 | 빠름, TTL 자동 관리 | Redis 인프라 필요 | Redis 이미 사용 중인 서비스 |
| **RedLock** | 분산 락 | 고가용성 (과반수 합의) | Redis 3대 이상 필요 | 미션 크리티컬 시스템 |
| **ZooKeeper** | 분산 락 | 강력한 일관성 보장 | 운영 복잡도 높음 | 대규모 분산 시스템 |
| **etcd** | 분산 락 | 경량, K8s 친화적 | 별도 클러스터 필요 | 클라우드 네이티브 환경 |
| **Quartz Cluster** | 스케줄러 락 | 스케줄러 전용 솔루션 | 설정 복잡 | 복잡한 스케줄링 요구 |

#### 접근 방식 2: 샤드키 기반 설계 (Work Partitioning)

작업을 **분할**하여 각 인스턴스가 자신의 담당 영역만 처리하는 방식.

```
[Instance A] ── Match ID % 2 == 0 ── Match #2, #4, #6 처리
[Instance B] ── Match ID % 2 == 1 ── Match #1, #3, #5 처리
```

**샤딩 전략 예시:**

| 전략 | 설명 | 장점 | 단점 |
|------|------|------|------|
| **ID 기반 샤딩** | `matchId % instanceCount` | 균등 분배, 단순 | 인스턴스 수 변경 시 재분배 |
| **지역 기반 샤딩** | 서울/부산/기타 담당 분리 | 지역 친화적 | 트래픽 불균형 가능 |
| **Consistent Hashing** | 해시 링 기반 분배 | 인스턴스 추가/제거 용이 | 구현 복잡 |

**샤드키 기반 구현 예시:**

```java
@Scheduled(fixedRate = 60000)
public void updateMatchStatuses() {
    int instanceId = getInstanceId();      // 0, 1, 2...
    int totalInstances = getTotalInstances();  // 3

    // 자신이 담당하는 경기만 처리
    List<Match> myMatches = matchRepository.findMatchesToStart(...)
        .stream()
        .filter(m -> m.getId() % totalInstances == instanceId)
        .toList();

    for (Match match : myMatches) {
        match.startMatch();
        matchRepository.save(match);
    }
}
```

#### 두 접근 방식 비교

| 항목 | 락 기반 (ShedLock 등) | 샤드키 기반 |
|------|----------------------|------------|
| **동작 원리** | 경쟁 후 단일 실행 | 작업 분할 후 병렬 실행 |
| **처리량** | 단일 인스턴스 성능 | 인스턴스 수에 비례 |
| **구현 복잡도** | 낮음 (라이브러리) | 중간 (샤딩 로직) |
| **장애 대응** | 다른 인스턴스 자동 대체 | 특정 샤드 작업 중단 |
| **적합한 규모** | 소~중규모 | 대규모 |

---

### 질문 4: 우리 서비스에 가장 적합한 방안은?

**선택: ShedLock (스케줄러 락)**

**선택 근거:**

1. **현재 인프라**: MySQL만 사용 중, Redis 없음
2. **데이터 규모**: 경기 수 수천 건 미만 예상
3. **처리 주기**: 1분 주기 스케줄러는 단일 인스턴스로 충분
4. **구현 비용**: 라이브러리 추가만으로 해결 가능
5. **운영 복잡도**: 추가 인프라 없이 기존 DB 활용

**샤드키 기반을 선택하지 않은 이유:**

- 현재 규모에서는 **오버엔지니어링**
- 인스턴스 수 변경 시 샤드 재분배 로직 필요
- 특정 인스턴스 장애 시 해당 샤드 작업 중단 문제

**향후 확장 전략:**

```
현재: ShedLock (단일 실행 보장)
  ↓ 경기 수 10만건 이상
중기: 샤드키 기반 설계 (병렬 처리)
  ↓ 글로벌 확장
장기: Kafka 기반 이벤트 드리븐 + 지역별 파티셔닝
```

---

## Solution: 최종 해결책

### ShedLock 구현

#### 1. 의존성 추가

```groovy
// build.gradle
implementation 'net.javacrumbs.shedlock:shedlock-spring:5.10.0'
implementation 'net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.10.0'
```

#### 2. 락 테이블 생성

```sql
-- schema.sql
CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
```

#### 3. ShedLock 설정

```java
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build()
        );
    }
}
```

#### 4. 스케줄러에 락 적용

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchStatusScheduler {

    private final UpdateMatchStatusUseCase updateMatchStatusUseCase;

    @Scheduled(fixedRate = 60000)
    @SchedulerLock(
        name = "updateMatchStatuses",
        lockAtLeastFor = "PT50S",   // 최소 50초 락 유지 (중복 실행 방지)
        lockAtMostFor = "PT5M"      // 최대 5분 (장애 시 자동 해제)
    )
    public void updateMatchStatuses() {
        log.debug("경기 상태 업데이트 스케줄러 실행");
        int startedCount = updateMatchStatusUseCase.startMatches();
        int endedCount = updateMatchStatusUseCase.endMatches();
        log.info("스케줄러 완료 - 시작: {}건, 종료: {}건", startedCount, endedCount);
    }
}
```

### 동작 원리

```
시간    Instance A                    Instance B
────────────────────────────────────────────────────────────
T1      락 획득 시도                   락 획득 시도
        → shedlock 테이블 UPDATE

T2      락 획득 성공 ✅                락 획득 실패 ❌
        → lock_until = now + 5min     → 스킵

T3      startMatches() 실행
        endMatches() 실행

T4      작업 완료
        (락은 lockAtLeastFor까지 유지)
```

### 테스트 방법

#### 1. 로컬 테스트 (Docker Compose)

```bash
# 2개 인스턴스 실행
docker-compose up --scale backend=2
```

#### 2. 로그 확인

```
# Instance A 로그
INFO  - 스케줄러 완료 - 시작: 3건, 종료: 2건

# Instance B 로그
DEBUG - 락 획득 실패, 스킵
```

#### 3. DB 확인

```sql
SELECT * FROM shedlock WHERE name = 'updateMatchStatuses';
-- lock_until, locked_at, locked_by 확인
```

---

## Insight: 배운 점

### 기술적 인사이트

1. **@Version 예외는 성공의 증거**
   - `OptimisticLockingFailureException`은 "이미 처리 완료"를 의미
   - 별도 예외 처리가 필요 없음

2. **락 기반 vs 작업 분할은 다른 패러다임**
   - 락 기반: 경쟁 → 단일 실행
   - 샤드키: 분할 → 병렬 실행
   - 규모에 따라 적절한 방식 선택

3. **ShedLock의 lockAtLeastFor 중요성**
   - 스케줄 주기보다 약간 짧게 설정
   - 60초 주기 → 50초 lockAtLeastFor

4. **점진적 확장 전략**
   - 현재: ShedLock (간단, 충분)
   - 성장 시: 샤드키 기반 (병렬 처리)
   - 글로벌: 이벤트 드리븐 (Kafka)

### AI 협업 과정에서 얻은 인사이트

1. **용어 혼동 해소의 중요성**
   - "ShedLock"을 "샤드키 락"으로 오해
   - 정확한 용어 이해가 기술 선택의 기반

2. **대안 비교의 가치**
   - 스케줄러 락과 분산 락 방식 비교
   - 2가지 접근 방식(락 기반 vs 샤딩) 비교
   - 트레이드오프 이해 후 합리적 선택

3. **현재 규모에 맞는 솔루션**
   - "오버엔지니어링 경계"
   - 향후 확장 경로만 확보하고 현재는 단순하게

---

## 참고 자료

- [ShedLock GitHub](https://github.com/lukas-krecan/ShedLock) - 공식 문서
- [Redisson Distributed Locks](https://redisson.org/docs/distributed-locks.html) - Redis 분산 락
- [Martin Kleppmann - How to do distributed locking](https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html) - RedLock 비판
- [Consistent Hashing](https://en.wikipedia.org/wiki/Consistent_hashing) - 샤딩 전략

---

## 부록: 대화 로그

### Q1. Race Condition 분석

**Human:**
> 문제가 발생할 수 있는 상황은 다음과 같습니다. 해당 코드의 문제는 상태가 변경될 때 여러 인스턴스가 한번에 접근하게 되면 race condition 문제가 발생할 수 있습니다.

**AI:**
> T1~T5 타임라인으로 Race Condition 시나리오 분석. 두 서버가 동시에 같은 경기를 조회하고 처리하는 문제 도출.

---

### Q2. @Version 예외 처리 필요성

**Human:**
> version으로 인한 예외는 정상처리된겁니다. 그렇기 때문에 예외 처리할 필요가 없다고 판단했습니다.

**AI:**
> 동의. OptimisticLockingFailureException은 "이미 다른 인스턴스에서 처리 완료"를 의미하므로 별도 예외 처리 불필요.

---

### Q3. 락 방식 탐색

**Human:**
> 샤드키를 통한 분산 환경에서의 대처법과 여러가지가 있을텐데 이걸 알고싶어요

**AI:**
> 스케줄러 락(ShedLock, Quartz)과 분산 락(Redis, ZooKeeper, etcd) 방식 비교 설명

---

### Q4. ShedLock 적합성 확인

**Human:**
> hoops에는 ShedLock이 적합할 것 같은데 맞아?

**AI:**
> 맞음. MySQL만 사용 중이고 Redis 없음. 소규모 서비스에 ShedLock이 적합.

---

### Q5. 샤드키 기반 수평 확장

**Human:**
> 샤드키를 이용해서 수평적 확장에 대처 가능한 설계는 어떤가요?

**AI:**
> 락 기반과 샤드키 기반 설계는 다른 접근 방식. 현재 규모에서는 ShedLock(스케줄러 락), 10만건 이상 성장 시 샤드키 기반 검토 권고.

---

### Q6. Phantom Read 문제 분석

**Human:**
> phantom-read에 대한 문제는 발생하지 않을지 분석하고 설명해주세요

**AI:**
> 현재 코드는 한 트랜잭션 내에서 같은 쿼리를 두 번 실행하지 않으므로 Phantom Read 해당 없음. 조회-처리 사이 상태 변경(Lost Update)은 @Version이 방지. 진짜 문제는 Race Condition이며 ShedLock으로 해결.

---

### Q7. 용어 정확성 확인

**Human:**
> 분산락이라고 부르는게 맞아요?

**AI:**
> ShedLock은 "스케줄러 락"이 정확한 표현. 분산 락(Redis, ZooKeeper)과 스케줄러 락(ShedLock)은 다른 개념. 용어 구분 테이블 추가.

---

*이 문서는 Human과 AI의 협업 Deep Dive를 통해 작성되었습니다.*
