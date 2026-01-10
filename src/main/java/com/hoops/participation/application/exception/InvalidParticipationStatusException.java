package com.hoops.participation.application.exception;

import com.hoops.common.exception.DomainException;

/**
 * 취소 불가능한 참가 상태 예외
 * CONFIRMED 상태가 아닌 참가를 취소하려 할 때 발생합니다
 */
public class InvalidParticipationStatusException extends DomainException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_PARTICIPATION_STATUS";

    public InvalidParticipationStatusException(Long participationId, String status) {
        super(DEFAULT_ERROR_CODE,
                String.format("취소할 수 없는 참가 상태입니다. (participationId: %d, status: %s)",
                        participationId, status));
    }
}
