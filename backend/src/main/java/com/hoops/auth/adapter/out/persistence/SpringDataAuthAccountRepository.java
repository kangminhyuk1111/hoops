package com.hoops.auth.adapter.out.persistence;

import com.hoops.auth.domain.vo.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataAuthAccountRepository extends JpaRepository<AuthAccountJpaEntity, Long> {

    Optional<AuthAccountJpaEntity> findByUserId(Long userId);

    Optional<AuthAccountJpaEntity> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
