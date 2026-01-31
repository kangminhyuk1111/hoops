package com.hoops.match.domain.vo;

import com.hoops.match.domain.exception.InvalidSearchDistanceException;

import java.math.BigDecimal;

public enum SearchDistance {

    ONE(1),
    THREE(3),
    FIVE(5),
    TEN(10);

    private final int km;

    SearchDistance(int km) {
        this.km = km;
    }

    public int getKm() {
        return km;
    }

    public BigDecimal toMeters() {
        return BigDecimal.valueOf(km * 1000L);
    }

    public static SearchDistance from(int km) {
        for (SearchDistance distance : values()) {
            if (distance.km == km) {
                return distance;
            }
        }
        throw new InvalidSearchDistanceException(km);
    }
}
