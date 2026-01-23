import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://54.116.54.99:8080';

export const options = {
    vus: 1,
    iterations: 1,
};

export default function () {
    const healthRes = http.get(`${BASE_URL}/actuator/health`);
    check(healthRes, {
        'health status 200': (r) => r.status === 200,
    });
    console.log(`Health: ${healthRes.status} - ${healthRes.body}`);

    const matchesRes = http.get(`${BASE_URL}/api/matches?latitude=37.5665&longitude=126.9780&distance=50`);
    check(matchesRes, {
        'matches status 200': (r) => r.status === 200,
    });
    console.log(`Matches: ${matchesRes.status} - ${matchesRes.body.substring(0, 100)}`);

    const locationsRes = http.get(`${BASE_URL}/api/locations`);
    check(locationsRes, {
        'locations status 2xx': (r) => r.status >= 200 && r.status < 300,
    });
    console.log(`Locations: ${locationsRes.status}`);
}
