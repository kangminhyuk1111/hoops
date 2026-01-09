package com.hoops.match.infrastructure.jpa;

import com.hoops.match.infrastructure.MatchEntity;
import java.math.BigDecimal;
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
}
