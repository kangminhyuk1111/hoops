package com.hoops.match.domain.policy;

import com.hoops.match.domain.exception.InvalidMatchDateException;
import com.hoops.match.domain.exception.InvalidMaxParticipantsException;
import com.hoops.match.domain.exception.InvalidTimeRangeException;
import com.hoops.match.domain.exception.MatchTooFarException;
import com.hoops.match.domain.exception.MatchTooSoonException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 경기 생성/수정 정책 검증기 (순수 도메인 객체)
 *
 * 경기 생성/수정 시 비즈니스 규칙을 검증합니다.
 * 프레임워크 의존성 없이 순수 Java로 구현됩니다.
 */
public class MatchPolicyValidator {

    private static final int MIN_PARTICIPANTS = 4;
    private static final int MAX_PARTICIPANTS = 20;
    private static final int MIN_HOURS_BEFORE_MATCH = 1;
    private static final int MAX_DAYS_IN_ADVANCE = 14;

    /**
     * 경기 생성 시 유효성을 검증합니다.
     *
     * @param matchDate 경기 날짜
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @param maxParticipants 최대 참가 인원
     * @throws InvalidMaxParticipantsException 참가 인원 범위 초과
     * @throws InvalidMatchDateException 과거 날짜인 경우
     * @throws InvalidTimeRangeException 시작 시간이 종료 시간보다 늦은 경우
     * @throws MatchTooSoonException 경기 시작까지 1시간 미만
     * @throws MatchTooFarException 14일 이후 경기
     */
    public void validateCreateMatch(LocalDate matchDate, LocalTime startTime,
                                    LocalTime endTime, Integer maxParticipants) {
        validateMaxParticipants(maxParticipants);
        validateMatchDate(matchDate);
        validateTimeRange(startTime, endTime);
        validateMatchStartTime(matchDate, startTime);
        validateMatchNotTooFar(matchDate);
    }

    /**
     * 경기 수정 시 유효성을 검증합니다.
     * null이 아닌 필드만 검증합니다.
     *
     * @param matchDate 경기 날짜 (nullable)
     * @param startTime 시작 시간 (nullable)
     * @param endTime 종료 시간 (nullable)
     * @param maxParticipants 최대 참가 인원 (nullable)
     */
    public void validateUpdateMatch(LocalDate matchDate, LocalTime startTime,
                                    LocalTime endTime, Integer maxParticipants) {
        if (maxParticipants != null) {
            validateMaxParticipants(maxParticipants);
        }
        if (matchDate != null) {
            validateMatchDate(matchDate);
        }
        if (startTime != null && endTime != null) {
            validateTimeRange(startTime, endTime);
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
