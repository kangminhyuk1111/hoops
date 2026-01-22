package com.hoops.match.domain.policy;

import com.hoops.match.domain.exception.InvalidMatchDateException;
import com.hoops.match.domain.exception.InvalidMaxParticipantsException;
import com.hoops.match.domain.exception.MatchTooFarException;
import com.hoops.match.domain.exception.MatchTooSoonException;
import com.hoops.match.domain.vo.MatchSchedule;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Match creation/update policy validator.
 *
 * Validates business policies that may change over time.
 * Domain invariants (like time range) are validated in Value Objects.
 */
public class MatchPolicyValidator {

    private static final int MIN_PARTICIPANTS = 4;
    private static final int MAX_PARTICIPANTS = 20;
    private static final int MIN_HOURS_BEFORE_MATCH = 1;
    private static final int MAX_DAYS_IN_ADVANCE = 14;

    /**
     * Validate business policies for match creation.
     * Time range invariant is validated in MatchSchedule VO.
     */
    public void validateCreateMatch(MatchSchedule schedule, Integer maxParticipants) {
        validateMaxParticipants(maxParticipants);
        validateMatchDate(schedule.date());
        validateMatchStartTime(schedule);
        validateMatchNotTooFar(schedule.date());
    }

    /**
     * Validate business policies for match update.
     * Only validates non-null fields.
     */
    public void validateUpdateMatch(MatchSchedule schedule, Integer maxParticipants) {
        if (maxParticipants != null) {
            validateMaxParticipants(maxParticipants);
        }
        if (schedule != null) {
            validateMatchDate(schedule.date());
        }
    }

    private void validateMaxParticipants(Integer maxParticipants) {
        if (maxParticipants < MIN_PARTICIPANTS || maxParticipants > MAX_PARTICIPANTS) {
            throw new InvalidMaxParticipantsException(maxParticipants);
        }
    }

    private void validateMatchDate(LocalDate matchDate) {
        if (matchDate.isBefore(LocalDate.now())) {
            throw new InvalidMatchDateException(matchDate);
        }
    }

    private void validateMatchStartTime(MatchSchedule schedule) {
        LocalDateTime matchStartDateTime = schedule.startDateTime();
        LocalDateTime minStartTime = LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_MATCH);

        if (matchStartDateTime.isBefore(minStartTime)) {
            throw new MatchTooSoonException(matchStartDateTime);
        }
    }

    private void validateMatchNotTooFar(LocalDate matchDate) {
        LocalDate maxDate = LocalDate.now().plusDays(MAX_DAYS_IN_ADVANCE);

        if (matchDate.isAfter(maxDate)) {
            throw new MatchTooFarException(matchDate, MAX_DAYS_IN_ADVANCE);
        }
    }
}
