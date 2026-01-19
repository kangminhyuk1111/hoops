package com.hoops.auth.domain.model;

import com.hoops.auth.domain.vo.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthAccount {

    private final Long id;
    private final Long userId;
    private final AuthProvider provider;
    private final String providerId;
    private final String passwordHash;
    private final String refreshToken;

    public static AuthAccount create(Long userId, AuthProvider provider, String providerId, String refreshToken) {
        return AuthAccount.builder()
                .userId(userId)
                .provider(provider)
                .providerId(providerId)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthAccount withRefreshToken(String newRefreshToken) {
        return AuthAccount.builder()
                .id(this.id)
                .userId(this.userId)
                .provider(this.provider)
                .providerId(this.providerId)
                .passwordHash(this.passwordHash)
                .refreshToken(newRefreshToken)
                .build();
    }
}
