package com.hoops.auth.domain.repository;

import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.vo.AuthProvider;

import java.util.Optional;

/**
 * AuthAccount 저장소 인터페이스 (DDD Repository)
 */
public interface AuthAccountRepository {

    AuthAccount save(AuthAccount authAccount);

    Optional<AuthAccount> findById(Long id);

    Optional<AuthAccount> findByUserId(Long userId);

    Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
