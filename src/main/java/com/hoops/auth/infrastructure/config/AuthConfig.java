package com.hoops.auth.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 인증 관련 설정
 */
@Configuration
@EnableConfigurationProperties(KakaoOAuthProperties.class)
public class AuthConfig {
}
