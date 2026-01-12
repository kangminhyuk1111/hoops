package com.hoops.match.application.service;

import com.hoops.match.application.exception.InvalidMaxParticipantsUpdateException;
import com.hoops.match.application.exception.MatchCannotBeUpdatedException;
import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.application.exception.NotMatchHostException;
import com.hoops.match.application.port.in.UpdateMatchCommand;
import com.hoops.match.application.port.in.UpdateMatchUseCase;
import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.domain.policy.MatchPolicyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchUpdater implements UpdateMatchUseCase {

    private final MatchRepository matchRepository;
    private final MatchPolicyValidator policyValidator;

    @Override
    public Match updateMatch(UpdateMatchCommand command) {
        Match match = matchRepository.findById(command.matchId())
                .orElseThrow(() -> new MatchNotFoundException(command.matchId()));

        validateUpdate(match, command);

        policyValidator.validateUpdateMatch(command);

        Match updatedMatch = match.update(
                command.title(),
                command.description(),
                command.matchDate(),
                command.startTime(),
                command.endTime(),
                command.maxParticipants()
        );

        return matchRepository.save(updatedMatch);
    }

    private void validateUpdate(Match match, UpdateMatchCommand command) {
        if (!match.isHost(command.userId())) {
            throw new NotMatchHostException(match.getId(), command.userId());
        }

        if (!match.canUpdate()) {
            throw new MatchCannotBeUpdatedException(match.getId(), match.getStatus().name());
        }

        if (command.maxParticipants() != null
                && command.maxParticipants() < match.getCurrentParticipants()) {
            throw new InvalidMaxParticipantsUpdateException(
                    match.getId(),
                    match.getCurrentParticipants(),
                    command.maxParticipants()
            );
        }
    }
}
