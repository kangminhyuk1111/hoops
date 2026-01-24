package com.hoops.match.adapter.out.persistence;

import com.hoops.match.domain.repository.MatchRepository;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MatchJpaAdapter implements MatchRepository {

    private final SpringDataMatchRepository springDataMatchRepository;

    @Override
    public Match save(Match match) {
        MatchJpaEntity entity = MatchMapper.toEntity(match);
        MatchJpaEntity savedEntity = springDataMatchRepository.save(entity);
        return MatchMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Match> findById(Long id) {
        return springDataMatchRepository.findById(id).map(MatchMapper::toDomain);
    }

    @Override
    public Optional<Match> findByIdWithLock(Long id) {
        return springDataMatchRepository.findByIdWithLock(id).map(MatchMapper::toDomain);
    }

    @Override
    public List<Match> findAllByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance) {
        BigDecimal latDelta = distance.divide(BigDecimal.valueOf(111000), 6, java.math.RoundingMode.HALF_UP);
        double cosLat = Math.cos(Math.toRadians(latitude.doubleValue()));
        BigDecimal lngDelta = distance.divide(BigDecimal.valueOf(111000 * cosLat), 6, java.math.RoundingMode.HALF_UP);

        BigDecimal minLat = latitude.subtract(latDelta);
        BigDecimal maxLat = latitude.add(latDelta);
        BigDecimal minLng = longitude.subtract(lngDelta);
        BigDecimal maxLng = longitude.add(lngDelta);

        return springDataMatchRepository.findAllByLocationWithDistance(
                        minLat, maxLat, minLng, maxLng, latitude, longitude, distance)
                .stream()
                .map(MatchMapper::toDomain)
                .toList();
    }

    @Override
    public List<Match> findMatchesToStart(LocalDate date, LocalTime time, List<MatchStatus> statuses) {
        return springDataMatchRepository.findMatchesToStart(date, time, statuses).stream()
                .map(MatchMapper::toDomain)
                .toList();
    }

    @Override
    public List<Match> findMatchesToEnd(LocalDate date, LocalTime time, MatchStatus status) {
        return springDataMatchRepository.findMatchesToEnd(date, time, status).stream()
                .map(MatchMapper::toDomain)
                .toList();
    }

    @Override
    public List<Match> findByHostId(Long hostId) {
        return springDataMatchRepository.findByHostIdOrderByMatchDateDesc(hostId).stream()
                .map(MatchMapper::toDomain)
                .toList();
    }

    @Override
    public List<Match> findActiveMatchesByHostId(Long hostId) {
        return springDataMatchRepository.findActiveMatchesByHostId(hostId).stream()
                .map(MatchMapper::toDomain)
                .toList();
    }

    @Override
    public List<Match> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return springDataMatchRepository.findAllById(ids).stream()
                .map(MatchMapper::toDomain)
                .toList();
    }
}
