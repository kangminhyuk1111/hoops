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

    private static final String DEFAULT_SECRET = "hoops-default-jwt-secret-key-for-development-only-do-not-use-in-production";
    private static final int MIN_SECRET_LENGTH = 32; // 256 bits
    private static final long DEFAULT_ACCESS_TOKEN_EXPIRY = 1800000L;      // 30분
    private static final long DEFAULT_REFRESH_TOKEN_EXPIRY = 1209600000L;  // 14일
    private static final long DEFAULT_TEMP_TOKEN_EXPIRY = 600000L;         // 10분

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            secret = DEFAULT_SECRET;
        } else if (secret.length() < MIN_SECRET_LENGTH) {
            secret = padSecret(secret);
        }
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

    private static String padSecret(String shortSecret) {
        StringBuilder padded = new StringBuilder(shortSecret);
        while (padded.length() < MIN_SECRET_LENGTH) {
            padded.append(shortSecret);
        }
        return padded.substring(0, Math.max(MIN_SECRET_LENGTH, shortSecret.length()));
    }
}
