package com.hoops.auth.application.port.out;

import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.vo.AuthProvider;

import java.util.Optional;

/**
 * AuthAccount 영속성 Port
 */
public interface AuthAccountPort {

    AuthAccount save(AuthAccount authAccount);

    Optional<AuthAccount> findById(Long id);

    Optional<AuthAccount> findByUserId(Long userId);

    Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
