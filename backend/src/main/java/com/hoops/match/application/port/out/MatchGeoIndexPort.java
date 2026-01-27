package com.hoops.match.application.port.out;

import java.math.BigDecimal;
import java.util.List;

/**
 * 매치 위치 검색용 Geo Index 포트
 * Redis GeoHash를 활용한 위치 기반 검색 인덱스
 */
public interface MatchGeoIndexPort {

    /**
     * 매치를 Geo Index에 추가
     */
    void addMatch(Long matchId, BigDecimal longitude, BigDecimal latitude);

    /**
     * 매치를 Geo Index에서 제거
     */
    void removeMatch(Long matchId);

    /**
     * 반경 내 매치 ID 목록 조회
     */
    List<Long> findMatchIdsWithinRadius(BigDecimal longitude, BigDecimal latitude, double radiusKm, int limit);

    /**
     * 모든 매치 ID 조회 (정합성 체크용)
     */
    List<Long> findAllMatchIds();

    /**
     * 전체 인덱스 초기화
     */
    void clearAll();
}
