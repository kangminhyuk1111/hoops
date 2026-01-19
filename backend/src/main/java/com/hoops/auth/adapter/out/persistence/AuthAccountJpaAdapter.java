package com.hoops.auth.adapter.out.persistence;

import com.hoops.auth.domain.repository.AuthAccountRepository;
import com.hoops.auth.domain.model.AuthAccount;
import com.hoops.auth.domain.vo.AuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuthAccountJpaAdapter implements AuthAccountRepository {

    private final SpringDataAuthAccountRepository repository;

    @Override
    public AuthAccount save(AuthAccount authAccount) {
        AuthAccountJpaEntity entity = toEntity(authAccount);
        AuthAccountJpaEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<AuthAccount> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByUserId(Long userId) {
        return repository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return repository.findByProviderAndProviderId(provider, providerId)
                .map(this::toDomain);
    }

    private AuthAccount toDomain(AuthAccountJpaEntity entity) {
        return new AuthAccount(
                entity.getId(),
                entity.getUserId(),
                entity.getProvider(),
                entity.getProviderId(),
                entity.getPasswordHash(),
                entity.getRefreshToken()
        );
    }

    private AuthAccountJpaEntity toEntity(AuthAccount domain) {
        return new AuthAccountJpaEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getProvider(),
                domain.getProviderId(),
                domain.getPasswordHash(),
                domain.getRefreshToken()
        );
    }
}
