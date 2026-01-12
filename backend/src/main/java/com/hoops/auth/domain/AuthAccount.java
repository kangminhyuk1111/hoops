package com.hoops.auth.domain;

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
}
