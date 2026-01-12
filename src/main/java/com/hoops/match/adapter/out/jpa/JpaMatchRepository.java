package com.hoops.match.adapter.out.jpa;

import com.hoops.match.adapter.out.MatchEntity;
import com.hoops.match.domain.MatchStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaMatchRepository extends JpaRepository<MatchEntity, Long> {

    @Query(value = "SELECT * FROM matches m WHERE " +
            "ST_Distance_Sphere(POINT(:longitude, :latitude), POINT(m.longitude, m.latitude)) <= :distance " +
            "ORDER BY ST_Distance_Sphere(POINT(:longitude, :latitude), POINT(m.longitude, m.latitude)) ASC",
            nativeQuery = true)
    List<MatchEntity> findAllByLocation(@Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("distance") BigDecimal distance);

    @Query("SELECT m FROM MatchEntity m WHERE m.status IN :statuses " +
            "AND (m.matchDate < :date OR (m.matchDate = :date AND m.startTime <= :time))")
    List<MatchEntity> findMatchesToStart(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("statuses") List<MatchStatus> statuses);

    @Query("SELECT m FROM MatchEntity m WHERE m.status = :status " +
            "AND (m.matchDate < :date OR (m.matchDate = :date AND m.endTime <= :time))")
    List<MatchEntity> findMatchesToEnd(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time,
            @Param("status") MatchStatus status);

    List<MatchEntity> findByHostIdOrderByMatchDateDesc(Long hostId);
}
