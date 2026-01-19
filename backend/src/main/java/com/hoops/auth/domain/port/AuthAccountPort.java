package com.hoops.auth.domain.port;

import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.model.AuthProvider;
import java.util.Optional;

/**
 * Outbound port for AuthAccount persistence operations.
 */
public interface AuthAccountPort {

    AuthAccount save(AuthAccount authAccount);

    Optional<AuthAccount> findById(Long id);

    Optional<AuthAccount> findByUserId(Long userId);

    Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
