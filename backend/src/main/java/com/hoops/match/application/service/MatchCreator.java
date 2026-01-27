package com.hoops.match.application.service;

import com.hoops.match.application.dto.HostInfoResult;
import com.hoops.match.application.dto.LocationInfoResult;
import com.hoops.match.application.exception.OverlappingHostingException;
import com.hoops.match.application.port.in.CreateMatchCommand;
import com.hoops.match.application.port.in.CreateMatchUseCase;
import com.hoops.match.application.port.out.HostInfoPort;
import com.hoops.match.application.port.out.LocationInfoPort;
import com.hoops.match.application.port.out.MatchGeoIndexPort;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.policy.MatchPolicyValidator;
import com.hoops.match.application.port.out.MatchRepositoryPort;
import com.hoops.match.domain.vo.MatchHost;
import com.hoops.match.domain.vo.MatchLocation;
import com.hoops.match.domain.vo.MatchSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchCreator implements CreateMatchUseCase {

    private final MatchRepositoryPort matchRepository;
    private final MatchGeoIndexPort matchGeoIndex;
    private final HostInfoPort hostInfoPort;
    private final LocationInfoPort locationInfoPort;
    private final MatchPolicyValidator policyValidator;

    @Override
    public Match createMatch(CreateMatchCommand command) {
        MatchSchedule schedule = new MatchSchedule(
                command.matchDate(),
                command.startTime(),
                command.endTime()
        );

        policyValidator.validateCreateMatch(schedule, command.maxParticipants());
        validateNoOverlappingHosting(command.hostId(), schedule);

        HostInfoResult hostInfo = hostInfoPort.getHostInfo(command.hostId());
        LocationInfoResult locationInfo = locationInfoPort.getLocationInfo(command.locationId());

        MatchHost host = new MatchHost(hostInfo.hostId(), hostInfo.nickname());
        MatchLocation location = new MatchLocation(
                locationInfo.latitude(),
                locationInfo.longitude(),
                locationInfo.address()
        );

        Match match = Match.create(
                host,
                command.title(),
                command.description(),
                location,
                schedule,
                command.maxParticipants()
        );

        Match savedMatch = matchRepository.save(match);
        matchGeoIndex.addMatch(savedMatch.getId(), savedMatch.getLongitude(), savedMatch.getLatitude());

        return savedMatch;
    }

    private void validateNoOverlappingHosting(Long hostId, MatchSchedule newSchedule) {
        List<Match> activeMatches = matchRepository.findActiveMatchesByHostId(hostId);

        for (Match existingMatch : activeMatches) {
            if (existingMatch.getSchedule().overlapsWith(newSchedule)) {
                throw new OverlappingHostingException(existingMatch.getId());
            }
        }
    }
}
