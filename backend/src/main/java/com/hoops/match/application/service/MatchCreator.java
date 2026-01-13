package com.hoops.match.application.service;

import com.hoops.match.application.exception.OverlappingHostingException;
import com.hoops.match.application.port.in.CreateMatchCommand;
import com.hoops.match.application.port.in.CreateMatchUseCase;
import com.hoops.match.application.port.out.HostInfo;
import com.hoops.match.application.port.out.HostInfoProvider;
import com.hoops.match.application.port.out.LocationInfo;
import com.hoops.match.application.port.out.LocationInfoProvider;
import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;
import com.hoops.match.domain.policy.MatchPolicyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchCreator implements CreateMatchUseCase {

    private static final int INITIAL_PARTICIPANTS = 1;

    private final MatchRepository matchRepository;
    private final HostInfoProvider hostInfoProvider;
    private final LocationInfoProvider locationInfoProvider;
    private final MatchPolicyValidator policyValidator;

    @Override
    public Match createMatch(CreateMatchCommand command) {
        policyValidator.validateCreateMatch(command);

        validateNoOverlappingHosting(command);

        HostInfo host = hostInfoProvider.getHostInfo(command.hostId());
        LocationInfo location = locationInfoProvider.getLocationInfo(command.locationId());

        Match match = new Match(
                null,
                null,
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
                command.maxParticipants(),
                INITIAL_PARTICIPANTS,
                MatchStatus.PENDING,
                null
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
