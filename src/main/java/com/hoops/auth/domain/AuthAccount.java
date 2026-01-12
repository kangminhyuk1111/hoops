package com.hoops.auth.domain;

import lombok.Getter;

@Getter
public class AuthAccount {

    private final Long id;
    private final Long userId;
    private final AuthProvider provider;
    private final String providerId;
    private final String passwordHash;
    private final String refreshToken;

    public AuthAccount(Long id, Long userId, AuthProvider provider, String providerId,
            String passwordHash, String refreshToken) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerId = providerId;
        this.passwordHash = passwordHash;
        this.refreshToken = refreshToken;
    }
}
