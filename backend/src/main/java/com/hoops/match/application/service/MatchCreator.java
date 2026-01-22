package com.hoops.match.application.service;

import com.hoops.match.application.dto.HostInfoResult;
import com.hoops.match.application.dto.LocationInfoResult;
import com.hoops.match.application.exception.OverlappingHostingException;
import com.hoops.match.application.port.in.CreateMatchCommand;
import com.hoops.match.application.port.in.CreateMatchUseCase;
import com.hoops.match.application.port.out.HostInfoPort;
import com.hoops.match.application.port.out.LocationInfoPort;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.policy.MatchPolicyValidator;
import com.hoops.match.domain.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchCreator implements CreateMatchUseCase {

    private final MatchRepository matchRepository;
    private final HostInfoPort hostInfoPort;
    private final LocationInfoPort locationInfoPort;
    private final MatchPolicyValidator policyValidator;

    @Override
    public Match createMatch(CreateMatchCommand command) {
        policyValidator.validateCreateMatch(
                command.matchDate(),
                command.startTime(),
                command.endTime(),
                command.maxParticipants()
        );

        validateNoOverlappingHosting(command);

        HostInfoResult host = hostInfoPort.getHostInfo(command.hostId());
        LocationInfoResult location = locationInfoPort.getLocationInfo(command.locationId());

        Match match = Match.create(
                host.hostId(),
                host.nickname(),
                command.title(),
                command.description(),
                location.latitude(),
                location.longitude(),
                location.address(),
                command.matchDate(),
                command.startTime(),
                command.endTime(),
                command.maxParticipants()
        );

        return matchRepository.save(match);
    }

    private void validateNoOverlappingHosting(CreateMatchCommand command) {
        LocalDateTime newMatchStart = LocalDateTime.of(command.matchDate(), command.startTime());
        LocalDateTime newMatchEnd = LocalDateTime.of(command.matchDate(), command.endTime());

        List<Match> activeMatches = matchRepository.findActiveMatchesByHostId(command.hostId());

        for (Match existingMatch : activeMatches) {
            if (existingMatch.overlapsWithTime(newMatchStart, newMatchEnd)) {
                throw new OverlappingHostingException(existingMatch.getId());
            }
        }
    }
}
