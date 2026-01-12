package com.hoops.auth.infrastructure.mapper;

import com.hoops.auth.domain.AuthAccount;
import com.hoops.auth.infrastructure.AuthAccountEntity;

public class AuthMapper {

    public static AuthAccount toDomain(AuthAccountEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AuthAccount(
                entity.getId(),
                entity.getUserId(),
                entity.getProvider(),
                entity.getProviderId(),
                entity.getPasswordHash(),
                entity.getRefreshToken());
    }

    public static AuthAccountEntity toEntity(AuthAccount domain) {
        if (domain == null) {
            return null;
        }
        return new AuthAccountEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getProvider(),
                domain.getProviderId(),
                domain.getPasswordHash(),
                domain.getRefreshToken());
    }
}
