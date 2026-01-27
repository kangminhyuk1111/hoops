package com.hoops.match.application.service;

import com.hoops.match.application.event.MatchRemovedFromGeoIndexEvent;
import com.hoops.match.application.exception.CancelReasonRequiredException;
import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.application.port.in.CancelMatchCommand;
import com.hoops.match.application.port.in.CancelMatchUseCase;
import com.hoops.match.domain.model.Match;
import com.hoops.match.application.port.out.MatchRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchCanceller implements CancelMatchUseCase {

    private final MatchRepositoryPort matchRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void cancelMatch(CancelMatchCommand command) {
        validateReason(command);

        Match match = matchRepository.findById(command.matchId())
                .orElseThrow(() -> new MatchNotFoundException(command.matchId()));

        match.cancel(command.userId());
        matchRepository.save(match);

        eventPublisher.publishEvent(new MatchRemovedFromGeoIndexEvent(match.getId()));
    }

    private void validateReason(CancelMatchCommand command) {
        if (command.reason() == null || command.reason().isBlank()) {
            throw new CancelReasonRequiredException(command.matchId());
        }
    }
}
