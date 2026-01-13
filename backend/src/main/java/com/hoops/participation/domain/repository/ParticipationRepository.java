package com.hoops.participation.domain.repository;

import com.hoops.participation.domain.Participation;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository {
    Participation save(Participation participation);

    Optional<Participation> findById(Long id);

    boolean existsByMatchIdAndUserId(Long matchId, Long userId);

    List<Participation> findByUserIdAndNotCancelled(Long userId);

    List<Participation> findByMatchIdAndNotCancelled(Long matchId);
}
