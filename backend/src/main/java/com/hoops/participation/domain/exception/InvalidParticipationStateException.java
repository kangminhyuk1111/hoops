package com.hoops.participation.domain.exception;

import com.hoops.common.exception.DomainException;

/**
 * 참가 상태 전이가 불가능할 때 발생하는 도메인 예외
 * 예: CANCELLED 상태에서 cancel() 호출 시
 */
public class InvalidParticipationStateException extends DomainException {

    private static final String ERROR_CODE = "INVALID_PARTICIPATION_STATE";

    public InvalidParticipationStateException(String currentStatus, String attemptedOperation) {
        super(ERROR_CODE,
                String.format("Cannot %s participation in %s status", attemptedOperation, currentStatus));
    }
}
