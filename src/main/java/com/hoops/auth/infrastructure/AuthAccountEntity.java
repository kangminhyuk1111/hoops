package com.hoops.auth.infrastructure;

import com.hoops.auth.domain.AuthProvider;
import com.hoops.common.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_accounts")
public class AuthAccountEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuthProvider provider;

    @Column
    private String providerId;

    @Column
    private String passwordHash;

    @Column(length = 500)
    private String refreshToken;

    protected AuthAccountEntity() {
    }

    public AuthAccountEntity(Long userId, AuthProvider provider, String providerId,
            String passwordHash, String refreshToken) {
        this.userId = userId;
        this.provider = provider;
        this.providerId = providerId;
        this.passwordHash = passwordHash;
        this.refreshToken = refreshToken;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

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
