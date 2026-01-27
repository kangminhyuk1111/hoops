import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://54.116.54.99:8080';

// Stress Test: 계속 VU를 증가시켜 한계점 찾기
export const options = {
    stages: [
        { duration: '30s', target: 100 },   // Warm-up
        { duration: '30s', target: 200 },
        { duration: '30s', target: 300 },
        { duration: '30s', target: 400 },
        { duration: '30s', target: 500 },
        { duration: '30s', target: 600 },
        { duration: '30s', target: 700 },
        { duration: '30s', target: 800 },
        { duration: '30s', target: 0 },     // Ramp-down
    ],
    thresholds: {
        http_req_failed: ['rate<0.5'],  // 50% 에러까지 허용 (한계 찾기용)
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/api/matches?latitude=37.5665&longitude=126.9780&distance=5`);

    check(res, {
        'status 200': (r) => r.status === 200,
    });
}
