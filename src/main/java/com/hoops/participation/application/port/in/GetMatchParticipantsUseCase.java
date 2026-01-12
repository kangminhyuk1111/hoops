package com.hoops.participation.application.port.in;

import com.hoops.participation.domain.Participation;

import java.util.List;

public interface GetMatchParticipantsUseCase {
    List<Participation> getMatchParticipants(Long matchId);
}
