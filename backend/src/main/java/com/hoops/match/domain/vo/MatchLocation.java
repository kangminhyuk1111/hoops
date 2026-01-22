package com.hoops.match.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

public record MatchLocation(BigDecimal latitude, BigDecimal longitude, String address) {

    public MatchLocation {
        Objects.requireNonNull(latitude, "latitude must not be null");
        Objects.requireNonNull(longitude, "longitude must not be null");
        Objects.requireNonNull(address, "address must not be null");
    }
}
