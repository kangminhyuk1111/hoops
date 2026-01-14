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
        return jpaMatchRepository.findAllByLocation(latitude, longitude, distance).stream()
                .map(MatchMapper::toDomain)
                .toList();
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
