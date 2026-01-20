package com.hoops.location.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class Location {

    private final Long id;
    private final Long userId;
    private final String alias;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final String address;
}
