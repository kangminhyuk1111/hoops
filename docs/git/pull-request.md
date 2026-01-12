# Pull Request Convention

## 브랜치 네이밍

```
<type>/<description>
```

| Type | 설명 |
|------|------|
| feat | 새로운 기능 |
| fix | 버그 수정 |
| refactor | 리팩토링 |
| docs | 문서 작업 |
| chore | 설정, 빌드 |

예: `feat/kakao-oauth`, `fix/match-date-validation`, `refactor/participation-domain`

## PR 제목

```
<type>(<scope>): <subject>
```

커밋 컨벤션과 동일한 형식을 따른다.

예: `feat(auth): 카카오 OAuth 로그인 구현`

## PR 본문 템플릿

```markdown
## Summary
- 변경 사항 요약 (1-3줄)

## Changes
- 주요 변경 내용 목록

## Test
- 테스트 방법 또는 체크리스트
```

## 규칙

1. **1 PR = 1 기능**: 하나의 PR은 하나의 기능/수정만 포함
2. **작은 단위**: 리뷰 가능한 크기로 분할 (300줄 이하 권장)
3. **테스트 포함**: 기능 추가/수정 시 테스트 코드 필수
4. **CI 통과**: 모든 테스트 통과 후 머지

## 예시

**제목:**
```
feat(participation): 경기 참가 신청 기능 구현
```

**본문:**
```markdown
## Summary
- 사용자가 경기에 참가 신청할 수 있는 기능 추가

## Changes
- ParticipationService 구현
- POST /api/matches/{id}/participations 엔드포인트 추가
- 참가 인원 초과 검증 로직 추가

## Test
- [x] 참가 신청 성공 테스트
- [x] 인원 초과 시 예외 테스트
- [x] 중복 참가 방지 테스트
```
