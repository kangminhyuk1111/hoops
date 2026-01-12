package com.hoops.participation.application.port.in;

import com.hoops.participation.domain.Participation;

import java.util.List;

public interface GetMyParticipationsUseCase {
    List<Participation> getMyParticipations(Long userId);
}
