package com.hoops.user.adapter.in.web.dto;

import com.hoops.user.domain.model.User;
import java.math.BigDecimal;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String profileImage,
        BigDecimal rating,
        Integer totalMatches
) {
    public static UserResponse of(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage(),
                user.getRating(),
                user.getTotalMatches()
        );
    }
}
