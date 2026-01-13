package com.hoops.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Spring Retry 설정
 *
 * 낙관적 락 충돌 시 자동 재시도를 위한 설정입니다.
 */
@Configuration
@EnableRetry
public class RetryConfig {
}
