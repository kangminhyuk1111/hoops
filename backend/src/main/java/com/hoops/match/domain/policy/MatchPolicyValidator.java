package com.hoops.match.domain.policy;

import com.hoops.match.application.exception.InvalidMatchDateException;
import com.hoops.match.application.exception.InvalidMaxParticipantsException;
import com.hoops.match.application.exception.InvalidTimeRangeException;
import com.hoops.match.application.exception.MatchTooFarException;
import com.hoops.match.application.exception.MatchTooSoonException;
import com.hoops.match.application.port.in.CreateMatchCommand;
import com.hoops.match.application.port.in.UpdateMatchCommand;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

/**
 * 경기 생성 정책 검증기
 *
 * 경기 생성 시 비즈니스 규칙을 검증합니다.
 */
@Component
public class MatchPolicyValidator {

    private static final int MIN_PARTICIPANTS = 4;
    private static final int MAX_PARTICIPANTS = 20;
    private static final int MIN_HOURS_BEFORE_MATCH = 1;
    private static final int MAX_DAYS_IN_ADVANCE = 14;

    /**
     * 경기 생성 명령의 유효성을 검증합니다.
     *
     * @param command 경기 생성 명령
     * @throws InvalidMaxParticipantsException 최소 참가 인원 미달
     * @throws InvalidMatchDateException 과거 날짜인 경우
     * @throws InvalidTimeRangeException 시작 시간이 종료 시간보다 늦은 경우
     */
    public void validateCreateMatch(CreateMatchCommand command) {
        validateMaxParticipants(command.maxParticipants());
        validateMatchDate(command.matchDate());
        validateTimeRange(command.startTime(), command.endTime());
        validateMatchStartTime(command.matchDate(), command.startTime());
        validateMatchNotTooFar(command.matchDate());
    }

    /**
     * 경기 수정 명령의 유효성을 검증합니다.
     * null이 아닌 필드만 검증합니다.
     *
     * @param command 경기 수정 명령
     */
    public void validateUpdateMatch(UpdateMatchCommand command) {
        if (command.maxParticipants() != null) {
            validateMaxParticipants(command.maxParticipants());
        }
        if (command.matchDate() != null) {
            validateMatchDate(command.matchDate());
        }
        if (command.startTime() != null && command.endTime() != null) {
            validateTimeRange(command.startTime(), command.endTime());
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

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new InvalidTimeRangeException(startTime, endTime);
        }
    }

    private void validateMatchStartTime(LocalDate matchDate, LocalTime startTime) {
        LocalDateTime matchStartDateTime = LocalDateTime.of(matchDate, startTime);
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
