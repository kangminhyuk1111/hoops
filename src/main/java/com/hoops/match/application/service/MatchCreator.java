package com.hoops.match.application.service;

import com.hoops.location.application.exception.LocationNotFoundException;
import com.hoops.location.domain.Location;
import com.hoops.location.domain.repository.LocationRepository;
import com.hoops.match.application.exception.InvalidMatchDateException;
import com.hoops.match.application.exception.InvalidMaxParticipantsException;
import com.hoops.match.application.exception.InvalidTimeRangeException;
import com.hoops.match.application.port.in.CreateMatchCommand;
import com.hoops.match.application.port.in.CreateMatchUseCase;
import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MatchCreator implements CreateMatchUseCase {

    private static final int MIN_PARTICIPANTS = 4;
    private static final int INITIAL_PARTICIPANTS = 1; // 호스트 포함

    private final MatchRepository matchRepository;
    private final LocationRepository locationRepository;

    public MatchCreator(MatchRepository matchRepository, LocationRepository locationRepository) {
        this.matchRepository = matchRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public Match createMatch(CreateMatchCommand command) {
        validateMaxParticipants(command.maxParticipants());
        validateMatchDate(command.matchDate());
        validateTimeRange(command.startTime(), command.endTime());

        Location location = locationRepository.findById(command.locationId())
                .orElseThrow(() -> new LocationNotFoundException(command.locationId()));

        Match match = new Match(
                null,
                command.hostId(),
                command.title(),
                command.description(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAddress(),
                command.matchDate(),
                command.startTime(),
                command.endTime(),
                command.maxParticipants(),
                INITIAL_PARTICIPANTS,
                MatchStatus.PENDING
        );

        return matchRepository.save(match);
    }

    private void validateMaxParticipants(Integer maxParticipants) {
        if (maxParticipants < MIN_PARTICIPANTS) {
            throw new InvalidMaxParticipantsException(maxParticipants);
        }
    }

    private void validateMatchDate(LocalDate matchDate) {
        if (matchDate.isBefore(LocalDate.now())) {
            throw new InvalidMatchDateException(matchDate);
        }
    }

    private void validateTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new InvalidTimeRangeException(startTime, endTime);
        }
    }
}
