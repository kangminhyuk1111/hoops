package com.hoops.match.application.port.out;

import java.math.BigDecimal;
import java.util.List;

/**
 * 매치 위치 검색용 Geo Index 포트
 * Redis GeoHash를 활용한 위치 기반 검색 인덱스
 */
public interface MatchGeoIndexPort {

    /**
     * Geo Index에 저장할 매치 정보
     */
    record GeoIndexEntry(Long matchId, BigDecimal longitude, BigDecimal latitude) {}

    /**
     * 매치를 Geo Index에 추가
     */
    void addMatch(Long matchId, BigDecimal longitude, BigDecimal latitude);

    /**
     * 여러 매치를 Geo Index에 벌크 추가 (Pipeline 사용)
     */
    void addMatchesBulk(List<GeoIndexEntry> entries);

    /**
     * 매치를 Geo Index에서 제거
     */
    void removeMatch(Long matchId);

    /**
     * 거리 정보를 포함한 Geo 검색 결과
     */
    record GeoMatchResult(Long matchId, double distanceKm) {}

    /**
     * 반경 내 매치 ID 목록 조회 (페이지네이션 지원)
     *
     * @param longitude 중심점 경도
     * @param latitude 중심점 위도
     * @param radiusKm 반경 (km)
     * @param offset 시작 위치 (skip할 개수)
     * @param limit 조회할 최대 개수
     * @return 거리순으로 정렬된 매치 ID 목록
     */
    List<Long> findMatchIdsWithinRadius(BigDecimal longitude, BigDecimal latitude, double radiusKm, int offset, int limit);

    /**
     * 반경 내 매치 목록 조회 (거리 정보 포함, 페이지네이션 지원)
     *
     * @param longitude 중심점 경도
     * @param latitude 중심점 위도
     * @param radiusKm 반경 (km)
     * @param offset 시작 위치 (skip할 개수)
     * @param limit 조회할 최대 개수
     * @return 거리순으로 정렬된 매치 ID + 거리 목록
     */
    List<GeoMatchResult> findMatchesWithinRadius(BigDecimal longitude, BigDecimal latitude, double radiusKm, int offset, int limit);

    /**
     * 반경 내 매치 개수 조회
     *
     * @param longitude 중심점 경도
     * @param latitude 중심점 위도
     * @param radiusKm 반경 (km)
     * @return 반경 내 매치 개수
     */
    long countMatchesWithinRadius(BigDecimal longitude, BigDecimal latitude, double radiusKm);

    /**
     * 모든 매치 ID 조회 (정합성 체크용)
     */
    List<Long> findAllMatchIds();

    /**
     * 전체 인덱스 초기화
     */
    void clearAll();
}
