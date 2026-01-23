# Performance Report

## 테스트 구성

- **타겟**: `GET /api/matches?latitude=37.5665&longitude=126.9780&distance=50`
- **인프라**: EC2 단일 인스턴스 / MySQL 8.0 (Docker) / Spring Boot 3.x
- **도구**: k6 (부하), Prometheus + Grafana (모니터링)
- **VUser**: 0 → 10 → 30 → 50 → 100 (유지 60s) → 0 / 총 3분 30초
- **Threshold**: p(95) < 2,000ms, 에러율 < 10%

## k6 결과 리포트

### 1,000건 데이터 (Threshold PASS)

```
http_req_duration: avg=515ms  min=51ms  med=490ms  max=2.46s  p(90)=969ms  p(95)=1.14s
http_req_failed:   0.00%
http_reqs:         7,740  (36.7/s)
```

### 10,000건 데이터 (Threshold FAIL)

```
http_req_duration: avg=15.6s  min=339ms  med=13.2s  max=33.5s  p(90)=28.9s  p(95)=30.1s
http_req_failed:   0.89%
http_reqs:         784  (3.5/s)
```

## Bottleneck Analysis

### Breaking Point

VUser **50명** 시점에서 Latency가 **5,000ms 이상**으로 치솟기 시작하고, VUser **100명** 유지 구간에서는 TPS **3.5**, 평균 Latency **15,600ms**, p95 **30,180ms**까지 악화되었다.

### 병목 원인: DB 쿼리 (CPU/Connection Pool 아님)

- **CPU**: Grafana 모니터링 기준 EC2 CPU 사용률은 부하 중에도 70% 미만으로 여유 있음
- **Memory**: JVM 힙 사용률 안정적, GC pause 미미
- **DB Connection Pool**: Connection 고갈 현상 없음, 에러율 0.89%는 timeout에 의한 것
- **DB 쿼리**: 매 요청마다 10,000건 전체에 대해 Haversine 삼각함수(`SIN`, `COS`, `ACOS`) 연산을 수행하는 Full Table Scan이 병목

데이터 10배 증가(1,000→10,000)에 응답시간 30배 악화. 인덱스를 활용할 수 없는 함수 기반 WHERE 조건이므로 O(n) 이상의 성능 저하 패턴.

## 개선 방향

Spatial Index 적용으로 O(n) Full Scan → O(log n) 인덱스 탐색으로 전환 후 동일 조건 재테스트 예정.

## Grafana 대시보드

> 부하테스트 기간 Grafana 스크린샷: http://54.116.54.99:3001 (admin / hoops2024)
> 시간 범위를 "Last 1 hour"로 설정하여 테스트 구간 확인
