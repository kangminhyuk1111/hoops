package com.hoops.match.application.service;

import com.hoops.match.application.event.MatchCreatedEvent;
import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.application.port.in.ReactivateMatchCommand;
import com.hoops.match.application.port.in.ReactivateMatchUseCase;
import com.hoops.match.domain.model.Match;
import com.hoops.match.application.port.out.MatchRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchReactivator implements ReactivateMatchUseCase {

    private final MatchRepositoryPort matchRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void reactivateMatch(ReactivateMatchCommand command) {
        Match match = matchRepository.findById(command.matchId())
                .orElseThrow(() -> new MatchNotFoundException(command.matchId()));

        match.reactivate(command.userId());
        matchRepository.save(match);

        eventPublisher.publishEvent(new MatchCreatedEvent(
                match.getId(),
                match.getLongitude(),
                match.getLatitude()
        ));
    }
}
