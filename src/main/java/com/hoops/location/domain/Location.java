package com.hoops.location.domain;

import java.math.BigDecimal;

public class Location {

    private Long id;
    private Long userId;
    private String alias;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;

    public Location(Long id, Long userId, String alias, BigDecimal latitude, BigDecimal longitude,
            String address) {
        this.id = id;
        this.userId = userId;
        this.alias = alias;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    // Domain Logic - None strictly required yet as per YAGNI

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAlias() {
        return alias;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }
}
