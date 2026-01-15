package com.hoops.integration.shedlock;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ShedLock 분산 락 통합 테스트
 *
 * MySQL Testcontainers를 사용하여 실제 DB 환경에서 락 동작을 검증한다.
 */
@Testcontainers
@DisplayName("ShedLock 분산 락 테스트")
class ShedLockIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private LockProvider lockProvider;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DataSource dataSource = createDataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);

        // shedlock 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS shedlock (
                name VARCHAR(64) NOT NULL PRIMARY KEY,
                lock_until TIMESTAMP(3) NOT NULL,
                locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                locked_by VARCHAR(255) NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);

        // 테스트 간 데이터 정리
        jdbcTemplate.execute("TRUNCATE TABLE shedlock");

        // LockProvider 생성
        lockProvider = new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(jdbcTemplate)
                        .usingDbTime()
                        .build()
        );
    }

    private DataSource createDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(mysql.getJdbcUrl());
        dataSource.setUsername(mysql.getUsername());
        dataSource.setPassword(mysql.getPassword());
        return dataSource;
    }

    @Test
    @DisplayName("락을 획득하면 shedlock 테이블에 정보가 저장된다")
    void whenLockAcquired_thenLockInfoStoredInTable() {
        // given
        String lockName = "testScheduler";
        LockConfiguration config = createLockConfiguration(lockName);

        // when
        Optional<SimpleLock> lock = lockProvider.lock(config);

        // then
        assertThat(lock).isPresent();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM shedlock WHERE name = ?",
                Integer.class,
                lockName
        );
        assertThat(count).isEqualTo(1);

        // cleanup
        lock.ifPresent(SimpleLock::unlock);
    }

    @Test
    @DisplayName("동일한 락은 하나만 획득할 수 있다")
    void whenLockAlreadyHeld_thenSecondLockFails() {
        // given
        String lockName = "updateMatchStatuses";
        LockConfiguration config = createLockConfiguration(lockName);

        // when
        Optional<SimpleLock> firstLock = lockProvider.lock(config);
        Optional<SimpleLock> secondLock = lockProvider.lock(config);

        // then
        assertThat(firstLock)
                .as("첫 번째 락 획득은 성공해야 함")
                .isPresent();
        assertThat(secondLock)
                .as("두 번째 락 획득은 실패해야 함 (이미 락이 점유됨)")
                .isEmpty();

        // cleanup
        firstLock.ifPresent(SimpleLock::unlock);
    }

    @Test
    @DisplayName("락 해제 후에는 다시 획득할 수 있다")
    void whenLockReleased_thenCanReacquire() {
        // given
        String lockName = "reacquireTest";
        // lockAtLeastFor를 0으로 설정하여 즉시 재획득 가능하도록 함
        LockConfiguration config = new LockConfiguration(
                Instant.now(),
                lockName,
                Duration.ofMinutes(5),   // lockAtMostFor
                Duration.ZERO            // lockAtLeastFor: 0으로 설정하여 즉시 해제 가능
        );

        // when
        Optional<SimpleLock> firstLock = lockProvider.lock(config);
        assertThat(firstLock).isPresent();

        // 락 해제
        firstLock.get().unlock();

        // 다시 획득 시도
        Optional<SimpleLock> secondLock = lockProvider.lock(config);

        // then
        assertThat(secondLock)
                .as("락 해제 후 다시 획득할 수 있어야 함")
                .isPresent();

        // cleanup
        secondLock.ifPresent(SimpleLock::unlock);
    }

    @Test
    @DisplayName("서로 다른 락은 동시에 획득할 수 있다")
    void whenDifferentLocks_thenBothCanBeAcquired() {
        // given
        String lockName1 = "scheduler1";
        String lockName2 = "scheduler2";

        // when
        Optional<SimpleLock> lock1 = lockProvider.lock(createLockConfiguration(lockName1));
        Optional<SimpleLock> lock2 = lockProvider.lock(createLockConfiguration(lockName2));

        // then
        assertThat(lock1)
                .as("첫 번째 스케줄러 락 획득")
                .isPresent();
        assertThat(lock2)
                .as("두 번째 스케줄러 락 획득 (다른 이름)")
                .isPresent();

        // cleanup
        lock1.ifPresent(SimpleLock::unlock);
        lock2.ifPresent(SimpleLock::unlock);
    }

    private LockConfiguration createLockConfiguration(String lockName) {
        return new LockConfiguration(
                Instant.now(),
                lockName,
                Duration.ofMinutes(5),   // lockAtMostFor
                Duration.ofSeconds(50)   // lockAtLeastFor
        );
    }
}
