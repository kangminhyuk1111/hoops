package com.hoops.location.domain.model;

import com.hoops.location.domain.exception.InvalidLocationNameException;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Location {

    private static final int MIN_NAME_LENGTH = 2;

    private final Long id;
    private final Long userId;
    private final String alias;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final String address;

    private Location(Long id, Long userId, String alias, BigDecimal latitude,
                     BigDecimal longitude, String address) {
        this.id = id;
        this.userId = userId;
        this.alias = alias;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    /**
     * 새로운 장소를 생성한다.
     * 도메인 불변식을 검증한다.
     */
    public static Location createNew(Long userId, String alias, BigDecimal latitude,
                                      BigDecimal longitude, String address) {
        validateAlias(alias);
        validateCoordinates(latitude, longitude);
        return new Location(null, userId, alias, latitude, longitude, address);
    }

    /**
     * 데이터베이스에서 복원할 때 사용한다.
     * 이미 검증된 데이터이므로 검증을 생략한다.
     */
    public static Location reconstitute(Long id, Long userId, String alias,
                                         BigDecimal latitude, BigDecimal longitude, String address) {
        return new Location(id, userId, alias, latitude, longitude, address);
    }

    private static void validateAlias(String alias) {
        if (alias == null || alias.trim().length() < MIN_NAME_LENGTH) {
            throw new InvalidLocationNameException(alias);
        }
    }

    private static void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Coordinates are required");
        }
    }
}
