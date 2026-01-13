# 고도화 포인트 기술서 (Optimization Report)

> 작성일: 2026-01-13
> 작성자: AI Code Review + Human Refinement
> 대상: hoops 프로젝트 백엔드

---

## 개요

### 리뷰 배경
프로덕션 배포 전, AI 코드 리뷰를 통해 잠재적 이슈(성능, 동시성, 확장성)를 사전 식별하고, 비즈니스 관점에서 우선순위를 판단하여 문서화함.

### 분석 범위
- Match 도메인 (경기 생성/취소/상태 관리)
- Participation 도메인 (참가 신청/승인/거절)
- Notification 도메인 (알림 발송/조회)
- User 도메인 (프로필 관리)

### 서비스 규모 가정
| 항목 | 예상 수치 |
|------|----------|
| 시간당 사용자 | 10~100명 |
| 총 경기 데이터 | 수백 개 |
| 서버 구성 | 단일 인스턴스 |
| 핵심 트래픽 | 지도 기반 경기 검색 |

---

## Section A. 수용된 이슈 (Accepted Issues)

### A-1. 경기 참가 시 정원 초과 Race Condition

#### [이슈 정의]
경기 정원 마감 시점에 동시 참가 요청이 발생하면, 최대 인원을 초과하여 참가가 승인될 수 있음.

#### [예상 원인]
현재 낙관적 락(`@Version`)만 적용되어 있어, 동시 요청 시 "읽기 → 검증 → 쓰기" 사이에 Race Condition 발생.

```
시나리오:
1. 정원 10명 경기, 현재 9명 참가 중
2. 사용자 A: currentParticipants=9 읽음 → "참가 가능" 판단
3. 사용자 B: currentParticipants=9 읽음 → "참가 가능" 판단
4. A가 먼저 저장 → currentParticipants=10
5. B도 저장 시도 → version 충돌 없이 성공 (같은 값이므로)
6. 결과: 정원 10명인데 11명 참가
```

#### [영향받는 코드]
- `MatchEntity.java:27-28` - `@Version` 필드
- `ParticipationService.java:85-103` - `applyParticipation()` 메서드
- `MatchParticipationAdapter.java:28-32` - `addParticipant()` 메서드

#### [딥다이브]

**검토한 해결 방안들:**

| 방안 | 장점 | 단점 |
|------|------|------|
| 비관적 락 (Pessimistic Lock) | 추가 인프라 불필요, JPA 네이티브 지원 | DB 락으로 인한 대기 시간 |
| Redis 분산 락 | 빠른 락 획득/해제 | Redis 인프라 필요, 복잡도 증가 |
| 낙관적 락 + 재시도 | 충돌 적을 때 효율적 | 충돌 많으면 재시도 폭증 |
| DB 유니크 제약조건 | 가장 확실한 방어 | 비즈니스 로직 표현 한계 |

**선택: 비관적 락 (Pessimistic Lock)**

선택 근거:
1. 현재 단일 서버 구성으로 Redis 도입은 Over-engineering
2. 시간당 10~100명 트래픽에서 락 대기 시간은 무시할 수준
3. JPA `@Lock(LockModeType.PESSIMISTIC_WRITE)` 으로 간단히 구현 가능
4. 향후 트래픽 증가 시 Redis 분산 락으로 전환 용이

#### [해결 방향]

**1단계: Match 조회 시 비관적 락 적용**

```java
// MatchRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT m FROM MatchEntity m WHERE m.id = :matchId")
Optional<MatchEntity> findByIdWithLock(@Param("matchId") Long matchId);
```

**2단계: 참가 신청 로직에서 락 적용 조회 사용**

```java
// MatchParticipationAdapter.java
@Transactional
public void addParticipant(Long matchId) {
    MatchEntity match = matchRepository.findByIdWithLock(matchId)
        .orElseThrow(() -> new MatchNotFoundException(matchId));

    if (match.getCurrentParticipants() >= match.getMaxParticipants()) {
        throw new MatchFullException(matchId);
    }

    match.incrementParticipants();
    matchRepository.save(match);
}
```

**3단계: 트랜잭션 타임아웃 설정**

```java
@Transactional(timeout = 5) // 5초 타임아웃
public void addParticipant(Long matchId) { ... }
```

---

### A-2. 참가 승인/거절 동시 요청 시 상태 불일치

#### [이슈 정의]
호스트가 동일한 참가 요청에 대해 승인과 거절을 거의 동시에 클릭하면, 의도치 않은 최종 상태가 저장될 수 있음.

