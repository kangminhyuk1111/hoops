# 개념 비교 가이드 (Concept Guide)

> 이 문서는 **자주 헷갈리는 개념들을 비교 설명**합니다.

---

## 🎯 Match vs Participation - 무엇이 다른가?

### 개념 정의

**Match (경기)**
```
특정 날짜, 시간, 장소에서 하는 농구 경기
예: "내일 오후 7시, 광주 시청 농구장에서 하는 경기"

특징:
  • 1개의 경기에 1개만 존재
  • Host가 만듦
  • 날짜, 시간, 장소가 정해짐
  • 정원: n명 (고정)
```

**Participation (신청)**
```
사용자가 경기에 참가하려는 의사를 표현한 것
예: "철수가 내일 광주 경기에 신청했어"

특징:
  • 1개의 경기에 여러 개 존재 (신청한 사람만큼)
  • 신청자(Applicant)가 만듦
  • 신청 시간이 기록됨
  • 상태가 계속 변함
```

### 비교 표

| 항목 | Match | Participation |
|------|-------|-------------|
| **정의** | 특정 날짜, 시간, 장소에서 하는 경기 | 사용자가 경기에 참가하려는 의사 |
| **주인** | Host | Applicant |
| **개수** | 1개 | 여러 개 (신청자마다) |
| **생성** | Host가 경기 생성 | Applicant가 신청 |
| **상태 변경** | 시간 경과 또는 Host 취소 | 경기 상태에 따라 자동 변경 |
| **예시** | "경기: 내일 7시 광주 코트" | "철수의 신청: 경기에 신청함" |
| **삭제** | Host가 취소 | 신청자가 취소 |
| **신뢰도** | 영향 없음 | 노쇼 시 신뢰도 감소 |

### 실제 시나리오

```
민혁이 경기를 만듦
  → Match 1개 생성
  → 상태: RECRUITING

철수, 영희, 준호가 신청
  → Participation 3개 생성
  → 상태: APPLIED

4명이 모임
  → Match 상태: RECRUITING → CONFIRMED (자동)
  → 3개 Participation 상태: APPLIED → CONFIRMED (자동)

경기가 끝남
  → Match 상태: CONFIRMED → COMPLETED (자동)
  → Participation 상태:
    • 민혁: CONFIRMED → COMPLETED
    • 철수: CONFIRMED → COMPLETED
    • 영희: CONFIRMED → COMPLETED
    • 준호: CONFIRMED → NO_SHOW (참석하지 않음)
```

---

## 🎯 Host vs Applicant vs Participant - 누구인가?

### 개념 정의

**Host (주최자)**
```
경기를 만드는 사람
예: "민혁이가 경기를 만들었어"

특징:
  • 경기 생성 시 자동 할당
  • 1경기에 1명만 가능
  • 신뢰도 3.0 이상이어야 경기 생성 가능
  • 경기 생성 시 자동으로 참여 확정
```

**Applicant (신청자)**
```
경기에 신청한 사용자
예: "철수가 경기에 신청했어"

특징:
  • 신청할 때 할당
  • 같은 사람이 경기 B에 신청하면 경기 B의 신청자가 됨
  • 신청 상태 = APPLIED
  • 신청을 취소할 수 있음
```

**Participant (참여자)**
```
경기에 확정된 사람 (총 n명)
예: "이 경기의 참여자는 민혁, 철수, 영희, 준호 4명이야"

특징:
  • 경기가 CONFIRMED되면 참여자가 됨
  • 항상 정확히 n명
  • 신청 상태 = CONFIRMED
  • Host도 참여자에 포함됨
```

### 비교 표

| 항목 | Host | Applicant | Participant |
|------|------|-----------|-------------|
| **역할** | 경기를 만드는 사람 | 경기에 신청한 사람 | 경기에 확정된 사람 |
| **할당 시기** | 경기 생성 시 | 신청할 때 | 경기 CONFIRMED 시 |
| **상태** | 항상 CONFIRMED | APPLIED → CONFIRMED | CONFIRMED |
| **개수** | 1명 | 신청자만큼 | 정확히 n명 |
| **취소 가능** | 경기 취소 가능 | 신청 취소 가능 | 신청 취소 가능 |
| **신뢰도 영향** | 경기 취소 시 없음 | 노쇼 시 -0.5 | 노쇼 시 -0.5 |
| **예시** | "내가 경기를 만들었어" | "철수가 신청했어" | "4명이 경기해" |

