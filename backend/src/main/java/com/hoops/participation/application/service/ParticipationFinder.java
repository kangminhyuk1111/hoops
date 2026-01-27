package com.hoops.participation.application.service;

import com.hoops.participation.application.exception.ParticipationNotFoundException;
import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.application.port.out.ParticipationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ParticipationFinder {

    private final ParticipationRepositoryPort participationRepository;

    public Participation findById(Long participationId) {
        return participationRepository.findById(participationId)
                .orElseThrow(() -> new ParticipationNotFoundException(participationId));
    }

    public List<Participation> findByUserIdAndNotCancelled(Long userId) {
        return participationRepository.findByUserIdAndNotCancelled(userId);
    }

    public List<Participation> findByMatchIdAndNotCancelled(Long matchId) {
        return participationRepository.findByMatchIdAndNotCancelled(matchId);
    }
}
