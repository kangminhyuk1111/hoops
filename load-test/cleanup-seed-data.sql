-- Load Test Seed Data Cleanup
DELETE FROM matches WHERE title LIKE '부하테스트 매치%';
DELETE FROM locations WHERE alias LIKE '%농구장' OR alias LIKE '%체육관' OR alias LIKE '%코트';
DELETE FROM users WHERE email LIKE 'loadtest%';

SELECT '=== Cleanup Complete ===' AS info;
SELECT COUNT(*) AS remaining_matches FROM matches;
SELECT COUNT(*) AS remaining_users FROM users;
