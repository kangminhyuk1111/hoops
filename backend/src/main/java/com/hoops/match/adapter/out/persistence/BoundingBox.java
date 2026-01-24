package com.hoops.match.adapter.out.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 중심 좌표와 반경으로부터 계산된 Bounding Box.
 * B-Tree 인덱스를 활용한 위치 기반 쿼리의 사전 필터링에 사용.
 */
record BoundingBox(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng) {

    private static final BigDecimal METERS_PER_DEGREE = BigDecimal.valueOf(111000);

    static BoundingBox from(BigDecimal latitude, BigDecimal longitude, BigDecimal distanceMeters) {
        BigDecimal latDelta = distanceMeters.divide(METERS_PER_DEGREE, 6, RoundingMode.HALF_UP);
        double cosLat = Math.cos(Math.toRadians(latitude.doubleValue()));
        BigDecimal lngDelta = distanceMeters.divide(
                BigDecimal.valueOf(METERS_PER_DEGREE.doubleValue() * cosLat), 6, RoundingMode.HALF_UP);

        return new BoundingBox(
                latitude.subtract(latDelta),
                latitude.add(latDelta),
                longitude.subtract(lngDelta),
                longitude.add(lngDelta));
    }
}
