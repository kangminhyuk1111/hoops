package com.hoops.auth.adapter.out.persistence.repository;

import com.hoops.auth.adapter.out.persistence.entity.AuthAccountEntity;
import com.hoops.auth.domain.model.AuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for AuthAccount entity.
 */
public interface AuthAccountJpaRepository extends JpaRepository<AuthAccountEntity, Long> {

    Optional<AuthAccountEntity> findByUserId(Long userId);

    Optional<AuthAccountEntity> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
