package com.hoops.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        Long accessTokenExpiry,
        Long refreshTokenExpiry,
        Long tempTokenExpiry
) {

    private static final long DEFAULT_ACCESS_TOKEN_EXPIRY = 1800000L;      // 30분
    private static final long DEFAULT_REFRESH_TOKEN_EXPIRY = 1209600000L;  // 14일
    private static final long DEFAULT_TEMP_TOKEN_EXPIRY = 600000L;         // 10분

    public JwtProperties {
        if (accessTokenExpiry == null) {
            accessTokenExpiry = DEFAULT_ACCESS_TOKEN_EXPIRY;
        }
        if (refreshTokenExpiry == null) {
            refreshTokenExpiry = DEFAULT_REFRESH_TOKEN_EXPIRY;
        }
        if (tempTokenExpiry == null) {
            tempTokenExpiry = DEFAULT_TEMP_TOKEN_EXPIRY;
        }
    }
}
