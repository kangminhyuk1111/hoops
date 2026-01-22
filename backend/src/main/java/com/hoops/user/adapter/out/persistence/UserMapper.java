package com.hoops.user.adapter.out.persistence;

import com.hoops.user.domain.model.User;

public class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.reconstitute(
                entity.getId(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getProfileImage(),
                entity.getRating(),
                entity.getTotalMatches());
    }

    public static UserJpaEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return new UserJpaEntity(
                domain.getEmail(),
                domain.getNickname(),
                domain.getProfileImage());
    }
}
