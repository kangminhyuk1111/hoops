package com.hoops.participation.application.service;

import com.hoops.common.event.ParticipationCancelledEvent;
import com.hoops.participation.application.port.out.MatchInfo;
import com.hoops.participation.application.port.out.MatchInfoPort;
import com.hoops.participation.application.port.out.ParticipationEventPublisher;
import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.domain.vo.ParticipationStatus;
import com.hoops.participation.domain.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ParticipationCanceller {

    private final ParticipationRepository participationRepository;
    private final MatchInfoPort matchInfoProvider;
    private final ParticipationEventPublisher eventPublisher;

    public void cancel(Participation participation, MatchInfo matchInfo, Long matchId) {
        boolean wasConfirmed = participation.getStatus() == ParticipationStatus.CONFIRMED;

        participationRepository.save(participation.cancel());

        if (wasConfirmed) {
            matchInfoProvider.removeParticipant(matchId);
        }

        publishCancelledEvent(participation, matchInfo);
    }

    private void publishCancelledEvent(Participation participation, MatchInfo matchInfo) {
        eventPublisher.publish(new ParticipationCancelledEvent(
                participation.getId(),
                participation.getMatchId(),
                participation.getUserId(),
                matchInfo.title(),
                LocalDateTime.now()
        ));
    }
}
