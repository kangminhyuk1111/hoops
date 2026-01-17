# Refactoring Checklist

> Task 1: Safe Refactoring with AI
>
> 기준: Private 메서드 제거, 메서드 10줄 이내, 영어처럼 읽히는 가독성

---

## Priority 1: 즉시 수정 (규칙 위반)

### 1.1 AuthService - IllegalStateException 직접 사용
- [ ] **파일**: `auth/application/service/AuthService.java`
- **위치**: Line 61-62
- **문제**: `IllegalStateException` 직접 사용 (CLAUDE.md 규칙 위반)
- **해결**: `AuthAccountUserNotFoundException` 예외 클래스 생성

### 1.2 AuthService - 일반 Exception catch
- [ ] **파일**: `auth/application/service/AuthService.java`
- **위치**: Line 146
- **문제**: `catch (Exception e)` 사용
- **해결**: 명시적 예외 타입으로 변경 (`JwtException`, `SignatureException`)

### 1.3 Notification - Boolean 타입 사용
- [ ] **파일**: `notification/domain/Notification.java`
- **위치**: Line 20
- **문제**: `Boolean isRead` (nullable)
- **해결**: `boolean isRead` (primitive)로 변경

---

## Priority 2: 메서드 분할 (10줄 초과)

### 2.1 AuthService.processCallback() - 38줄
- [ ] **파일**: `auth/application/service/AuthService.java`
- **위치**: Line 52-89
- **분할 방안**:
  - `processExistingUserCallback()` 추출
  - `processNewUserCallback()` 추출

### 2.2 AuthService.signup() - 44줄
- [ ] **파일**: `auth/application/service/AuthService.java`
- **위치**: Line 92-135
- **분할 방안**:
  - `createNewUser()` 추출
  - `createAuthAccount()` 추출
  - `buildSignupResponse()` 추출

### 2.3 AuthService.testLogin() - 59줄
- [ ] **파일**: `auth/application/service/AuthService.java`
- **위치**: Line 176-234
- **분할 방안**:
  - `getOrCreateTestUser()` 추출
  - `updateTestUserRefreshToken()` 추출

### 2.4 MatchRepositoryImpl.findAllByLocation() - 26줄
- [ ] **파일**: `match/adapter/out/adapter/MatchRepositoryImpl.java`
- **위치**: Line 42-67
- **분할 방안**:
  - `GeoLocationCalculator` 컴포넌트 생성
  - `BoundingBoxCalculator` 컴포넌트 생성

### 2.5 Match.update() - 29줄
- [ ] **파일**: `match/domain/Match.java`
- **위치**: Line 161-189
- **분할 방안**:
  - `calculateNewStatus()` 메서드 추출

---

## Priority 3: Private 메서드 → 객체 통신

### 3.1 MatchCreator - validateNoOverlappingHosting()
- [ ] **파일**: `match/application/service/MatchCreator.java`
- **위치**: Line 64-75
- **해결**: `OverlappingHostingValidator` 컴포넌트 생성

### 3.2 MatchUpdater - validateUpdate()
- [ ] **파일**: `match/application/service/MatchUpdater.java`
- **위치**: Line 45-62
- **해결**: `MatchUpdateValidator` 컴포넌트 생성

### 3.3 LocationCreator - 검증 메서드들
- [ ] **파일**: `location/application/service/LocationCreator.java`
- **위치**: Line 39-49
- **문제**: `validateLocationName()`, `checkDuplicateName()`
- **해결**: `LocationNameValidator` 컴포넌트 생성

### 3.4 AuthService - 검증/업데이트 메서드들
- [ ] **파일**: `auth/application/service/AuthService.java`
- **위치**: Line 143-173
- **문제**:
  - `extractAndValidateTempToken()`
  - `validateNickname()`
  - `updateRefreshToken()`
- **해결**:
  - `TempTokenValidator` 생성
  - `NicknameValidator` 생성
  - `RefreshTokenManager` 생성

