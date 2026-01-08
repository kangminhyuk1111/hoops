package com.hoops.auth.infrastructure.jpa;

import com.hoops.auth.domain.AuthProvider;
import com.hoops.auth.infrastructure.AuthAccountEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAuthAccountRepository extends JpaRepository<AuthAccountEntity, Long> {
    Optional<AuthAccountEntity> findByUserId(Long userId);

    Optional<AuthAccountEntity> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
