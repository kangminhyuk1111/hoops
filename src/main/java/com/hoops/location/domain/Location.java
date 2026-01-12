package com.hoops.location.domain;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Location {

    private final Long id;
    private final Long userId;
    private final String alias;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final String address;

    public Location(Long id, Long userId, String alias, BigDecimal latitude, BigDecimal longitude,
            String address) {
        this.id = id;
        this.userId = userId;
        this.alias = alias;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
}