### 3.5 ParticipationValidator - 내부 검증 메서드들
- [ ] **파일**: `participation/application/service/ParticipationValidator.java`
- **위치**: Line 60-123
- **문제**: 8개의 private 검증 메서드
- **해결**: 각 검증을 개별 Validator 컴포넌트로 분리 (또는 유지 - 이미 Validator로 분리됨)

### 3.6 ParticipationService - 헬퍼 메서드들
- [ ] **파일**: `participation/application/service/ParticipationService.java`
- **위치**: Line 128-163
- **문제**:
  - `findParticipation()`
  - `findOrCreateParticipation()`
  - `publishParticipationCreatedEvent()`
  - `processCancellation()`
- **해결**:
  - `ParticipationFinder` 컴포넌트 생성
  - 이벤트 발행은 이미 `ParticipationEventPublisher` 포트 존재

### 3.7 MatchRepositoryImpl - calculateDistanceInMeters()
- [ ] **파일**: `match/adapter/out/adapter/MatchRepositoryImpl.java`
- **위치**: Line 72-86
- **해결**: `GeoLocationCalculator` 컴포넌트 생성

### 3.8 MatchInfoAdapter - toMatchInfo()
- [ ] **파일**: `participation/infrastructure/adapter/MatchInfoAdapter.java`
- **위치**: Line 42-54
- **해결**: `MatchParticipationDataMapper` 생성

---

## Priority 4: 가독성 개선

### 4.1 ParticipationService - @Recover 메서드 정리
- [ ] **파일**: `participation/application/service/ParticipationService.java`
- **위치**: Line 165-212
- **문제**: 8개의 중복된 복구 메서드
- **해결**: 2-3개로 통합하거나 `RetryExceptionHandler` 컴포넌트 생성

### 4.2 네이밍 개선
- [ ] `Participation.reactivate()` → `reactivateAfterCancellation()`
- [ ] `User.updateProfile()` → `updateNicknameAndProfileImage()`
- [ ] `minLat`, `maxLng` → `minimumLatitude`, `maximumLongitude`

---

## Priority 5: 코드 중복 제거

### 5.1 AuthService - Token 관리 로직 중복
- [ ] **파일**: `auth/application/service/AuthService.java`
- **위치**: Line 163-172, Line 216-224
- **해결**: `AuthAccountTokenUpdater` 컴포넌트 생성

### 5.2 AuthService - UserInfo 생성 로직 중복
- [ ] **파일**: `auth/application/service/AuthService.java`
- **위치**: Line 67-72, 127-132, 226-231
- **해결**: `UserInfoFactory` 또는 factory 메서드 생성

### 5.3 시간 검증 로직 중복
- [ ] **여러 파일에 분산**
- **해결**: `TimeRangeValidator` 공용 컴포넌트 생성

---

## 완료된 항목

### ParticipationService 리팩토링 (2026-01-17)
- [x] `ParticipationValidator` 클래스 추출
- [x] 검증 로직 분리
- [x] 서비스 메서드 간결화
- [x] `Participation.createPending()` 팩토리 메서드 추가

---

## 우수 사례 (유지할 것)

- **Constructor Injection**: 모든 파일에서 `@RequiredArgsConstructor` 사용
- **포트 기반 아키텍처**: `LocationInfoAdapter`, `UserHostInfoAdapter`, `MatchInfoAdapter`
- **Transactional 명확성**: `@Transactional(readOnly = true)` 적절히 사용
- **이벤트 기반 아키텍처**: `ParticipationEventPublisher` 포트 추상화
- **MatchPolicyValidator**: 정책 검증 분리 패턴 (우수 사례)

---

## 진행 방법

1. Priority 순서대로 진행
2. 각 항목 수정 전 테스트 Green 확인
3. 리팩토링 후 테스트 재실행
4. 완료 시 체크박스 표시 및 날짜 기록
