package com.hoops.match.domain.vo;

import java.util.Objects;

public record MatchHost(Long id, String nickname) {

    public MatchHost {
        Objects.requireNonNull(id, "hostId must not be null");
        Objects.requireNonNull(nickname, "nickname must not be null");
    }
}
