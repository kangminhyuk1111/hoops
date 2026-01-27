package com.hoops.match.application.service;

import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.application.port.in.UpdateMatchCommand;
import com.hoops.match.application.port.in.UpdateMatchUseCase;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.policy.MatchPolicyValidator;
import com.hoops.match.application.port.out.MatchRepositoryPort;
import com.hoops.match.domain.vo.MatchSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchUpdater implements UpdateMatchUseCase {

    private final MatchRepositoryPort matchRepository;
    private final MatchPolicyValidator policyValidator;

    @Override
    public Match updateMatch(UpdateMatchCommand command) {
        Match match = matchRepository.findById(command.matchId())
                .orElseThrow(() -> new MatchNotFoundException(command.matchId()));

        MatchSchedule newSchedule = buildSchedule(command, match);

        policyValidator.validateUpdateMatch(newSchedule, command.maxParticipants());

        Match updatedMatch = match.update(
                command.userId(),
                command.title(),
                command.description(),
                command.matchDate(),
                command.startTime(),
                command.endTime(),
                command.maxParticipants()
        );

        return matchRepository.save(updatedMatch);
    }

    private MatchSchedule buildSchedule(UpdateMatchCommand command, Match match) {
        if (command.matchDate() == null && command.startTime() == null && command.endTime() == null) {
            return null;
        }
        return new MatchSchedule(
                command.matchDate() != null ? command.matchDate() : match.getMatchDate(),
                command.startTime() != null ? command.startTime() : match.getStartTime(),
                command.endTime() != null ? command.endTime() : match.getEndTime()
        );
    }
}
