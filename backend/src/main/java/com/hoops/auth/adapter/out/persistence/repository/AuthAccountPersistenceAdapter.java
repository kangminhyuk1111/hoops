package com.hoops.auth.adapter.out.persistence.repository;

import com.hoops.auth.adapter.out.persistence.entity.AuthAccountEntity;
import com.hoops.auth.adapter.out.persistence.mapper.AuthMapper;
import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.model.AuthProvider;
import com.hoops.auth.domain.port.AuthAccountPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Persistence adapter for AuthAccount port implementation.
 */
@Repository
@RequiredArgsConstructor
public class AuthAccountPersistenceAdapter implements AuthAccountPort {

    private final AuthAccountJpaRepository authAccountJpaRepository;

    @Override
    public AuthAccount save(AuthAccount authAccount) {
        AuthAccountEntity entity = AuthMapper.toEntity(authAccount);
        AuthAccountEntity savedEntity = authAccountJpaRepository.save(entity);
        return AuthMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AuthAccount> findById(Long id) {
        return authAccountJpaRepository.findById(id).map(AuthMapper::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByUserId(Long userId) {
        return authAccountJpaRepository.findByUserId(userId).map(AuthMapper::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return authAccountJpaRepository.findByProviderAndProviderId(provider, providerId)
                .map(AuthMapper::toDomain);
    }
}
