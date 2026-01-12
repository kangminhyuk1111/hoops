# Commit Convention

## 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

## Type

| Type | 설명 |
|------|------|
| feat | 새로운 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 수정 |
| style | 코드 포맷팅 (세미콜론 누락 등, 기능 변경 없음) |
| refactor | 코드 리팩토링 (기능 변경 없음) |
| test | 테스트 코드 추가/수정 |
| chore | 빌드, 설정 파일 수정 |

## Scope (선택)

변경된 모듈/도메인을 명시한다.

예: `feat(auth):`, `fix(match):`, `refactor(participation):`

## Subject

- 명령형 현재 시제 사용 (add, fix, change)
- 첫 글자 소문자
- 마침표 없음
- 50자 이내

## Body (선택)

- 변경 이유와 이전 동작과의 차이점 설명
- 72자마다 줄바꿈

## Footer (선택)

- Breaking Changes: `BREAKING CHANGE:` 접두사 사용
- Issue 참조: `Closes #123`, `Fixes #456`

## 예시

```
feat(auth): 카카오 OAuth 로그인 구현

카카오 REST API를 사용한 소셜 로그인 기능 추가
- 인가 코드 발급
- 액세스 토큰 교환
- 사용자 정보 조회

Closes #12
```

```
fix(match): 경기 생성 시 날짜 검증 오류 수정

과거 날짜로 경기 생성이 가능했던 버그 수정
```

```
refactor(participation): 참가 취소 로직 도메인으로 이동
```
