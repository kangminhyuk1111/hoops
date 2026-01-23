import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://54.116.54.99:8080';

const errorRate = new Rate('errors');
const matchListDuration = new Trend('match_list_duration');
const healthDuration = new Trend('health_duration');

export const options = {
    stages: [
        { duration: '30s', target: 10 },   // Ramp up to 10 users
        { duration: '1m', target: 50 },    // Ramp up to 50 users
        { duration: '30s', target: 100 },  // Ramp up to 100 users
        { duration: '1m', target: 100 },   // Stay at 100 users
        { duration: '30s', target: 0 },    // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],  // 95% of requests < 2s
        errors: ['rate<0.1'],               // Error rate < 10%
    },
};

export default function () {
    // GET /actuator/health
    const healthRes = http.get(`${BASE_URL}/actuator/health`);
    healthDuration.add(healthRes.timings.duration);
    check(healthRes, {
        'health status 200': (r) => r.status === 200,
    }) || errorRate.add(1);

    sleep(0.5);

    // GET /api/matches (위치 기반 조회 - 서울 중심)
    const matchesRes = http.get(`${BASE_URL}/api/matches?latitude=37.5665&longitude=126.9780&distance=50`);
    matchListDuration.add(matchesRes.timings.duration);
    check(matchesRes, {
        'matches status 200': (r) => r.status === 200,
    }) || errorRate.add(1);

    sleep(0.5);

    // GET /api/matches (다른 위치 - 강남)
    const matchesRes2 = http.get(`${BASE_URL}/api/matches?latitude=37.4979&longitude=127.0276&distance=10`);
    check(matchesRes2, {
        'matches gangnam status 200': (r) => r.status === 200,
    }) || errorRate.add(1);

    sleep(0.5);

    // GET /api/locations
    const locationsRes = http.get(`${BASE_URL}/api/locations`);
    check(locationsRes, {
        'locations status 2xx': (r) => r.status >= 200 && r.status < 300,
    }) || errorRate.add(1);

    sleep(1);
}
