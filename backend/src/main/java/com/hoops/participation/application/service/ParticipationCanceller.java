package com.hoops.participation.application.service;

import com.hoops.participation.application.port.out.MatchInfo;
import com.hoops.participation.application.port.out.MatchInfoPort;
import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.domain.vo.ParticipationStatus;
import com.hoops.participation.application.port.out.ParticipationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipationCanceller {

    private final ParticipationRepositoryPort participationRepository;
    private final MatchInfoPort matchInfoProvider;

    public void cancel(Participation participation, MatchInfo matchInfo, Long matchId) {
        boolean wasConfirmed = participation.getStatus() == ParticipationStatus.CONFIRMED;

        participationRepository.save(participation.cancel());

        if (wasConfirmed) {
            matchInfoProvider.removeParticipant(matchId);
        }
    }
}
