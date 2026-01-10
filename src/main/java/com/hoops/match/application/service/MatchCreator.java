package com.hoops.match.application.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MatchCreator implements CreateMatchUseCase {

    private static final int INITIAL_PARTICIPANTS = 1; // 호스트 포함

    private final MatchRepository matchRepository;
    private final HostInfoProvider hostInfoProvider;
    private final LocationInfoProvider locationInfoProvider;
    private final MatchPolicyValidator policyValidator;

    public MatchCreator(MatchRepository matchRepository, HostInfoProvider hostInfoProvider,
            LocationInfoProvider locationInfoProvider, MatchPolicyValidator policyValidator) {
        this.matchRepository = matchRepository;
        this.hostInfoProvider = hostInfoProvider;
        this.locationInfoProvider = locationInfoProvider;
        this.policyValidator = policyValidator;
    }

    @Override
    public Match createMatch(CreateMatchCommand command) {
        policyValidator.validateCreateMatch(command);

        HostInfo host = hostInfoProvider.getHostInfo(command.hostId());
        LocationInfo location = locationInfoProvider.getLocationInfo(command.locationId());

        Match match = new Match(
                null,
                null,  // version - 새로 생성 시 null
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
                MatchStatus.PENDING
        );

        return matchRepository.save(match);
    }
}
