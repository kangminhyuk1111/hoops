import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://54.116.54.99:8080';

export const options = {
    stages: [
        { duration: '10s', target: 100 },
        { duration: '30s', target: 200 },
        { duration: '30s', target: 300 },
        { duration: '30s', target: 300 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.1'],
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/api/matches?latitude=37.5665&longitude=126.9780&distance=5`);

    check(res, {
        'status 200': (r) => r.status === 200,
    });
    // No sleep - maximum throughput test
}
