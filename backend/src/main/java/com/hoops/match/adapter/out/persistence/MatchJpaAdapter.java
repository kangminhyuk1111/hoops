package com.hoops.match.adapter.out.persistence;

import com.hoops.match.application.port.out.MatchRepositoryPort;
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
public class MatchJpaAdapter implements MatchRepositoryPort {

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
    public List<Match> findAllByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance, int page, int size) {
        BoundingBox box = BoundingBox.from(latitude, longitude, distance);
        int offset = page * size;

        return springDataMatchRepository.findAllByLocationWithDistance(
                        box.minLat(), box.maxLat(), box.minLng(), box.maxLng(),
                        latitude, longitude, distance, size, offset)
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

    @Override
    public List<Match> findAllSearchableMatches() {
        return springDataMatchRepository.findAllSearchableMatches().stream()
                .map(MatchMapper::toDomain)
                .toList();
    }

    @Override
    public List<Long> findSearchableMatchIds() {
        return springDataMatchRepository.findSearchableMatchIds();
    }
}
