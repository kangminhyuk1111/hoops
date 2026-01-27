package com.hoops.auth.application.port.out;

import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.vo.AuthProvider;

import java.util.Optional;

/**
 * AuthAccount 영속성 포트 인터페이스
 */
public interface AuthAccountRepositoryPort {

    AuthAccount save(AuthAccount authAccount);

    Optional<AuthAccount> findById(Long id);

    Optional<AuthAccount> findByUserId(Long userId);

    Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
