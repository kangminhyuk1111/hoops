package com.hoops.match.infrastructure.config;

import com.hoops.match.domain.policy.MatchPolicyValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Match 도메인 설정
 *
 * 순수 도메인 객체를 Spring Bean으로 등록합니다.
 */
@Configuration
public class MatchConfig {

    @Bean
    public MatchPolicyValidator matchPolicyValidator() {
        return new MatchPolicyValidator();
    }
}
