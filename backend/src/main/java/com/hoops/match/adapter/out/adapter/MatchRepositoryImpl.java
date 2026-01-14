package com.hoops.match.adapter.out.adapter;

import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;
import com.hoops.match.adapter.out.MatchEntity;
import com.hoops.match.adapter.out.jpa.JpaMatchRepository;
import com.hoops.match.adapter.out.mapper.MatchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MatchRepositoryImpl implements MatchRepository {

    private final JpaMatchRepository jpaMatchRepository;

    @Override
    public Match save(Match match) {
        MatchEntity entity = MatchMapper.toEntity(match);
        MatchEntity savedEntity = jpaMatchRepository.save(entity);
        return MatchMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Match> findById(Long id) {
        return jpaMatchRepository.findById(id).map(MatchMapper::toDomain);
    }

    @Override
    public Optional<Match> findByIdWithLock(Long id) {
        return jpaMatchRepository.findByIdWithLock(id).map(MatchMapper::toDomain);
    }

    @Override
    public List<Match> findAllByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance) {
        // Bounding Box 계산
        // 위도 1도 ≈ 111km, 경도 1도 ≈ 111km * cos(latitude)
        BigDecimal latDelta = distance.divide(BigDecimal.valueOf(111000), 6, java.math.RoundingMode.HALF_UP);
        double cosLat = Math.cos(Math.toRadians(latitude.doubleValue()));
        BigDecimal lngDelta = distance.divide(BigDecimal.valueOf(111000 * cosLat), 6, java.math.RoundingMode.HALF_UP);

        BigDecimal minLat = latitude.subtract(latDelta);
        BigDecimal maxLat = latitude.add(latDelta);
        BigDecimal minLng = longitude.subtract(lngDelta);
        BigDecimal maxLng = longitude.add(lngDelta);

        // Bounding Box로 1차 필터링 후 Java에서 정확한 거리 계산
        double distanceMeters = distance.doubleValue();
        double centerLat = latitude.doubleValue();
        double centerLng = longitude.doubleValue();

        return jpaMatchRepository.findAllByLocationBoundingBoxOnly(minLat, maxLat, minLng, maxLng)
                .stream()
                .map(MatchMapper::toDomain)
                .filter(match -> calculateDistanceInMeters(
                        centerLat, centerLng,
                        match.getLatitude().doubleValue(),
                        match.getLongitude().doubleValue()) <= distanceMeters)
                .sorted((m1, m2) -> {
                    double d1 = calculateDistanceInMeters(centerLat, centerLng,
                            m1.getLatitude().doubleValue(), m1.getLongitude().doubleValue());
                    double d2 = calculateDistanceInMeters(centerLat, centerLng,
                            m2.getLatitude().doubleValue(), m2.getLongitude().doubleValue());
                    return Double.compare(d1, d2);
                })
                .toList();
    }

    /**
     * Haversine 공식을 사용한 두 지점 간 거리 계산 (미터)
     */
    private double calculateDistanceInMeters(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS = 6371000; // 지구 반지름 (미터)

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    @Override
    public List<Match> findMatchesToStart(LocalDate date, LocalTime time, List<MatchStatus> statuses) {
        return jpaMatchRepository.findMatchesToStart(date, time, statuses).stream()
                .map(MatchMapper::toDomain)
                .toList();
    }

    @Override
    public List<Match> findMatchesToEnd(LocalDate date, LocalTime time, MatchStatus status) {
        return jpaMatchRepository.findMatchesToEnd(date, time, status).stream()
                .map(MatchMapper::toDomain)
                .toList();
    }

    @Override
    public List<Match> findByHostId(Long hostId) {
        return jpaMatchRepository.findByHostIdOrderByMatchDateDesc(hostId).stream()
                .map(MatchMapper::toDomain)
                .toList();
    }

    @Override
    public List<Match> findActiveMatchesByHostId(Long hostId) {
        return jpaMatchRepository.findActiveMatchesByHostId(hostId).stream()
                .map(MatchMapper::toDomain)
                .toList();
    }

    @Override
    public List<Match> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaMatchRepository.findAllById(ids).stream()
                .map(MatchMapper::toDomain)
                .toList();
    }
}
