package com.hoops.user.domain;

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
}