### 타임라인으로 이해하기

```
[경기 생성]
  민혁: Host (자동 CONFIRMED) + Participant
  상태: Host 1명 + 신청자 0명 = 인원 부족

[1번째 신청]
  철수: Applicant (APPLIED)
  상태: Host 1명 + 신청자 1명 = 인원 부족

[2번째 신청]
  영희: Applicant (APPLIED)
  상태: Host 1명 + 신청자 2명 = 인원 부족

[3번째 신청]
  준호: Applicant (APPLIED)
  상태: Host 1명 + 신청자 3명 = 4명 완성!
  → 경기 자동 CONFIRMED
  → 모든 신청자 자동 CONFIRMED
  
[경기 확정 후]
  민혁: Participant (CONFIRMED)
  철수: Participant (CONFIRMED)
  영희: Participant (CONFIRMED)
  준호: Participant (CONFIRMED)
```

---

## 🎯 상태 변경의 종류

### 자동 상태 변경 (자동으로 일어남)

```
1. 경기: RECRUITING → CONFIRMED
   조건: Host가 (n-1)명을 승인 (본인 포함 n명)
   결과: 승인된 모든 신청 자동 CONFIRMED

2. 경기: CONFIRMED → COMPLETED
   조건: 경기 시간이 지남
   결과: Participation은 각각 COMPLETED 또는 NO_SHOW로 변경

3. 신청: APPROVED → CONFIRMED
   조건: Host가 n명 승인 완료 (경기 CONFIRMED)
   결과: 자동으로 승인된 모든 신청 상태 변경

4. 신청: CONFIRMED → COMPLETED
   조건: 경기 시간이 지나고 참석
   결과: 신뢰도 점수 계산

5. 신청: CONFIRMED → NO_SHOW
   조건: 경기 시간이 지나고 미참석
   결과: 신뢰도 -0.5, 노쇼 카운트 증가
```

### 수동 상태 변경 (누군가가 해야 함)

```
1. 신청: PENDING_APPROVAL → APPROVED
   누가: Host
   결과: 신청자에게 승인 알림

2. 신청: PENDING_APPROVAL → REJECTED
   누가: Host
   결과: 신청자에게 거부 알림 (선택적)

3. 경기: RECRUITING/CONFIRMED → CANCELLED
   누가: Host
   결과: 모든 신청도 자동 취소

4. 신청: PENDING_APPROVAL/APPROVED/CONFIRMED → CANCELLED
   누가: 신청자
   결과: 신뢰도 영향 없음
```

---

## 🎯 신청 상태가 자동으로 변경되는 흐름

### 신청자의 입장에서 본 흐름

```
1️⃣ 신청 (Applicant이 됨)
   "경기에 신청하기" 클릭
   → Participation 생성
   → 상태: PENDING_APPROVAL
   → Host에게만 "신청됨" 알림

2️⃣ Host 승인 대기
   Host가 신청자 목록 확인
   → Host가 "승인" 또는 "거부" 선택

3️⃣ 승인됨 (Approved)
   Host가 "승인" 버튼 클릭
   → 상태: PENDING_APPROVAL → APPROVED
   → 신청자에게 "승인됨" 알림
   → 아직 경기 확정 전 (다른 사람들도 승인 대기)

4️⃣ 경기 인원 완성 (Participant가 됨)
   Host가 총 (n-1)명을 승인 완료
   → 경기 자동 CONFIRMED
   → 모든 APPROVED 신청 자동 CONFIRMED ⭐
   → 모두에게 "경기 확정" 알림

5️⃣ 경기 시작 1시간 전
   → "경기 시작 1시간 전" 알림

6️⃣ 경기가 끝남
   → 참석: CONFIRMED → COMPLETED
   → 미참석: CONFIRMED → NO_SHOW
```

### 신청자가 직접 하는 행동

