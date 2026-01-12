package com.hoops.user.domain;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class User {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String profileImage;
    private final BigDecimal rating;
    private final Integer totalMatches;

    public User(Long id, String email, String nickname, String profileImage, BigDecimal rating,
            Integer totalMatches) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.rating = rating;
        this.totalMatches = totalMatches;
    }
}
