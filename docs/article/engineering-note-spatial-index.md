# Engineering Note: Spatial Index를 활용한 위치 기반 검색 최적화

> 작성일: 2026-01-16
> 참고 아티클: [Spatial Index를 활용한 공간 데이터 조회 최적화](https://haril.dev/blog/2024/03/03/Spatial-index)

---

## 1. Selection Reason

hoops 프로젝트는 위치 기반 농구 경기 매칭 서비스다. 핵심 기능인 "내 주변 경기 검색"은 현재 2단계 필터링 방식을 사용한다: DB에서 Bounding Box로 1차 필터링 후, Java에서 Haversine 공식으로 2차 필터링한다.

문제는 스키마에 Spatial Index(`idx_match_location`)가 이미 정의되어 있음에도 실제 쿼리에서 활용되지 않는다는 점이다. `latitude BETWEEN ? AND ?` 형태의 쿼리는 일반 B-Tree 인덱스를 사용하며, Bounding Box 내 모든 데이터를 애플리케이션 메모리에 로드한 후 Java에서 거리 계산을 수행한다.

이 방식은 데이터가 증가할수록 메모리 사용량과 CPU 연산이 선형적으로 증가한다. HARIL 블로그는 정확히 이 문제를 다루며, Spatial Index 적용으로 1분 47초 → 0.23ms라는 극적인 성능 개선 사례를 보여준다.

---

## 2. Key Takeaways

저자가 제시한 해결책의 핵심은 R-Tree 기반 Spatial Index다.

**R-Tree 인덱스 원리**:
- 평면을 직사각형(MBR)으로 계층적 분할
- 검색 시 관련 없는 영역을 조기에 제거(Pruning)
- 시간복잡도: O(N) → O(log N)

**MySQL 적용 시 핵심 포인트**:
- `POINT` 타입 컬럼에 `SPATIAL INDEX` 생성
- **SRID 4326 필수**: 미설정 시 인덱스가 무효화됨
- `ST_Distance_Sphere()`, `ST_Contains()` 등 공간 함수 활용

저자의 사례에서 가장 인상적인 부분은 "DB 통합 + Spatial Index"라는 단순한 변경만으로 500배 성능 향상을 달성했다는 점이다. 복잡한 캐싱이나 분산 처리 없이도 DB 본연의 기능을 제대로 활용하면 충분하다는 교훈을 준다.

---

## 3. Application

### 현재 프로젝트 상태

현재 hoops 프로젝트는 Bounding Box로 1차 필터링 후 Java에서 Haversine 거리 계산으로 2차 필터링하는 방식을 사용한다. 스키마에는 `location POINT ... SRID 4326`과 `SPATIAL INDEX`가 이미 정의되어 있지만, 실제 쿼리에서는 `latitude/longitude` 컬럼의 범위 검색만 사용하고 있어 Spatial Index가 활용되지 않는다.

### 적용 시 고려 사항

아티클의 내용을 프로젝트에 적용한다면 다음과 같은 변경이 필요하다:

- 쿼리에서 `location` POINT 컬럼과 `ST_Distance_Sphere()` 함수를 사용하여 Spatial Index 활용
- Java 레벨의 Haversine 필터링 로직 제거
- DB에서 거리 계산이 가능해지므로 거리순 정렬 기능 추가 가능

### 예상 효과

- **인덱스**: B-Tree (범위 검색) → R-Tree Spatial Index
- **거리 계산**: Java (매 요청 반복) → DB (최적화된 내장 함수)
- **메모리**: 후보 전체 로드 → 결과만 로드
- **정렬**: 불가 → 거리순 정렬 가능

이미 준비된 인프라(Spatial Index, POINT 컬럼)를 실제로 활용하는 것만으로 성능 개선과 기능 확장이 가능할 것으로 보인다.

---

## 용어 정리

- **Haversine**: 지구 곡면 위 두 점 사이의 거리를 계산하는 공식. GPS 좌표 간 실제 거리 계산에 사용
- **Bounding Box**: 원형 검색 영역을 감싸는 최소 직사각형. 사각형으로 1차 필터링 후 원으로 2차 필터링
- **Pruning**: 인덱스 탐색 시 불필요한 영역을 조기에 건너뛰는 최적화 기법
- **SRID 4326**: 좌표계 식별 번호. 4326은 GPS에서 사용하는 WGS84 좌표계를 의미. 미설정 시 Spatial Index 무효화