```
- 경기 신청: "신청하기" 클릭 → PENDING_APPROVAL
- 신청 취소: "신청 취소" 클릭 → CANCELLED
- 평가 작성: "평가하기" 클릭
- 리뷰 작성: "리뷰 작성" 클릭

신청자가 할 수 없는 것:
  ✗ 신청을 승인/거부 (Host만 가능)
  ✗ 신청 상태를 APPROVED로 변경 (Host가 승인)
  ✗ 신청 상태를 CONFIRMED로 변경 (경기가 자동)
  ✗ 신청 상태를 COMPLETED로 변경 (경기 시간 경과 자동)
  ✗ 신청 상태를 NO_SHOW로 변경 (경기 시간 경과 자동)
```

### Host가 직접 하는 행동

```
- 경기 생성: "경기 만들기" 클릭
- 신청 승인: "승인" 클릭 → PENDING_APPROVAL → APPROVED
- 신청 거부: "거부" 클릭 → PENDING_APPROVAL → REJECTED
- 경기 취소: "경기 취소" 클릭 → CANCELLED

Host가 할 수 없는 것:
  ✗ 승인된 참여자를 다시 거부 (APPROVED 이후 변경 불가)
  ✗ n명 이상 승인 (본인 포함 n명까지만)
  ✗ 경기 확정을 수동으로 변경 (n명 승인 시 자동)
```

---

## 🎯 신뢰도 변동 시나리오

### 시나리오 1: 정상 완료

```
상황: 철수가 경기에 신청 → 경기 완료 → 참석

경기 진행:
  1. 철수: 신청 (APPLIED)
  2. 4명 완성: 신청 자동 CONFIRMED
  3. 경기 진행
  4. 경기 완료: CONFIRMED → COMPLETED
  5. 철수가 평가 받음: +0.3점

신뢰도:
  변동 전: 3.2점
  변동 후: 3.5점 ⬆️
```

### 시나리오 2: 노쇼

```
상황: 준호가 경기에 신청 → 경기 완료 → 미참석

경기 진행:
  1. 준호: 신청 (APPLIED)
  2. 4명 완성: 신청 자동 CONFIRMED
  3. 경기 진행
  4. 경기 완료: CONFIRMED → NO_SHOW ⚠️
  5. 준호의 신뢰도 자동 감소: -0.5점

신뢰도:
  변동 전: 3.5점
  변동 후: 3.0점 ⬇️

누적:
  노쇼 기록 누적
  노쇼 3회 이상 → 자동 정지

경고:
  "경기에 나타나지 않아 신뢰도가 하락했어요" 알림
```

### 시나리오 3: 경기 취소

```
상황: 영희가 신청했는데 Host가 경기를 취소

경기 진행:
  1. 영희: 신청 (APPLIED)
  2. Host가 경기 취소
  3. 경기 상태: RECRUITING → CANCELLED
  4. 영희의 신청: APPLIED → CANCELLED (자동)

신뢰도:
  변동 없음 (취소했으므로)

결과:
  영희: "경기가 취소되었어요" 알림
```

---

## 📋 용어 사용 체크리스트

```
□ "신청자"와 "참여자"를 구분해서 사용하는가?
  → 신청자: PENDING_APPROVAL 또는 APPROVED 상태
  → 참여자: CONFIRMED 상태 (n명)

□ "승인"과 "확정"을 구분하는가?
  → 승인: Host가 개별 신청을 승인 (APPROVED)
  → 확정: Host가 n명 승인 완료 후 경기 자동 확정 (CONFIRMED)

□ "경기 취소"와 "신청 취소"를 구분하는가?
  → 경기 취소: Host가 함 (모든 신청도 취소)
  → 신청 취소: 신청자가 함

□ 상태 변경이 "자동"인지 "수동"인지 알고 있는가?
  → 자동: 시간 경과, Host가 n명 승인 완료
  → 수동: Host 승인/거부, 신청자 취소

□ Host도 "참여자"에 포함되는 것을 알고 있는가?
  → n명 = Host 1 + 승인된 신청자 (n-1)
```

---

**이 가이드를 읽고 나면, 팀원들과 헷갈림 없이 대화할 수 있습니다!**