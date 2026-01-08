package com.hoops.participation.domain.repository;

import com.hoops.participation.domain.Participation;
import java.util.Optional;

public interface ParticipationRepository {
    Participation save(Participation participation);

    Optional<Participation> findById(Long id);
}
