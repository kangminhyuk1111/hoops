package com.hoops.participation.application.service;

import com.hoops.participation.application.port.in.GetMatchParticipantsUseCase;
import com.hoops.participation.application.port.in.GetMyParticipationsUseCase;
import com.hoops.participation.application.port.out.MatchInfoPort;
import com.hoops.participation.domain.model.Participation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParticipationQueryService implements GetMyParticipationsUseCase, GetMatchParticipantsUseCase {

    private final MatchInfoPort matchInfoPort;
    private final ParticipationFinder finder;

    @Override
    public List<Participation> getMyParticipations(Long userId) {
        return finder.findByUserIdAndNotCancelled(userId);
    }

    @Override
    public List<Participation> getMatchParticipants(Long matchId) {
        matchInfoPort.getMatchInfo(matchId);
        return finder.findByMatchIdAndNotCancelled(matchId);
    }
}
