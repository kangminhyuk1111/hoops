package com.hoops.match.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 최대 참가자 수를 현재 참가자 수보다 작게 설정하려 할 때 발생하는 예외
 */
public class InvalidMaxParticipantsUpdateException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_MAX_PARTICIPANTS_UPDATE";

    public InvalidMaxParticipantsUpdateException(Long matchId, int currentParticipants, int requestedMax) {
        super(DEFAULT_ERROR_CODE,
                String.format("최대 참가자 수는 현재 참가자 수(%d)보다 작을 수 없습니다. (matchId: %d, 요청값: %d)",
                        currentParticipants, matchId, requestedMax));
    }
}
