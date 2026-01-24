package com.hoops.match.adapter.out.persistence;

import com.hoops.match.domain.vo.MatchStatus;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataMatchRepository extends JpaRepository<MatchJpaEntity, Long> {

    /**
     * 비관적 락을 사용하여 경기 조회
     * 참가자 수 변경 시 Race Condition 방지용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchJpaEntity m WHERE m.id = :matchId")
    Optional<MatchJpaEntity> findByIdWithLock(@Param("matchId") Long matchId);

    /**
     * Spatial Index(R-Tree)를 활용한 위치 기반 경기 검색
     *
     * 최적화 전략:
     * 1. ST_Within + Bounding Box 폴리곤으로 R-Tree 인덱스 활용 (O(log n) 탐색)
     * 2. ST_Distance_Sphere로 축소된 후보군에 대해 정확한 원형 거리 필터링
     *
     * @param boundingBoxPolygon Bounding Box WKT 문자열 (SRID 4326)
     * @param latitude 중심점 위도
     * @param longitude 중심점 경도
     * @param distance 검색 반경 (미터)
     */
    @Query(value = """
            SELECT * FROM matches m
            WHERE ST_Within(
                m.location,
                ST_GeomFromText(:boundingBoxPolygon, 4326)
            )
            AND m.status NOT IN ('CANCELLED', 'ENDED')
            AND ST_Distance_Sphere(m.location, ST_SRID(POINT(:longitude, :latitude), 4326)) <= :distance
            ORDER BY m.match_date ASC, m.start_time ASC
            """,
            nativeQuery = true)
    List<MatchJpaEntity> findAllByLocationWithSpatialIndex(
            @Param("boundingBoxPolygon") String boundingBoxPolygon,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("distance") BigDecimal distance);

    /**
     * H2 호환 위치 기반 경기 검색 (테스트용)
     * CANCELLED, ENDED 상태 경기 제외
     * 경기 시작 시간순 정렬
     */
    @Query(value = """
            SELECT * FROM matches m
            WHERE m.latitude BETWEEN :minLat AND :maxLat
            AND m.longitude BETWEEN :minLng AND :maxLng
            AND m.status NOT IN ('CANCELLED', 'ENDED')
            ORDER BY m.match_date ASC, m.start_time ASC
            """,
            nativeQuery = true)
    List<MatchJpaEntity> findAllByLocationBoundingBoxOnly(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng);

    @Query("SELECT m FROM MatchJpaEntity m WHERE m.status IN :statuses " +
            "AND (m.matchDate < :date OR (m.matchDate = :date AND m.startTime <= :time))")
    List<MatchJpaEntity> findMatchesToStart(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("statuses") List<MatchStatus> statuses);

    @Query("SELECT m FROM MatchJpaEntity m WHERE m.status = :status " +
            "AND (m.matchDate < :date OR (m.matchDate = :date AND m.endTime <= :time))")
    List<MatchJpaEntity> findMatchesToEnd(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("status") MatchStatus status);

    @Query("SELECT m FROM MatchJpaEntity m WHERE m.hostId = :hostId ORDER BY m.matchDate DESC, m.startTime DESC")
    List<MatchJpaEntity> findByHostIdOrderByMatchDateDesc(@Param("hostId") Long hostId);

    @Query("SELECT m FROM MatchJpaEntity m WHERE m.hostId = :hostId " +
            "AND m.status NOT IN ('CANCELLED', 'ENDED')")
    List<MatchJpaEntity> findActiveMatchesByHostId(@Param("hostId") Long hostId);
}
