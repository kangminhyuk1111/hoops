# Performance Report

## 테스트 구성

- **타겟**: `GET /api/matches?latitude=37.5665&longitude=126.9780&distance=50`
- **인프라**: EC2 단일 인스턴스 / MySQL 8.0 (Docker) / Spring Boot 3.x
- **도구**: k6 (부하), Prometheus + Grafana (모니터링)
- **VUser**: 0 → 10 → 30 → 50 → 100 (유지 60s) → 0 / 총 3분 30초
- **Threshold**: p(95) < 2,000ms, 에러율 < 10%

## 결과 비교

| 지표 | 1,000건 | 10,000건 | 변화 |
|------|---------|----------|------|
| p95 응답시간 | 1,140ms | 30,180ms | 26x 악화 |
| 평균 응답시간 | 515ms | 15,600ms | 30x 악화 |
| RPS | 36.7 | 3.5 | 10x 감소 |
| 에러율 | 0% | 0.89% | 에러 발생 |
| Threshold | PASS | **FAIL** | - |

## Bottleneck Analysis

**병목**: DB 쿼리 (Haversine 거리 계산)

데이터 10배 증가에 응답시간 30배 악화 = O(n) 이상의 성능 저하 패턴.

원인: 매치 조회 시 Haversine 공식(`SIN`, `COS`, `ACOS`)을 **전체 row에 대해 계산**하는 Full Table Scan 발생. 인덱스 활용이 불가능한 함수 기반 WHERE 조건이므로 데이터 증가에 비례해 선형 이상으로 느려짐.

CPU/Memory/Connection Pool은 여유 있음. 병목은 순수하게 **쿼리 연산 비용**.

## 개선 방향

Spatial Index 적용으로 O(n) Full Scan → O(log n) 인덱스 탐색으로 전환 후 동일 조건 재테스트 예정.
