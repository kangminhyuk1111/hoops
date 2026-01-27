package com.hoops.participation.application.service;

import com.hoops.participation.application.port.in.ParticipateInMatchCommand;
import com.hoops.participation.application.port.out.MatchInfo;
import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.application.port.out.ParticipationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipationCreator {

    private final ParticipationRepositoryPort participationRepository;

    public Participation create(ParticipateInMatchCommand command, MatchInfo matchInfo) {
        return findOrCreateParticipation(command);
    }

    private Participation findOrCreateParticipation(ParticipateInMatchCommand command) {
        return participationRepository
                .findCancelledParticipation(command.matchId(), command.userId())
                .map(cancelled -> participationRepository.save(cancelled.reactivate()))
                .orElseGet(() -> participationRepository.save(
                        Participation.createPending(command.matchId(), command.userId())));
    }
}
