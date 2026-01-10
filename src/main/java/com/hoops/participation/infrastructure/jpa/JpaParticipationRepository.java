package com.hoops.participation.infrastructure.jpa;

import com.hoops.participation.infrastructure.ParticipationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaParticipationRepository extends JpaRepository<ParticipationEntity, Long> {

    boolean existsByMatchIdAndUserId(Long matchId, Long userId);
}
