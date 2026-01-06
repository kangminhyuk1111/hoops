# Database Schema

## 개요
본 문서는 데이터베이스 스키마를 정의합니다. ERD와 테이블 설계는 실제 프로젝트 개발 시 작성합니다.

## ERD (Entity Relationship Diagram)
- 실제 개발 시 ERD 다이어그램 삽입 또는 링크 추가

## 테이블 정의

### [테이블명]
**설명**: 테이블 용도 설명

| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|-------|------|------|--------|------|
| id | BIGINT | NO | AUTO_INCREMENT | Primary Key |
| created_at | DATETIME | NO | CURRENT_TIMESTAMP | 생성일시 |
| updated_at | DATETIME | NO | CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스**:
- PRIMARY KEY: `id`
- INDEX: (필요시 추가)

**제약조건**:
- FOREIGN KEY: (필요시 추가)
- UNIQUE: (필요시 추가)

---

## 작성 가이드
1. 도메인별로 테이블 그룹화
2. 컬럼명은 snake_case 사용
3. 모든 테이블에 created_at, updated_at 포함 권장
4. 외래키 관계는 명확히 표시
5. 인덱스 전략은 성능 고려하여 설계
