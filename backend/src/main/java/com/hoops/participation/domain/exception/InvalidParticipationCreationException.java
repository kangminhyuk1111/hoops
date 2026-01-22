package com.hoops.participation.domain.exception;

import com.hoops.common.exception.DomainException;

/**
 * 참가 생성 시 필수 값이 없을 때 발생하는 도메인 예외
 */
public class InvalidParticipationCreationException extends DomainException {

    private static final String ERROR_CODE = "INVALID_PARTICIPATION_CREATION";

    public InvalidParticipationCreationException(String reason) {
        super(ERROR_CODE, String.format("Cannot create participation: %s", reason));
    }
}
