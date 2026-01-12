package com.hoops.participation.infrastructure.jpa;

import com.hoops.participation.domain.ParticipationStatus;
import com.hoops.participation.infrastructure.ParticipationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaParticipationRepository extends JpaRepository<ParticipationEntity, Long> {

    boolean existsByMatchIdAndUserId(Long matchId, Long userId);

    List<ParticipationEntity> findByUserIdAndStatusNot(Long userId, ParticipationStatus status);

    List<ParticipationEntity> findByMatchIdAndStatusNot(Long matchId, ParticipationStatus status);
}
