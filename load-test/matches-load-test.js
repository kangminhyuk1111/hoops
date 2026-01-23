import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://54.116.54.99:8080';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '30s', target: 30 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.1'],
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/api/matches?latitude=37.5665&longitude=126.9780&distance=50`);

    check(res, {
        'status 200': (r) => r.status === 200,
    });

    sleep(1);
}
