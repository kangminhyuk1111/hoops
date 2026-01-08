package com.hoops.auth.infrastructure.adapter;

import com.hoops.auth.domain.AuthAccount;
import com.hoops.auth.domain.AuthProvider;
import com.hoops.auth.domain.repository.AuthAccountRepository;
import com.hoops.auth.infrastructure.AuthAccountEntity;
import com.hoops.auth.infrastructure.jpa.JpaAuthAccountRepository;
import com.hoops.auth.infrastructure.mapper.AuthMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AuthAccountRepositoryImpl implements AuthAccountRepository {

    private final JpaAuthAccountRepository jpaAuthAccountRepository;

    public AuthAccountRepositoryImpl(JpaAuthAccountRepository jpaAuthAccountRepository) {
        this.jpaAuthAccountRepository = jpaAuthAccountRepository;
    }

    @Override
    public AuthAccount save(AuthAccount authAccount) {
        AuthAccountEntity entity = AuthMapper.toEntity(authAccount);
        AuthAccountEntity savedEntity = jpaAuthAccountRepository.save(entity);
        return AuthMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AuthAccount> findById(Long id) {
        return jpaAuthAccountRepository.findById(id).map(AuthMapper::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByUserId(Long userId) {
        return jpaAuthAccountRepository.findByUserId(userId).map(AuthMapper::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return jpaAuthAccountRepository.findByProviderAndProviderId(provider, providerId)
                .map(AuthMapper::toDomain);
    }
}
