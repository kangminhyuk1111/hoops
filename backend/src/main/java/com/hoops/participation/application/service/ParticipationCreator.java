package com.hoops.participation.application.service;

import com.hoops.common.event.ParticipationCreatedEvent;
import com.hoops.participation.application.port.in.ParticipateInMatchCommand;
import com.hoops.participation.application.port.out.MatchInfo;
import com.hoops.participation.application.port.out.ParticipationEventPublisher;
import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.domain.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipationCreator {

    private final ParticipationRepository participationRepository;
    private final ParticipationEventPublisher eventPublisher;

    public Participation create(ParticipateInMatchCommand command, MatchInfo matchInfo) {
        Participation participation = findOrCreateParticipation(command);
        publishCreatedEvent(participation, matchInfo);
        return participation;
    }

    private Participation findOrCreateParticipation(ParticipateInMatchCommand command) {
        return participationRepository
                .findCancelledParticipation(command.matchId(), command.userId())
                .map(cancelled -> participationRepository.save(cancelled.reactivate()))
                .orElseGet(() -> participationRepository.save(
                        Participation.createPending(command.matchId(), command.userId())));
    }

    private void publishCreatedEvent(Participation participation, MatchInfo matchInfo) {
        eventPublisher.publish(new ParticipationCreatedEvent(
                participation.getId(),
                participation.getMatchId(),
                participation.getUserId(),
                matchInfo.title()
        ));
    }
}
