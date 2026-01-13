package com.hoops.acceptance.hooks;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Cucumber 시나리오 간 데이터베이스 정리
 *
 * 각 시나리오 시작 전에 데이터베이스를 초기화하여
 * 시나리오 간 데이터 격리를 보장합니다.
 */
public class DatabaseCleanupHook {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseCleanupHook(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Before(order = 0)
    public void cleanupDatabase() {
        // 외래키 제약조건 비활성화
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 테이블 데이터 삭제 (역순으로 - 참조하는 테이블부터)
        jdbcTemplate.execute("TRUNCATE TABLE participations");
        jdbcTemplate.execute("TRUNCATE TABLE notifications");
        jdbcTemplate.execute("TRUNCATE TABLE matches");
        jdbcTemplate.execute("TRUNCATE TABLE locations");
        jdbcTemplate.execute("TRUNCATE TABLE auth_accounts");
        jdbcTemplate.execute("TRUNCATE TABLE users");

        // 외래키 제약조건 활성화
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