#### [예상 원인]
Participation 엔티티에 버전 관리가 없어, 동시 상태 변경 시 Last-Write-Wins 발생.

```
시나리오:
1. 호스트가 실수로 "승인" 버튼을 두 번 빠르게 클릭
2. 첫 번째 요청: PENDING → CONFIRMED (+ Match.currentParticipants 증가)
3. 두 번째 요청: 이미 CONFIRMED인데 다시 처리 시도
4. 결과: currentParticipants가 2번 증가하거나, 중복 알림 발송
```

#### [영향받는 코드]
- `ParticipationService.java:207-246` - `approveParticipation()`, `rejectParticipation()`
- `ParticipationEntity.java` - 버전 필드 부재

#### [딥다이브]

**현재 방어 로직 분석:**
- `@Retryable` 어노테이션으로 낙관적 락 충돌 시 재시도는 구현됨
- 그러나 Participation 엔티티 자체에 `@Version`이 없어 충돌 감지 불가

**검토한 해결 방안들:**

| 방안 | 적용 난이도 | 효과 |
|------|------------|------|
| Participation에 @Version 추가 | 낮음 | 동시 수정 감지 가능 |
| 상태 전이 전 재조회 | 낮음 | 최신 상태 확인 |
| 멱등성 키 도입 | 중간 | 중복 요청 완전 차단 |

**선택: @Version 추가 + 상태 검증 강화**

#### [해결 방향]

**1단계: Participation 엔티티에 버전 필드 추가**

```java
// ParticipationEntity.java
@Version
private Long version;
```

**2단계: 상태 전이 전 명시적 검증**

```java
// ParticipationService.java
public void approveParticipation(ApproveParticipationCommand command) {
    Participation participation = participationRepository
        .findById(command.participationId())
        .orElseThrow(...);

    // 이미 처리된 요청인지 확인
    if (participation.getStatus() != ParticipationStatus.PENDING) {
        throw new ParticipationAlreadyProcessedException(
            command.participationId(),
            participation.getStatus()
        );
    }

    participation.approve();
    // ...
}
```

---

## Section B. 기각된 이슈 (Rejected Issues)

### B-1. N+1 쿼리: 참가 검증 시 Match 반복 조회

#### [AI 지적 사항]
`ParticipationService.validateNoOverlappingParticipation()`에서 사용자의 기존 참가 목록을 조회한 후, 각 Match 정보를 개별 조회하는 N+1 패턴 발생.

#### [기각 근거]
1. **현재 규모에서 무의미**: 사용자당 동시 참가 경기는 평균 1~3개 수준
2. **이미 배치 조회 구현됨**: `getMatchInfoByIds()`가 `IN` 절로 한 번에 조회
3. **체감 성능 차이 없음**: 시간당 100명 트래픽에서 추가 쿼리 수십 회는 무시 가능

#### [재검토 시점]
- 사용자당 동시 참가 경기가 10개 이상으로 증가할 때
- 시간당 트래픽이 1,000명 이상으로 증가할 때

---

### B-2. N+1 쿼리: 알림 목록 페이지네이션 부재

#### [AI 지적 사항]
`NotificationService.getNotifications()`에서 페이지네이션 없이 모든 알림을 메모리에 로드.

#### [기각 근거]
1. **데이터 양 제한적**: 서비스 초기 단계로 사용자당 알림 수십~수백 개 수준
2. **MVP 우선**: 현재는 기능 완성도가 우선, 최적화는 후순위
3. **프론트엔드 미구현**: 알림 페이지 자체가 아직 미구현 상태

#### [재검토 시점]
- 알림 페이지 프론트엔드 구현 시 함께 페이지네이션 적용
- 사용자당 알림이 1,000개 이상 누적될 때

---

### B-3. N+1 쿼리: 참가자 목록 조회 시 User 개별 조회

#### [AI 지적 사항]
참가자 목록 조회 시 각 참가자의 User 정보를 개별 조회하는 N+1 발생 가능성.

#### [기각 근거]
1. **이미 배치 조회 구현됨**: `getUserInfoByIds()`로 한 번에 조회
2. **경기당 참가자 제한**: 최대 20명으로 제한되어 있어 최악의 경우에도 20회
3. **체감 성능 차이 없음**: 20회 쿼리도 수십 ms 내 완료

#### [재검토 시점]
- 최대 참가자 수가 100명 이상으로 확장될 때

---

### B-4. 위치 기반 검색 Full Table Scan

#### [AI 지적 사항]
`JpaMatchRepository`의 위치 기반 검색이 Spatial Index 없이 Full Table Scan 수행.

