package com.hoops.participation.infrastructure.adapter;

import com.hoops.participation.domain.Participation;
import com.hoops.participation.domain.ParticipationStatus;
import com.hoops.participation.domain.repository.ParticipationRepository;
import com.hoops.participation.infrastructure.ParticipationEntity;
import com.hoops.participation.infrastructure.jpa.JpaParticipationRepository;
import com.hoops.participation.infrastructure.mapper.ParticipationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.hoops.participation.domain.ParticipationStatus.CONFIRMED;
import static com.hoops.participation.domain.ParticipationStatus.PENDING;

@Repository
@RequiredArgsConstructor
public class ParticipationRepositoryImpl implements ParticipationRepository {

    private final JpaParticipationRepository jpaParticipationRepository;

    @Override
    public Participation save(Participation participation) {
        ParticipationEntity entity = ParticipationMapper.toEntity(participation);
        ParticipationEntity savedEntity = jpaParticipationRepository.save(entity);
        return ParticipationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Participation> findById(Long id) {
        return jpaParticipationRepository.findById(id).map(ParticipationMapper::toDomain);
    }

    @Override
    public boolean existsByMatchIdAndUserId(Long matchId, Long userId) {
        return jpaParticipationRepository.existsByMatchIdAndUserId(matchId, userId);
    }

    @Override
    public boolean existsActiveParticipation(Long matchId, Long userId) {
        return jpaParticipationRepository.existsByMatchIdAndUserIdAndStatusIn(
                matchId, userId, List.of(PENDING, CONFIRMED));
    }

    @Override
    public List<Participation> findByUserIdAndNotCancelled(Long userId) {
        return jpaParticipationRepository
                .findByUserIdAndStatusNot(userId, ParticipationStatus.CANCELLED)
                .stream()
                .map(ParticipationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Participation> findActiveParticipationsByUserId(Long userId) {
        return jpaParticipationRepository
                .findByUserIdAndStatusIn(userId, List.of(PENDING, CONFIRMED))
                .stream()
                .map(ParticipationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Participation> findByMatchIdAndNotCancelled(Long matchId) {
        return jpaParticipationRepository
                .findByMatchIdAndStatusNot(matchId, ParticipationStatus.CANCELLED)
                .stream()
                .map(ParticipationMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Participation> findCancelledParticipation(Long matchId, Long userId) {
        return jpaParticipationRepository
                .findByMatchIdAndUserIdAndStatus(matchId, userId, ParticipationStatus.CANCELLED)
                .map(ParticipationMapper::toDomain);
    }
}
