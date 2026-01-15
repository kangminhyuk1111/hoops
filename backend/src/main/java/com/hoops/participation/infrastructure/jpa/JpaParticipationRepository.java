package com.hoops.participation.infrastructure.jpa;

import com.hoops.participation.domain.ParticipationStatus;
import com.hoops.participation.infrastructure.ParticipationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaParticipationRepository extends JpaRepository<ParticipationEntity, Long> {

    boolean existsByMatchIdAndUserId(Long matchId, Long userId);

    boolean existsByMatchIdAndUserIdAndStatusIn(Long matchId, Long userId, List<ParticipationStatus> statuses);

    List<ParticipationEntity> findByUserIdAndStatusNot(Long userId, ParticipationStatus status);

    List<ParticipationEntity> findByUserIdAndStatusIn(Long userId, List<ParticipationStatus> statuses);

    List<ParticipationEntity> findByMatchIdAndStatusNot(Long matchId, ParticipationStatus status);

    Optional<ParticipationEntity> findByMatchIdAndUserIdAndStatus(Long matchId, Long userId, ParticipationStatus status);
}
