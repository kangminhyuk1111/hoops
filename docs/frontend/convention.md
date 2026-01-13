# Frontend Convention

## UI 기본 원칙

- **모바일 고정 레이아웃**: 화면 최대 너비 430px 고정
- 데스크톱에서도 모바일 앱처럼 중앙 정렬되어 표시
- 앱 서비스 출시 목적이므로 반응형 디자인 불필요

## 스타일링

- Tailwind CSS 사용
- `hover:` 대신 `active:` 사용 (터치 피드백)
- 버튼/입력 필드 높이: `py-3.5` (터치 친화적)
- 폰트 크기: `text-sm`, `text-xs` 기본

## 레이아웃 구조

- `layout.tsx`에서 `max-w-[430px]` 컨테이너로 전체 감싸기
- 각 페이지는 `min-h-screen` 사용
- Sticky 헤더 패턴 사용

## 환경변수

- Docker 빌드 시 `NEXT_PUBLIC_*` 변수는 build args로 전달 필요
- `docker-compose.yml`의 frontend service에 args 설정 확인
