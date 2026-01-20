package com.hoops.participation.adapter.out.persistence;

import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.domain.vo.ParticipationStatus;
import com.hoops.participation.domain.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.hoops.participation.domain.vo.ParticipationStatus.CONFIRMED;
import static com.hoops.participation.domain.vo.ParticipationStatus.PENDING;

@Repository
@RequiredArgsConstructor
public class ParticipationJpaAdapter implements ParticipationRepository {

    private final SpringDataParticipationRepository springDataParticipationRepository;

    @Override
    public Participation save(Participation participation) {
        ParticipationJpaEntity entity = ParticipationMapper.toEntity(participation);
        ParticipationJpaEntity savedEntity = springDataParticipationRepository.save(entity);
        return ParticipationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Participation> findById(Long id) {
        return springDataParticipationRepository.findById(id).map(ParticipationMapper::toDomain);
    }

    @Override
    public boolean existsByMatchIdAndUserId(Long matchId, Long userId) {
        return springDataParticipationRepository.existsByMatchIdAndUserId(matchId, userId);
    }

    @Override
    public boolean existsActiveParticipation(Long matchId, Long userId) {
        return springDataParticipationRepository.existsByMatchIdAndUserIdAndStatusIn(
                matchId, userId, List.of(PENDING, CONFIRMED));
    }

    @Override
    public List<Participation> findByUserIdAndNotCancelled(Long userId) {
        return springDataParticipationRepository
                .findByUserIdAndStatusNot(userId, ParticipationStatus.CANCELLED)
                .stream()
                .map(ParticipationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Participation> findActiveParticipationsByUserId(Long userId) {
        return springDataParticipationRepository
                .findByUserIdAndStatusIn(userId, List.of(PENDING, CONFIRMED))
                .stream()
                .map(ParticipationMapper::toDomain)
                .toList();
    }

    @Override
    public List<Participation> findByMatchIdAndNotCancelled(Long matchId) {
        return springDataParticipationRepository
                .findByMatchIdAndStatusNot(matchId, ParticipationStatus.CANCELLED)
                .stream()
                .map(ParticipationMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Participation> findCancelledParticipation(Long matchId, Long userId) {
        return springDataParticipationRepository
                .findByMatchIdAndUserIdAndStatus(matchId, userId, ParticipationStatus.CANCELLED)
                .map(ParticipationMapper::toDomain);
    }
}
