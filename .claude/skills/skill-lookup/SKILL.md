---
name: skill-lookup
description: Hoops 프로젝트 스킬 인덱스. 사용 가능한 스킬 목록과 각 스킬의 용도를 안내. 어떤 스킬을 사용해야 할지 모를 때 참조.
---

# Hoops 스킬 인덱스

## 개발 워크플로우 스킬

| 스킬 | 용도 | 트리거 |
|------|------|--------|
| `/architecture-patterns` | Hexagonal Architecture 구조, 패키지 설계 | 새 도메인 생성, 패키지 구조 질문 |
| `/tdd-workflow` | ATDD 워크플로우, Cucumber 시나리오 작성 | 기능 구현 시작, 테스트 작성 |
| `/create-pull-request` | PR 생성, 커밋 분석, 브랜치 관리 | PR 생성 요청 |

## 품질 관리 스킬

| 스킬 | 용도 | 트리거 |
|------|------|--------|
| `/clean-code` | Self-validating entity, VO로 파라미터 그룹화, 트랜잭션 전략 | 도메인 모델 설계, 리팩토링 |
| `/code-review-excellence` | 코드 리뷰 가이드, 피드백 작성법 | PR 리뷰, 코드 검토 |
| `/debugging-strategies` | 체계적 디버깅, 성능 프로파일링 | 버그 조사, 성능 이슈 |

## 메타 스킬

| 스킬 | 용도 | 트리거 |
|------|------|--------|
| `/skill-creator` | 새 스킬 생성 가이드 | 스킬 생성/수정 요청 |

## 필수 사용 규칙

1. **아키텍처 작업** -> `/architecture-patterns` 필수
2. **기능 구현** -> `/tdd-workflow` 참조 (Cucumber 시나리오 우선)
3. **PR 생성** -> `/create-pull-request` 필수 (테스트 통과 확인)