#### [기각 근거]
1. **데이터 규모 제한적**: 현재 예상 경기 수는 수백 개 수준
2. **MySQL 기본 성능 충분**: 수백 건 테이블 스캔은 수십 ms 이내
3. **조기 최적화 지양**: Spatial Index는 데이터 1,000건 이상에서 효과적

#### [재검토 시점]
- 경기 데이터가 1,000개 이상으로 증가할 때
- 메인 페이지 응답 시간이 500ms 이상으로 체감될 때
- 해결 방향: MySQL Spatial Index 또는 PostGIS 도입 검토

---

### B-5. 배치 작업 중복 실행 가능성

#### [AI 지적 사항]
`MatchStatusUpdater` 스케줄러가 다중 인스턴스에서 동시 실행 시 중복 처리 가능.

#### [기각 근거]
1. **단일 서버 구성**: 현재 단일 인스턴스로 운영 예정
2. **멱등성 보장**: 이미 상태 변경된 경기는 재처리해도 동일 결과
3. **분산 락 도입 비용**: ShedLock 등 도입은 Over-engineering

#### [재검토 시점]
- 다중 인스턴스 배포(수평 확장) 시
- 해결 방향: ShedLock 또는 Quartz Cluster 모드 도입

---

### B-6. UserRepository UPDATE 시 불필요한 SELECT

#### [AI 지적 사항]
`UserRepositoryImpl.save()`에서 UPDATE 전 `findById()`로 불필요한 SELECT 수행.

#### [기각 근거]
1. **User 업데이트 빈도 낮음**: 프로필 수정은 드문 작업
2. **성능 차이 미미**: SELECT 1회 추가는 수 ms 수준
3. **코드 안정성 우선**: 명시적 존재 확인이 디버깅에 유리

#### [재검토 시점]
- 사용자 정보 실시간 업데이트 기능 추가 시 (예: 실시간 레이팅)

---

### B-7. 중복 호스팅 검증 시 전체 경기 로드

#### [AI 지적 사항]
`MatchCreator.validateNoOverlappingHosting()`에서 호스트의 모든 활성 경기를 메모리에 로드.

#### [기각 근거]
1. **호스트당 경기 수 제한적**: 일반적으로 동시에 수십 개 이하
2. **경기 생성 빈도 낮음**: 호스트가 경기를 자주 생성하지 않음
3. **쿼리 최적화 복잡도**: 시간 겹침 조건을 SQL로 표현하면 가독성 저하

#### [재검토 시점]
- "파워 호스트" 기능 추가로 한 호스트가 100개 이상 경기 운영 시

---

### B-8. 알림 unread count 매번 COUNT 쿼리

#### [AI 지적 사항]
`NotificationRepository.countUnreadByUserId()`가 매 요청마다 COUNT 쿼리 실행.

#### [기각 근거]
1. **트래픽 규모 제한적**: 시간당 100명이면 COUNT 쿼리도 100회 수준
2. **Redis 도입 비용**: 캐시 무효화 로직까지 고려하면 복잡도 증가
3. **DB 인덱스로 충분**: `(user_id, is_read)` 인덱스면 COUNT도 빠름

#### [재검토 시점]
- 실시간 알림 뱃지 업데이트 요구사항 추가 시
- 해결 방향: Redis INCR/DECR 또는 Denormalization

---

## 결론 및 액션 아이템

### 즉시 적용 (P0)

| 이슈 | 작업 | 예상 소요 |
|------|------|----------|
| A-1 | Match 조회 시 비관적 락 적용 | 2시간 |
| A-2 | Participation 엔티티 @Version 추가 | 1시간 |

### 모니터링 후 결정

| 트리거 조건 | 적용할 최적화 |
|------------|--------------|
| 경기 1,000개 초과 | Spatial Index 도입 |
| 시간당 1,000명 초과 | N+1 쿼리 최적화 |
| 다중 인스턴스 배포 | ShedLock 도입 |
| 알림 페이지 구현 | 페이지네이션 적용 |

---

## 부록: AI 리뷰 로그

### 사용 도구
- Claude Code (Opus 4.5)
- 분석 일시: 2026-01-13

### 리뷰 방법론
1. 핵심 서비스 코드 자동 분석 (N+1, 동시성, 성능, 확장성 관점)
2. 10개 잠재적 이슈 도출
3. 비즈니스 규모/특성 기반 Human Refinement
4. 2개 수용, 8개 기각 결정

### 주요 질문 및 판단 기준
- "트래픽 100배 증가 시 병목 지점은?"
- "현재 비즈니스 규모에서 치명적인가?"
- "추가 인프라 없이 해결 가능한가?"
