package com.hoops.auth.adapter.out.persistence;

import com.hoops.auth.domain.vo.AuthProvider;
import com.hoops.common.infrastructure.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthAccountJpaEntity extends BaseTimeEntity {

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

    public AuthAccountJpaEntity(Long userId, AuthProvider provider, String providerId,
            String passwordHash, String refreshToken) {
        this.userId = userId;
        this.provider = provider;
        this.providerId = providerId;
        this.passwordHash = passwordHash;
        this.refreshToken = refreshToken;
    }

    public AuthAccountJpaEntity(Long id, Long userId, AuthProvider provider, String providerId,
            String passwordHash, String refreshToken) {
        this.id = id;
        this.userId = userId;
        this.provider = provider;
        this.providerId = providerId;
        this.passwordHash = passwordHash;
        this.refreshToken = refreshToken;
    }
}
