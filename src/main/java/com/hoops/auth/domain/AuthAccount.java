package com.hoops.auth.domain;

public class AuthAccount {

    private Long id;
    private Long userId;
    private AuthProvider provider;
    private String providerId;
    private String passwordHash;
    private String refreshToken;

    public AuthAccount(Long id, Long userId, AuthProvider provider, String providerId,
            String passwordHash, String refreshToken) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerId = providerId;
        this.passwordHash = passwordHash;
        this.refreshToken = refreshToken;
    }

    // Domain Logic - None strictly required yet as per YAGNI

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
