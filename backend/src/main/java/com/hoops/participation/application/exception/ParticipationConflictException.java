package com.hoops.participation.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 참가 신청 동시성 충돌 예외
 *
 * 동시에 여러 사용자가 참가 신청할 때 발생하는 충돌 예외입니다.
 * 낙관적 락(Optimistic Lock)에 의해 감지됩니다.
 */
public class ParticipationConflictException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "PARTICIPATION_CONFLICT";

    public ParticipationConflictException(Long matchId) {
        super(DEFAULT_ERROR_CODE,
                String.format("동시에 다른 사용자가 참가 신청하여 충돌이 발생했습니다. 다시 시도해주세요. (matchId: %d)", matchId));
    }
}
