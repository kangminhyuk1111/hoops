package com.hoops.common.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * ShedLock 설정
 *
 * 분산 환경에서 스케줄러 중복 실행을 방지하기 위한 설정.
 * DB 기반 락을 사용하여 하나의 인스턴스만 스케줄러를 실행하도록 보장한다.
 *
 * 주의: shedlock 테이블은 schema.sql에서 생성되어야 한다.
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
public class ShedLockConfig {

    /**
     * JDBC 기반 LockProvider 설정
     *
     * @param dataSource 데이터소스
     * @return LockProvider
     */
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime()  // DB 서버 시간 사용 (인스턴스 간 시간 차이 방지)
                        .build()
        );
    }
}
