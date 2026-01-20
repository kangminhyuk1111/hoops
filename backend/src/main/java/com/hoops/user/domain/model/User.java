package com.hoops.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class User {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String profileImage;
    private final BigDecimal rating;
    private final Integer totalMatches;

    public static User createNew(String email, String nickname, String profileImage) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .profileImage(profileImage)
                .rating(BigDecimal.ZERO)
                .totalMatches(0)
                .build();
    }

    public User updateProfile(String nickname, String profileImage) {
        return new User(
                this.id,
                this.email,
                nickname != null ? nickname : this.nickname,
                profileImage != null ? profileImage : this.profileImage,
                this.rating,
                this.totalMatches
        );
    }
}
