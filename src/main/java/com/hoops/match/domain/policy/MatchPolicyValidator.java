package com.hoops.match.domain.policy;

import com.hoops.match.application.exception.InvalidMatchDateException;
import com.hoops.match.application.exception.InvalidMaxParticipantsException;
import com.hoops.match.application.exception.InvalidTimeRangeException;
import com.hoops.match.application.port.in.CreateMatchCommand;
import java.time.LocalDate;
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

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new InvalidTimeRangeException(startTime, endTime);
        }
    }
}
