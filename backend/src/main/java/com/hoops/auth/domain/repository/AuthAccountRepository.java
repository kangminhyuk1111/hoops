package com.hoops.auth.domain.repository;

import com.hoops.auth.domain.AuthAccount;
import com.hoops.auth.domain.AuthProvider;
import java.util.Optional;

public interface AuthAccountRepository {
    AuthAccount save(AuthAccount authAccount);

    Optional<AuthAccount> findById(Long id);

    Optional<AuthAccount> findByUserId(Long userId);

    Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
