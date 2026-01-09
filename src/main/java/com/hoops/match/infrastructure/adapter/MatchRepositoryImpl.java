package com.hoops.match.infrastructure.adapter;

import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.infrastructure.MatchEntity;
import com.hoops.match.infrastructure.jpa.JpaMatchRepository;
import com.hoops.match.infrastructure.mapper.MatchMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MatchRepositoryImpl implements MatchRepository {

    private final JpaMatchRepository jpaMatchRepository;

    public MatchRepositoryImpl(JpaMatchRepository jpaMatchRepository) {
        this.jpaMatchRepository = jpaMatchRepository;
    }

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
    public List<Match> findAllByLocation(BigDecimal latitude, BigDecimal longitude, BigDecimal distance) {
        return jpaMatchRepository.findAllByLocation(latitude, longitude, distance).stream()
                .map(MatchMapper::toDomain)
                .toList();
    }
}
