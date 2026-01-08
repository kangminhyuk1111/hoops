package com.hoops.user.infrastructure.mapper;

import com.hoops.user.domain.User;
import com.hoops.user.infrastructure.UserEntity;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getProfileImage(),
                entity.getRating(),
                entity.getTotalMatches());
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return new UserEntity(
                domain.getEmail(),
                domain.getNickname(),
                domain.getProfileImage());
    }
}
